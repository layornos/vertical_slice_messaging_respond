package main

import (
	"fmt"
	"strconv"

	"github.com/layornos/godes"
)

// ItemProcessingTime in minutes
const ItemProcessingTime = 10.0

// CheckTime per item in minutes
const CheckTime = 100.0

// CheckTimeSigma per item
const CheckTimeSigma = 10.0

// MTTF one item
const MTTFOneItem = 1000

//
type ItemCheckerRobot struct {
	*godes.Runner
	id        string
	itemCount int
}

//
type Operator struct {
	*godes.Runner
	id    string
	robot *ItemCheckerRobot
}

var checkingGen = godes.NewNormalDistr(true) // Generate random checking item
var faultyGen = godes.NewExpDistr(true)      // Generate random faulty item
var operatorAvailableSwt = godes.NewBooleanControl()
var maintainerAvailableSwt = godes.NewBooleanControl()
var shutdownTime float64 = 1 * 24 * 60

//
func (robot *ItemCheckerRobot) Run() {

	for {
		if godes.GetSystemTime() > shutdownTime {
			fmt.Printf("Robot %v produced %d items!\n", robot.id, robot.itemCount)
			break
		}
		godes.Advance(ItemProcessingTime)
		robot.itemCount++
		if (robot.itemCount > 0) && (robot.itemCount%100 == 0) {
			operatorAvailableSwt.Wait(false)
			operatorAvailableSwt.Set(true)
		}
	}
}

//
func (operator *Operator) Run() {
	robot := operator.robot
	for {
		operatorAvailableSwt.Wait(true)
		godes.Interrupt(robot)
		interrupted := godes.GetSystemTime()
		//fmt.Printf("Operator %v is checking item # %d on robot %v \n", operator.id, robot.itemCount, robot.id)
		godes.Advance(checkingGen.Get(CheckTime, CheckTimeSigma))

		//resume machine and change the scheduled time to compensate delay
		godes.Resume(robot, godes.GetSystemTime()-interrupted)
		//fmt.Println(godes.GetSystemTime())

		operatorAvailableSwt.Set(false)
	}
}

func main() {
	operatorAvailableSwt.Set(false)
	maintainerAvailableSwt.Set(false)
	godes.Run()

	var robot *ItemCheckerRobot
	for i := 0; i < 100; i++ {
		robot = &ItemCheckerRobot{&godes.Runner{}, strconv.Itoa(i), 0}
		godes.AddRunner(robot)
		godes.AddRunner(&Operator{&godes.Runner{}, "Hans", robot})
	}

	godes.WaitUntilDone()
}
