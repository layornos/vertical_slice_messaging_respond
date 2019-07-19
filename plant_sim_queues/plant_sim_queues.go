package main

import (
	"fmt"
	"strconv"

	"github.com/layornos/godes"
)

//Input Parameters
const (
	MAX_ROBOTS      = 3
	MAX_OPERATORS   = 1
	MAX_MAINTAINERS = 1
	MAX_QS          = 1

	// How long to wait for the n * 100th item
	ARRIVAL_INTERVAL = 20.0

	// Time Operator needs to go to the item checkup
	TRANSPORT_TIME       = 10
	TRANSPORT_TIME_SIGMA = .1

	MTTF_ROBOT        = 3000.0
	CHECK_TIME        = 100
	CHECK_TIME_SIGMA  = 1
	REPAIR_TIME       = 300
	REPAIR_TIME_SIGMA = 3.0
	SHUTDOWN_TIME     = 8 * 60.
)

/*
 * Setting up of the three queues in the system
 * itemArrival is dedicated to collect the items that must be transported to the quality assurance
 * checking is dedicated to collect the items that musst be checked by the quality assurance
 * machinesToRepair are dedicated to collect machines that must be repaired before they can continue
 */
var itemArrivalQueue = godes.NewFIFOQueue("Item Arrival Queue")
var checkingQueue = godes.NewFIFOQueue("Checking Queue")
var machinesToRepairQueue = godes.NewFIFOQueue("Machine Repair Queue")

var arrivalOfItems = godes.NewExpDistr(true)
var transportItemToCheck = godes.NewNormalDistr(true)
var checkOfItem = godes.NewNormalDistr(true)
var repairOfRobot = godes.NewExpDistr(true)
var repairTimeOfOneRobot = godes.NewNormalDistr(true)

var robotAvailableSwt = godes.NewBooleanControl()
var operatorAvailableSwt = godes.NewBooleanControl()
var maintainerAvailableSwt = godes.NewBooleanControl()
var qsAvailableSwt = godes.NewBooleanControl()

var robotsAvailable = 0
var busyOperators = 0
var busyMaintainers = 0
var busyQS = 0

var operators *Operators
var maintainers *Maintainers
var qs *QualityAssurance

var itemCount = 0
var itemsProcessed = 0
var checkedItems = 0
var repairedRobots = 0

type Item struct {
	*godes.Runner
	id string
}

type Robots struct {
	*godes.Runner
	id string
}

type Operators struct {
	max int
}

type QualityAssurance struct {
	max int
}

type Maintainers struct {
	max int
}

func (robot *Robots) Run() {
	for {
		robotAvailableSwt.Wait(true)
		robotsAvailable--
		if robotsAvailable == 0 {
			robotAvailableSwt.Set(false)
		}
		maintainerAvailableSwt.Wait(true)
		busyMaintainers++
		if busyMaintainers == maintainers.max {
			maintainerAvailableSwt.Set(false)
		}
		for {
			if machinesToRepairQueue.GetHead().(*Robots).id == robot.id {
				break
			} else {
				godes.Yield()
			}
		}
		machinesToRepairQueue.Get()
		godes.Advance(repairTimeOfOneRobot.Get(REPAIR_TIME, REPAIR_TIME_SIGMA))
		robotAvailableSwt.Set(true)
		maintainerAvailableSwt.Set(true)
	}
}

func (item *Item) Run() {
	operators.Catch(item)
	itemArrivalQueue.Get()
	godes.Advance(transportItemToCheck.Get(TRANSPORT_TIME, TRANSPORT_TIME_SIGMA))
	operators.Release(item)

	qs.Catch(item)
	checkingQueue.Get()
	godes.Advance(checkOfItem.Get(CHECK_TIME, CHECK_TIME_SIGMA))
	qs.Release(item)
}

func (operators *Operators) Catch(item *Item) {
	for {
		operatorAvailableSwt.Wait(true)
		if checkingQueue.GetHead().(*Item).id == item.id {
			break
		} else {
			godes.Yield()
		}
	}
	busyOperators++
	if busyOperators == operators.max {
		operatorAvailableSwt.Set(false)
	}
}

func (operators *Operators) Release(item *Item) {
	busyOperators--
	checkingQueue.Place(item)
	operatorAvailableSwt.Set(true)
}

func (maintainers *Maintainers) Catch(robot *Robots) {
	for {
		maintainerAvailableSwt.Wait(true)
		if checkingQueue.GetHead().(*Robots).id == robot.id {
			break
		} else {
			godes.Yield()
		}
	}
	busyMaintainers++

	if busyMaintainers == maintainers.max {
		maintainerAvailableSwt.Set(false)
	}
}

func (maintainers *Maintainers) Release() {
	busyMaintainers--
	maintainerAvailableSwt.Set(true)
}

func (qs *QualityAssurance) Catch(item *Item) {
	for {
		qsAvailableSwt.Wait(true)
		if machinesToRepairQueue.GetHead().(*Item).id == item.id {
			break
		} else {
			godes.Yield()
		}
	}
	busyQS++
	if busyQS == qs.max {
		qsAvailableSwt.Set(false)
	}
}

func (qs *QualityAssurance) Release(item *Item) {
	busyOperators--
	qsAvailableSwt.Set(true)
}

func main() {
	itemArrivalQueue.Clear()
	checkingQueue.Clear()
	machinesToRepairQueue.Clear()

	operators = &Operators{MAX_OPERATORS}
	maintainers = &Maintainers{MAX_MAINTAINERS}
	qs = &QualityAssurance{MAX_QS}

	operatorAvailableSwt.Set(true)
	maintainerAvailableSwt.Set(true)
	robotAvailableSwt.Set(true)

	godes.Run()

	for i := 0; i < MAX_ROBOTS; i++ {
		robot := &Robots{&godes.Runner{}, strconv.Itoa(i)}
		machinesToRepairQueue.Place(robot)
		godes.AddRunner(robot)
		godes.Advance(repairOfRobot.Get(1. / MTTF_ROBOT))
	}

	godes.WaitUntilDone()
	fmt.Printf("Number of processed items: %d\n", itemsProcessed)
	fmt.Printf("Number of checked items: %d\n", checkedItems)
	fmt.Printf("Number of repaired robots: %d\n", repairedRobots)
	fmt.Println("Done")
}
