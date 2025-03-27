#!/bin/bash

# This script is used to stop a service by setting the desired count to 0
# The script will wait for the running tasks to be 0 before exiting

CLUSTER_NAME="main-cluster"
SERVICE_NAME=$1

echo "Updating service $SERVICE_NAME to set desired count to 0..."
aws ecs update-service --cluster $CLUSTER_NAME --service $SERVICE_NAME --desired-count 0 | jq "{service: .service.serviceName, desiredCount: .service.desiredCount}"

# Wait for the actual running tasks to be 0, the timeout is 10 minutes
echo "Waiting for the running tasks to be 0..."
timeout 600 bash -c "while [[ $(aws ecs describe-services --cluster $CLUSTER_NAME --services $SERVICE_NAME | jq \".services[0].runningCount\") -ne 0 ]]; do sleep 5; done"

echo "Service $SERVICE_NAME has been successfully stopped."