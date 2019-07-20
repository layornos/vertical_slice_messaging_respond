package main

import (
	"fmt"
	"strconv"

	"github.com/layornos/godes"
)

//Input Parameters
const (
	MAX_ROBOTS      = 3
	MAX_OPERATORS   = 5
	MAX_MAINTAINERS = 3
	MAX_QS          = 1

	// How long to wait for the n * 100th item
	ARRIVAL_INTERVAL = 10

	// Time Operator needs to go to the item checkup
	TRANSPORT_TIME       = 10
	TRANSPORT_TIME_SIGMA = .1

	CHECK_TIME        = 100
	CHECK_TIME_SIGMA  = .1
	REPAIR_TIME       = 3000
	REPAIR_TIME_SIGMA = .2
	SHUTDOWN_TIME     = 5 * 24 * 60

	ITEM_INTACT       = 0
	ITEM_INTACT_SIGMA = .1
	ITEM_CHECK        = .2
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
var itemIntact = godes.NewNormalDistr(true)

var robotAvailableSwt = godes.NewBooleanControl()
var operatorAvailableSwt = godes.NewBooleanControl()
var maintainerAvailableSwt = godes.NewBooleanControl()
var qsAvailableSwt = godes.NewBooleanControl()

var robotsAvailable = MAX_ROBOTS
var busyOperators = 0
var busyMaintainers = 0
var busyQS = 0
var defectCount = 0

var operators *Operators
var maintainers *Maintainers
var qs *QualityAssurance

var itemCount = 0
var itemsProcessed = 0
var checkedItems = 0
var repairedRobots = 0

type Item struct {
	*godes.Runner
	id     string
	defect bool
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

func (item *Item) Run() {
	operators.Catch(item)
	itemArrivalQueue.Get()
	godes.Advance(transportItemToCheck.Get(TRANSPORT_TIME, TRANSPORT_TIME_SIGMA))
	operators.Release(item)

	qs.Catch(item)
	checkingQueue.Get()
	godes.Advance(checkOfItem.Get(CHECK_TIME, CHECK_TIME_SIGMA))
	if item.defect {
		defectCount++
		maintainers.Catch(item)
		machinesToRepairQueue.Get()
		godes.Advance(repairTimeOfOneRobot.Get(REPAIR_TIME, REPAIR_TIME_SIGMA))
		maintainers.Release()
		repairedRobots++
	}

	qs.Release(item)
	itemsProcessed++
}

func (operators *Operators) Catch(item *Item) {
	for {
		operatorAvailableSwt.Wait(true)
		if itemArrivalQueue.GetHead().(*Item).id == item.id {
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

func (maintainers *Maintainers) Catch(item *Item) {
	for {
		maintainerAvailableSwt.Wait(true)
		robotAvailableSwt.Wait(true)
		if machinesToRepairQueue.GetHead().(*Item).id == item.id {
			break
		} else {
			godes.Yield()
		}
	}
	busyMaintainers++
	robotsAvailable--

	if busyMaintainers == maintainers.max {
		maintainerAvailableSwt.Set(false)
	}

	if robotsAvailable == 0 {
		robotAvailableSwt.Set(false)
	}
}

func (maintainers *Maintainers) Release() {
	busyMaintainers--
	robotsAvailable++
	maintainerAvailableSwt.Set(true)
	robotAvailableSwt.Set(true)
}

func (qs *QualityAssurance) Catch(item *Item) {
	for {
		qsAvailableSwt.Wait(true)
		if checkingQueue.GetHead().(*Item).id == item.id {
			break
		} else {
			godes.Yield()
		}
	}
	busyQS++
	if busyQS == qs.max {
		qsAvailableSwt.Set(false)
	}

	if item.defect {
		machinesToRepairQueue.Place(item)
	}
}

func (qs *QualityAssurance) Release(item *Item) {
	busyOperators--
	checkedItems++
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
	qsAvailableSwt.Set(true)

	godes.Run()

	for {
		robotAvailableSwt.Wait(true)
		var item *Item
		if itemIntact.Get(ITEM_INTACT, ITEM_INTACT_SIGMA) > ITEM_CHECK {
			item = &Item{&godes.Runner{}, strconv.Itoa(itemCount), true}
		} else {
			item = &Item{&godes.Runner{}, strconv.Itoa(itemCount), false}
		}
		itemArrivalQueue.Place(item)
		godes.AddRunner(item)
		godes.Advance(arrivalOfItems.Get(1. / ARRIVAL_INTERVAL))
		if godes.GetSystemTime() > SHUTDOWN_TIME {
			break
		}

	}
	/*
		count := 0
		for i := 0; i < 100; i++ {
			result := itemIntact.Get(0, .1)
			if result > 0.2 {
				count++
			}
			fmt.Println(result)
		}
	fmt.Println(count)*/
	godes.WaitUntilDone()
	fmt.Printf("Number of checked items: %d\n", checkedItems)
	fmt.Printf("Number of defect items: %d\n", defectCount)
	fmt.Printf("Number of repaired robots: %d\n", repairedRobots)
	fmt.Println("Done")
}
