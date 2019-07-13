package main

import (
	"fmt"

	"github.com/layornos/godes"
)

// ItemProcessingTime in minutes
const ItemProcessingTime = 0.1

// CheckTime per item in minutes
const CheckTime = 10.0

// CheckTimeSigma per item
const CheckTimeSigma = 0.5

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

//
type Maintainer struct {
	*godes.Runner
	robot *ItemCheckerRobot
}

var checkingGen = godes.NewNormalDistr(true) // Generate random checking item
var faultyGen = godes.NewNormalDistr(true)   // Generate random faulty item
var operatorAvailableSwt = godes.NewBooleanControl()
var shutdownTime float64 = 5 * 24 * 60

//
func (robot *ItemCheckerRobot) Run() {

	for {
		if godes.GetSystemTime() > shutdownTime {
			break
		}
		godes.Advance(ItemProcessingTime)
		robot.itemCount++
		if (robot.itemCount > 0) && (robot.itemCount%100 == 0) {
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
		fmt.Printf("Operator %v is checking item # %d on robot %v \n", operator.id, robot.itemCount, robot.id)
		godes.Advance(checkingGen.Get(CheckTime, CheckTimeSigma))

		//resume machine and change the scheduled time to compensate delay
		godes.Resume(robot, godes.GetSystemTime()-interrupted)
		fmt.Println(godes.GetSystemTime())

		operatorAvailableSwt.Set(false)
	}
}

/*
func (maintainer *Maintainer) Run() {
	for {

	}
}
*/

func main() {
	operatorAvailableSwt.Set(false)
	godes.Run()

	robot := &ItemCheckerRobot{&godes.Runner{}, "Wall-E", 0}
	godes.AddRunner(robot)
	godes.AddRunner(&Operator{&godes.Runner{}, "Hans", robot})

	godes.WaitUntilDone()
}
