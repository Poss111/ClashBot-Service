# !/bin/bash

helm list
helm package ./helm -d ../helm-charts/
helm upgrade -f ./helm/local-values.yaml -i --wait --debug clash-bot-service ./helm