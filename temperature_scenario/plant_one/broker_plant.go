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
type Module struct {
	ModuleID   int
	StationID  int
	Main_ID    string
	Error_Code int
	Result     int
	Time       int64
}

func publishModule(client MQTT.Client, topic string, mainID string) {
	for true {
		fmt.Println("---- doing publish ----")
		sensorData := Module{1, rand.Intn(7), mainID, 0, 1, time.Now().UnixNano()}
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
	topic := "sitec/plant_one/sensors/a"
	//	topicSensorB := "sitec/plant_one/sensors/b"
	broker := "tcp://" + *brokerFlag + ":1883"
	id := "plant_one"

	opts := MQTT.NewClientOptions()
	opts.AddBroker(broker)
	opts.SetClientID(id)

	client := MQTT.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		panic(token.Error())
	}
	fmt.Println("Module 1 Publisher Started")
	go publishModule(client, topic, "A")
	go publishModule(client, topic, "B")
	go publishModule(client, topic, "C")
	go publishModule(client, topic, "D")
	go publishModule(client, topic, "E")
	go publishModule(client, topic, "F")
	publishModule(client, topic, "G")
	client.Disconnect(250)
	fmt.Println("Module 1 Publisher Disconnected")

}
