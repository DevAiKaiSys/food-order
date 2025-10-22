# Project Structure

```
order-service/
├── api-test/
├── docker/
│   ├── Dockerfile
│   ├── docker-compose.yml
├── my-api/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/orderservice/
│   │   │   │   ├── config/          # Configuration classes
│   │   │   │   ├── controller/      # REST Controllers
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── entity/          # JPA Entities
│   │   │   │   ├── exception/       # Exception handlers
│   │   │   │   ├── repository/      # JPA Repositories
│   │   │   │   ├── service/         # Business logic
│   │   │   │   └── util/            # Utilities
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/    # Flyway migrations
│   │   └── test/                    # Unit & Integration tests
│   └── pom.xml
```