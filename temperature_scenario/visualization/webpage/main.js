const plants = {
    "plant_one": "status_one",
    "plant_two": "status_two",
    "plant_example": "status_example"
};

websocket = new WebSocket("ws://localhost:8081/ws");

websocket.onopen = function (event) {
    for (const key in plants) {
        this.send(key)
    }
};

websocket.onmessage = function (event) {
    console.log("onMessageArrived:" + event.data);

    let info = event.data.split("/");
    let plant = info[0];
    let status = info[1] === "true";

    if (plant in plants) {
        const plant_id = plants[plant];
        if (status) {
            document.getElementById(plant_id).innerHTML = "ERROR";
            document.getElementById(plant_id).style.color = "red";
        }
        else {
            document.getElementById(plant_id).innerHTML = "OK";
            document.getElementById(plant_id).style.color = "#00b400";
        }
    }
};
