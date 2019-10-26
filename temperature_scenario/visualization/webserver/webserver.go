package main

import (
	"encoding/json"
	"flag"
	"fmt"
	MQTT "github.com/eclipse/paho.mqtt.golang"
	"github.com/gorilla/websocket"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"
)

type SensorData struct {
	Result    int
	MainID    string
	ErrorCode int
	Time      int64
}

var plantState = make(map[string]bool)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
}

var websockets = make([]*websocket.Conn, 0)

func getPath() string {
	ex, err := os.Executable()
	if err != nil {
		panic(err)
	}
	exPath := filepath.Dir(ex)
	exPath = strings.Replace(exPath, "webserver", "webpage", -1)
	fmt.Println(exPath)
	return exPath
}

func onWebsocket(w http.ResponseWriter, r *http.Request) {
	ws, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		panic(nil)
	}
	websockets = append(websockets, ws)
	webSocketListener(ws)
}

func webSocketListener(c *websocket.Conn) {
	for {
		messageType, plant, err := c.ReadMessage()
		if err != nil {
			return
		}

		if messageType == websocket.TextMessage {
			plantName := string(plant)
			err := c.WriteMessage(websocket.TextMessage, []byte(plantName+"/"+strconv.FormatBool(plantState[plantName])))
			if err != nil {
				return
			}
		}
	}
}

func mqttListen(brokerFlag *string) {
	broker := "tcp://" + *brokerFlag + ":1883"
	id := "visualisation"

	opts := MQTT.NewClientOptions()
	opts.AddBroker(broker)
	opts.SetClientID(id)

	client := MQTT.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		panic(token.Error())
	}

	client.Subscribe("sitec/#", 0, mqttHandler)
}

func mqttHandler(c MQTT.Client, m MQTT.Message) {
	var data SensorData
	err := json.Unmarshal(m.Payload(), &data)
	if err != nil {
		fmt.Println("Error parsing mqtt message")
		return
	}

	if data.ErrorCode != 0 {
		plant := strings.Split(m.Topic(), "/")[1]
		plantState[plant] = true

		tmp := websockets[:0]
		for _, ws := range websockets {
			err := ws.WriteMessage(1, []byte(plant+"/"+strconv.FormatBool(plantState[plant])))
			if err == nil {
				//Websocket still works
				tmp = append(tmp, ws)
			} else {
				_ = ws.Close()
			}
		}
		websockets = tmp
	}
}

func main() {
	brokerFlag := flag.String("broker", "localhost", "URL to the desired broker")
	flag.Parse()
	http.HandleFunc("/ws", onWebsocket);
	http.Handle("/", http.FileServer(http.Dir(getPath())))
	go mqttListen(brokerFlag)
	log.Fatal(http.ListenAndServe(":8081", nil))
}
