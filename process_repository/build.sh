#!/bin/sh
../gradlew shadowJar
cp build/libs/process_repository-all.jar docker/
