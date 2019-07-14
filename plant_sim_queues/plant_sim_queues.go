package main

import (
	"fmt"
	"strconv"

	"github.com/layornos/godes"
)

//Input Parameters
const (
	MAX_ROBOTS         = 10
	MAX_OPERATORS      = 1
	ARRIVAL_INTERVAL   = 1
	PROCESS_TIME       = 200
	PROCESS_TIME_SIGMA = 1.0
	CHECK_TIME         = 100
	CHECK_TIME_SIGMA   = 10.0
	SHUTDOWN_TIME      = 1 * 60.
)

// Setting up of the three queues in the system
var itemArrivalQueue = godes.NewFIFOQueue("Item Arrival Queue")
var checkingQueue = godes.NewFIFOQueue("Checking Queue")
var repairQueue = godes.NewFIFOQueue("Repair Queue")

var arrivalOfItems = godes.NewExpDistr(false)
var processOfItem = godes.NewNormalDistr(false)
var checkOfItem = godes.NewNormalDistr(false)
var repair = godes.NewNormalDistr(true)

var robotAvailableSwt = godes.NewBooleanControl()
var operatorAvailableSwt = godes.NewBooleanControl()

var occupiedRobots = 0
var busyOperators = 0
var robots *Robots
var operators *Operators

var itemCount = 0
var checkedItems = 0

type Item struct {
	*godes.Runner
	id string
}
type Robots struct {
	max int
}

type Operators struct {
	max int
}

type Maintainers struct {
	max int
}

func (robots *Robots) Catch(item *Item) {
	for {
		robotAvailableSwt.Wait(true)
		if itemArrivalQueue.GetHead().(*Item).id == item.id {
			break
		} else {
			godes.Yield()
		}
	}
	occupiedRobots++
	if occupiedRobots == robots.max {
		robotAvailableSwt.Set(false)
	}

}

func (robots *Robots) Release() {
	occupiedRobots--
	robotAvailableSwt.Set(true)
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

func main() {
	itemArrivalQueue.Clear()
	checkingQueue.Clear()
	robots = &Robots{MAX_ROBOTS}
	operators = &Operators{MAX_OPERATORS}

	robotAvailableSwt.Set(true)
	operatorAvailableSwt.Set(true)
	godes.Run()
	for {
		item := &Item{&godes.Runner{}, strconv.Itoa(itemCount)}
		if (itemCount%100) == 0 && itemCount > 0 {
			checkingQueue.Place(item)
			checkedItems++
		}
		itemArrivalQueue.Place(item)
		godes.AddRunner(item)
		godes.Advance(arrivalOfItems.Get(1. / ARRIVAL_INTERVAL))
		if godes.GetSystemTime() > SHUTDOWN_TIME {
			break
		}
		fmt.Println(itemArrivalQueue.Len())
		itemCount++
	}
	fmt.Printf("Number of processed items: %d\n", itemCount)
	fmt.Printf("Number of checked items: %d\n", checkedItems)
	godes.WaitUntilDone()
	fmt.Println("Done")
}
