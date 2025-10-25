# Cache

## ตรวจสอบ Redis

```bash
docker exec -it redis redis-cli ping
# ผลลัพธ์: PONG
```

## เข้า Redis CLI

```bash
docker exec -it redis redis-cli
```

## ดู key ทั้งหมดใน Redis

```bash
KEYS *
```

## ดูค่า cache ของ key

```bash
GET {key}

GET "orders::0-10-null-null"
GET "orders::1"
```