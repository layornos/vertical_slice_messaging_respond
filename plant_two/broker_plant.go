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
		payload := "PLANT 2: Sensor " + sensorID + " - Temperature: " + strconv.Itoa(rand.Intn(200-100)+100)
		fmt.Println(payload)
		token := client.Publish(topic, 0, false, payload)
		time.Sleep(time.Duration(rand.Intn(2)) * time.Second)
		token.Wait()
	}
}

func main() {
	topicSensorA := "sitec/plant_two/sensors/a"
	topicSensorB := "sitec/plant_two/sensors/b"
	broker := "tcp://localhost:1883"
	id := "plant_two"

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
