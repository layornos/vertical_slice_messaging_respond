package main

import (
	"encoding/json"
	"flag"
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
		sensorData := Sensor{"Plant Two", "Temperature", sensorID, rand.Intn(200-100) + 100, time.Now().UnixNano()}
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
	brokerFlag := flag.String("broker", "localhost", "URL to the desired broker")
	flag.Parse()
	topicSensorA := "sitec/plant_two/sensors/a"
	topicSensorB := "sitec/plant_two/sensors/b"
	broker := "tcp://" + *brokerFlag + ":1883"
	id := "plant_two"

	opts := MQTT.NewClientOptions()
	opts.AddBroker(broker)
	opts.SetClientID(id)

	client := MQTT.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		panic(token.Error())
	}
	fmt.Println("Plant Two Publisher Started")
	go publishSensor(client, topicSensorA, "A")
	publishSensor(client, topicSensorB, "B")
	client.Disconnect(250)
	fmt.Println("Plant Two Publisher Disconnected")

}
