# Food Ordering

## Clone Project

```
git clone https://github.com/DevAiKaiSys/food-order.git
cd food-order
```

##  Running the Application

### Website (Frontend)

Production Mode

```
cd website/docker
docker compose up -d
```

Development Mode

```
cd website/docker-development
docker compose -f docker-compose.dev.yaml up -d
```

### Order Service (Backend) & MySQL

```
cd order-service/docker
docker compose up -d
```

## Troubleshooting

Stop services:

```
docker compose down        # Stop only
docker compose down -v     # Stop and remove volumes
```

Rebuild and restart:

```
docker compose up --build  # Rebuild with logs
```

Common issues:
- Check port conflicts
- Verify API endpoints in frontend
- Enable CORS in CorsConfig.java

## Git Flow

Feature Development:

```
main → feature/new-feature → PR → review → merge to release → merge to main
```

Bug Fix:

```
main → bugfix/fix-issue → PR → review → merge to release → merge to main
```

Hotfix (Urgent):

```
main → hotfix/critical-fix → commit directly to main
```