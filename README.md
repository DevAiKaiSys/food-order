# Food Ordering

## Run project with Docker

### website

```
cd website/docker

# Production
docker compose up -d

# Development
docker compose -f docker-compose.dev.yaml up -d
```

### order-service & mysql

```
cd order-service/docker

docker compose up -d
```