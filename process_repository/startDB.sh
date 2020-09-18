#!/bin/sh

docker  run --name mariadb-respond -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mariadb:latest
