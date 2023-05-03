# !/bin/bash
set +x

./gradlew clean build

docker build -t "poss11111/clash-bot-service:1.0.0" .