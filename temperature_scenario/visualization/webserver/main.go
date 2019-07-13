package main

import (
	"log"
	"net/http"
)

func main() {
	http.Handle("/", http.FileServer(http.Dir("/Users/layornos/Projects/RESPOND/vertical_slice_messaging/visualization/webpage")))

	log.Fatal(http.ListenAndServe(":8081", nil))
}
