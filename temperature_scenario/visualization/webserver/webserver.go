package main

import (
	"fmt"
	"log"
	"net/http"
	"os"
	"path/filepath"
	"strings"
)

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

func main() {
	http.Handle("/", http.FileServer(http.Dir(getPath())))
	log.Fatal(http.ListenAndServe(":8081", nil))
}
