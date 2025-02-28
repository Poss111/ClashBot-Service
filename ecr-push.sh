#!/bin/bash

PROFILE="--profile Master"
TAG=$1
SERVICE_NAME="clash-bot-service-round-2"

if [ -z "$TAG" ]; then
    echo "Tag is required as the first argument."
    exit 1
fi

aws ecr get-login-password --region us-east-1 $PROFILE | docker login --username AWS --password-stdin 816923827429.dkr.ecr.us-east-1.amazonaws.com
docker build -t $SERVICE_NAME:$TAG .
docker tag $SERVICE_NAME:$TAG 816923827429.dkr.ecr.us-east-1.amazonaws.com/poss11111/$SERVICE_NAME:$TAG
docker push 816923827429.dkr.ecr.us-east-1.amazonaws.com/poss11111/$SERVICE_NAME:$TAG