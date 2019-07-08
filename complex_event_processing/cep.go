package main

import (
	"encoding/json"
	"fmt"
	"os"
	"strconv"

	MQTT "github.com/eclipse/paho.mqtt.golang"
)

// A Sensor is a Dummy Datastructure for Sensor Information on a Plant
type Sensor struct {
	Plant       string
	SensorName  string
	SensorID    string
	Temperature int
	Time        int64
}

//
type aggregatedPlantTemperature struct {
	SensorATemperature int
	SensorBTemperature int
}

func main() {
	topics := [4]string{"sitec/plant_one/sensors/a", "sitec/plant_one/sensors/b", "sitec/plant_two/sensors/a", "sitec/plant_two/sensors/b"}
	var message Sensor
	var tempPlantOne aggregatedPlantTemperature
	tempPlantOne.SensorATemperature = -1
	tempPlantOne.SensorBTemperature = -1

	var tempPlantTwo aggregatedPlantTemperature
	tempPlantTwo.SensorATemperature = -1
	tempPlantTwo.SensorBTemperature = -1

	broker := "tcp://localhost:1883"
	id := "cep"

	opts := MQTT.NewClientOptions()
	opts.AddBroker(broker)
	opts.SetClientID(id)

	choke := make(chan [2]string)

	opts.SetDefaultPublishHandler(func(client MQTT.Client, msg MQTT.Message) {
		choke <- [2]string{msg.Topic(), string(msg.Payload())}
	})

	client := MQTT.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		panic(token.Error())
	}

	for i := 0; i < 4; i++ {
		if token := client.Subscribe(topics[i], 0, nil); token.Wait() && token.Error() != nil {
			fmt.Println(token.Error())
			os.Exit(1)
		}
	}
	goOn := true

	for goOn {
		incoming := <-choke
		err := json.Unmarshal([]byte(incoming[1]), &message)
		if err == nil {
			if message.Plant == "Plant One" {
				if message.SensorID == "A" {
					tempPlantOne.SensorATemperature = message.Temperature
				} else {
					tempPlantOne.SensorBTemperature = message.Temperature
				}
			} else {
				if message.SensorID == "A" {
					tempPlantTwo.SensorATemperature = message.Temperature
				} else {
					tempPlantTwo.SensorBTemperature = message.Temperature
				}
			}
			if ((tempPlantOne.SensorATemperature + tempPlantOne.SensorBTemperature) / 2) > 90 {
				fmt.Println("Warning: Temperature in Plant One is " + strconv.Itoa((tempPlantOne.SensorATemperature+tempPlantOne.SensorBTemperature)/2) + ", that is to high!")
			}
			if ((tempPlantTwo.SensorATemperature + tempPlantTwo.SensorBTemperature) / 2) > 190 {
				fmt.Println("Warning: Temperature in Plant Two is " + strconv.Itoa((tempPlantTwo.SensorATemperature+tempPlantTwo.SensorBTemperature)/2) + ", that is to high!")
			}
			//fmt.Printf("RECEIVED TOPIC: %s MESSAGE: %s\n", incoming[0], incoming[1])
		}
	}

	client.Disconnect(250)
	fmt.Println("Sample Subscriber Disconnected")

}
