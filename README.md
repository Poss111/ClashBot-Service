# Clash Bot Spring Service

> Last updated: 5/2/2023

```bash
docker run --rm -it -p 4566:4566 -p 4510-4559:4510-4559 localstack/localstack
```

```bash
# Necessary to expose service
k port-forward service/clash-bot-service 8080:8080
```