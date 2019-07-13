package main

import (
	"encoding/json"
	"fmt"
	"math/rand"
	"time"

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

func publishSensor(client MQTT.Client, topic string, sensorID string) {
	for true {
		fmt.Println("---- doing publish ----")
		sensorData := Sensor{"Plant One", "Temperature", sensorID, rand.Intn(100-30) + 30, time.Now().UnixNano()}
		payload, err := json.Marshal(sensorData)
		if err == nil {
			fmt.Println(string(payload))
			token := client.Publish(topic, 0, false, string(payload))
			time.Sleep(time.Duration(rand.Intn(2)) * time.Second)
			token.Wait()
		}
	}
}

func main() {
	topicSensorA := "sitec/plant_one/sensors/a"
	topicSensorB := "sitec/plant_one/sensors/b"
	broker := "tcp://localhost:1883"
	id := "plant_one"

	opts := MQTT.NewClientOptions()
	opts.AddBroker(broker)
	opts.SetClientID(id)

	client := MQTT.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		panic(token.Error())
	}
	fmt.Println("Plant One Publisher Started")
	go publishSensor(client, topicSensorA, "A")
	publishSensor(client, topicSensorB, "B")
	client.Disconnect(250)
	fmt.Println("Plant One Publisher Disconnected")

}
