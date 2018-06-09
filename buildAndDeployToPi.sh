#!/bin/bash

set -e

HOST=pi@raspberrypi

./gradlew bootJar

scp build/libs/unsere-wg-bot-0.1.jar $HOST:/home/pi/unsere-wg-bot
ssh $HOST "sudo systemctl restart unserewgbot"

echo "SUCCESS!"