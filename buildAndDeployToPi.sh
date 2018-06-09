#!/bin/bash

HOST=pi@raspberrypi

./gradlew bootJar

scp build/libs/unsere-wg-bot-0.1.jar $HOST:/home/pi/unsere-wg-bot
ssh $HOST "sudo systemctrl restart unserewgbot"