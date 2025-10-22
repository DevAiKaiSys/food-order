# Project Structure

```
website/
├── docker/
│   ├── Dockerfile
│   ├── docker-compose.yml
├── my-app/
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/       # Singleton services
│   │   │   ├── features/   # Feature modules (customer, admin)
│   │   │   ├── shared/     # Reusable components, models
│   │   ├── environments/
```