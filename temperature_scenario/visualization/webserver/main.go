package main

import (
	"log"
	"net/http"
)

func main() {

	http.Handle("/", http.FileServer(http.Dir("/Users/layornos/Projects/RESPOND/vertical_slice_messaging_respond/visualization/webpage")))
	log.Fatal(http.ListenAndServe(":8081", nil))
}
