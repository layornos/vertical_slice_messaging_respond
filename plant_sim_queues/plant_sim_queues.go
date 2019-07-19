package main

import (
	"fmt"
	"strconv"

	"github.com/layornos/godes"
)

//Input Parameters
const (
	MAX_ROBOTS         = 3
	MAX_OPERATORS      = 1
	MAX_MAINTAINERS    = 1
	ARRIVAL_INTERVAL   = .1
	PROCESS_TIME       = 20
	PROCESS_TIME_SIGMA = 1.0
	CHECK_TIME         = 100
	CHECK_TIME_SIGMA   = 10.0
	REPAIR_TIME        = 300
	REPAIR_TIME_SIGMA  = 30.0
	SHUTDOWN_TIME      = 8 * 60.
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
var checkOfItem = godes.NewNormalDistr(true)
var repairOfRobot = godes.NewUniformDistr(true)
var repairTimeOfOneRobot = godes.NewNormalDistr(true)

var robotAvailableSwt = godes.NewBooleanControl()
var operatorAvailableSwt = godes.NewBooleanControl()
var maintainerAvailableSwt = godes.NewBooleanControl()
var qsAvailableSwt = godes.NewBooleanControl()

var occupiedRobots = 0
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

func (item *Item) Run() {
	robots.Catch(item)
	itemArrivalQueue.Get()
	godes.Advance(processOfItem.Get(PROCESS_TIME, PROCESS_TIME_SIGMA))
	robots.Release()
	if (itemCount%100) == 0 && itemCount > 0 {
		fmt.Printf("Checking %dth item.\n", itemCount)
		operators.Catch(item)
		checkingQueue.Get()
		godes.Advance(checkOfItem.Get(CHECK_TIME, CHECK_TIME_SIGMA))
		if faultyItemsQueue.Len() > 0 {
			fmt.Printf("Repairing Robot!\n")
			maintainers.Catch(item)
			faultyItemsQueue.Get()
			godes.Advance(repairTimeOfOneRobot.Get(REPAIR_TIME, REPAIR_TIME_SIGMA))
			maintainers.Release()
		}
		operators.Release()
	}
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

func (operators *Operators) Release() {
	busyOperators--
	operatorAvailableSwt.Set(true)
}

func (maintainers *Maintainers) Catch(item *Item) {
	for {
		maintainerAvailableSwt.Wait(true)
		if faultyItemsQueue.GetHead().(*Item).id == item.id {
			break
		} else {
			godes.Yield()
		}
	}
	busyMaintainers++
	occupiedRobots++
	if busyMaintainers == maintainers.max {
		maintainerAvailableSwt.Set(false)
	}
}

func (maintainers *Maintainers) Release() {
	busyMaintainers--
	maintainerAvailableSwt.Set(true)
}

func (qs *QualityAssurance) Catch(item *Item){
	qsAvailableSwt.Wait(true)
	machinesToRepairQueue.GetHead().(*Item).id == item.id{
		break
	} else {
		godes.Yield()
	}
}

func (qs *QualityAssurance) Release(item *Item){
    busyOperators--
	qsAvailableSwt.Set(true)
	
}

func main() {
	itemArrivalQueue.Clear()
	checkingQueue.Clear()
	robots = &Robots{MAX_ROBOTS}
	operators = &Operators{MAX_OPERATORS}
	maintainers = &Maintainers{MAX_MAINTAINERS}

	robotAvailableSwt.Set(true)
	operatorAvailableSwt.Set(true)
	maintainerAvailableSwt.Set(true)

	godes.Run()
	for {
		item := &Item{&godes.Runner{}, strconv.Itoa(itemCount)}
		if (itemCount%100) == 0 && itemCount > 0 {
			checkingQueue.Place(item)
			checkedItems++
			if repairOfRobot.Get(0, 1) > .99 {
				faultyItemsQueue.Place(item)
				repairedRobots++
			}
		}
		itemArrivalQueue.Place(item)
		godes.AddRunner(item)
		godes.Advance(arrivalOfItems.Get(1. / ARRIVAL_INTERVAL))
		if godes.GetSystemTime() > SHUTDOWN_TIME {
			break
		}
		//fmt.Println(itemArrivalQueue.Len())
		itemCount++
	}
	godes.WaitUntilDone()
	fmt.Printf("Number of processed items: %d\n", itemsProcessed)
	fmt.Printf("Number of checked items: %d\n", checkedItems)
	fmt.Printf("Number of repaired robots: %d\n", repairedRobots)
	fmt.Println("Done")
}
