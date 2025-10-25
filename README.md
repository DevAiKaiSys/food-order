# Food Ordering

## Run project with Docker

### website

run for Production

```
cd website/docker

docker compose up -d
```

run for Development

```
cd website/docker-development

docker compose -f docker-compose.dev.yaml up -d
```

### order-service & mysql

```
cd order-service/docker

docker compose up -d
```