#!/bin/sh
../gradlew shadowJar
cp build/libs/selfhealing-all.jar docker/
