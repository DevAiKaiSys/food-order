# Project Structure

```
order-service/
├── docker/
│   ├── Dockerfile
│   ├── docker-compose.yml
├── my-api/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/food_order/
│   │   │   │   ├── controller/      # REST Controllers
│   │   │   │   ├── entity/          # JPA Entities
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/    # Flyway migrations
│   └── pom.xml
```