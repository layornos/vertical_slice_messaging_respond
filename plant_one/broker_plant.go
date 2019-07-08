package main

import (
	"fmt"
	"math/rand"
	"strconv"
	"time"

	MQTT "github.com/eclipse/paho.mqtt.golang"
)

func publishSensor(client MQTT.Client, topic string, sensorID string) {
	for true {
		fmt.Println("---- doing publish ----")
		payload := "PLANT 1: Sensor " + sensorID + " - Temperature: " + strconv.Itoa(rand.Intn(100-30)+30)
		fmt.Println(payload)
		token := client.Publish(topic, 0, false, payload)
		time.Sleep(time.Duration(rand.Intn(2)) * time.Second)
		token.Wait()
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
	fmt.Println("Publisher Started")
	go publishSensor(client, topicSensorA, "A")
	publishSensor(client, topicSensorB, "B")
	client.Disconnect(250)
	fmt.Println("Sample Publisher Disconnected")

}
