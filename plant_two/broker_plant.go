package main

import (
	"fmt"
	"strconv"
	"time"

	MQTT "github.com/eclipse/paho.mqtt.golang"
)

func main() {
	topic := "sitec/plant_two/sensors/a"
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
	for i := 1; i <= 30; i++ {
		fmt.Println("---- doing publish ----")
		payload := "This is temperature " + strconv.Itoa(i*100) + " of sensor a on plant two"
		fmt.Println(payload)
		token := client.Publish(topic, 0, false, payload)
		time.Sleep(2 * time.Second)
		token.Wait()
	}

	client.Disconnect(250)
	fmt.Println("Sample Publisher Disconnected")

}
