// Create a client instance
// client = new Paho.MQTT.Client("localhost", 9001, "webpage");
// client = new Paho.MQTT.Client("i43vm03.ira.uka.de", 9001, "webpage");
client = new Paho.MQTT.Client("localhost", 9001, "webpage");

// set callback handlers
client.onConnectionLost = onConnectionLost;
client.onMessageArrived = onMessageArrived;

// connect the client
client.connect({onSuccess:onConnect});


// called when the client connects
function onConnect() {
  // Once a connection has been made, make a subscription and send a message.
  console.log("onConnect");
  client.subscribe("sitec/+/error");
  //client.subscribe("sitec/plant_two/error");
}

// called when the client loses its connection
function onConnectionLost(responseObject) {
  if (responseObject.errorCode !== 0) {
    console.log("onConnectionLost:"+responseObject.errorMessage);
  }
}

// called when a message arrives
function onMessageArrived(message) {
  console.log("onMessageArrived:"+message.payloadString);
  payload = JSON.parse(message.payloadString)
  if(payload.Plant == "plant_one"){
    document.getElementById("status_one").innerHTML = "ERROR"
    document.getElementById("status_one").style.color = "red";
  }
  if(payload.Plant == "plant_two"){
    document.getElementById("status_two").innerHTML = "ERROR"
    document.getElementById("status_two").style.color = "red";
  }

}
