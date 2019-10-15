package main

import (
	"encoding/csv"
	"encoding/json"
	"errors"
	"flag"
	"fmt"
	"os"
	"strconv"
	"time"

	MQTT "github.com/eclipse/paho.mqtt.golang"
)

const module = 1
const station = 2
const result = 3
const main_id = 4
const errorcode = 5
const timestamp = 6

type DataRow struct {
	Module    int
	Station   int
	Result    int
	MainID    string
	ErrorCode int
}

// A Sensor is a Dummy Datastructure for Sensor Information on a Plant
type SensorData struct {
	Result    int
	MainID    string
	ErrorCode int
	Time      int64
}

func publishSensor(client MQTT.Client, interval int, sourceFile string) {
	file, err := os.Open(sourceFile)
	if err != nil {
		fmt.Printf("\nError: %v \n ", err.Error())
		return
	}
	defer file.Close()

	reader := csv.NewReader(file)

	rows, err := reader.ReadAll()
	if err != nil {
		fmt.Printf("\nError: %v \n ", err.Error())
		return
	}

	for {
		for _, row := range rows {
			data := *toDataRow(row)
			fmt.Println("---- doing publish ----")
			sensorData := SensorData{data.Result, data.MainID, data.ErrorCode, time.Now().UnixNano()}
			payload, err := json.Marshal(sensorData)
			if err == nil {
				fmt.Println(string(payload))
				token := client.Publish(getTopic(data.Module, data.Station), 0, false, string(payload))
				time.Sleep(time.Duration(interval) * time.Millisecond)
				token.Wait()
			}
		}
	}
}

func toDataRow(arr []string) *DataRow {
	row := new(DataRow)
	var err error
	row.Module, err = strconv.Atoi(arr[module])
	handleParseError(err)
	row.Station, err = strconv.Atoi(arr[station])
	handleParseError(err)
	row.Result, err = strconv.Atoi(arr[result])
	handleParseError(err)
	row.MainID = arr[main_id]
	row.ErrorCode, err = strconv.Atoi(arr[errorcode])
	handleParseError(err)

	return row
}

func handleParseError(err error) {
	if err != nil {
		panic(errors.New("Malformed input data"))
	}
}

func getTopic(module int, station int) string {
	return "sitec/plant_example/sensors/TODO"
}

func main() {
	brokerFlag := flag.String("broker", "localhost", "URL to the desired broker")
	intervalFlag := flag.Int("interval", 1000, "Interval to send messages in in microseconds")
	sourceFileFlag := flag.String("source", "ECM_machine_data.csv", "The file to read the data from")
	flag.Parse()
	broker := "tcp://" + *brokerFlag + ":1883"
	id := "plant_example"

	opts := MQTT.NewClientOptions()
	opts.AddBroker(broker)
	opts.SetClientID(id)

	client := MQTT.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		panic(token.Error())
	}
	fmt.Println("Plant One Publisher Started")
	publishSensor(client, *intervalFlag, *sourceFileFlag)
	client.Disconnect(250)
	fmt.Println("Plant One Publisher Disconnected")

}
