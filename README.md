# Разработка микросервисного сервиса такси

## Архитектура
![img_1.png](img_1.png)

## Сборка проекта  
- mvn clean package -DskipTests

## Запуск через Docker Compose 
- Пересобрать проект   
mvn clean package -DskipTests 
######
- Остановить старые контейнеры или с остановть с очещением бд  
docker compose down || docker compose down -v
#####
- Пересобрать и поднять сразу  
docker-compose up -d --build
######
- Пересобрать образы  
docker compose build --no-cache
######
- Запуск  
docker compose up -d
######
- Проверь статус  
docker compose ps
######
- Логи всех сервисов 
docker compose logs -f

## Тестовые данные

### 1. Получение токена
`TOKEN=$(curl -s -X POST http://localhost:8081/auth/login 
-H "Content-Type: application/json" 
-d '{"username":"ivan","password":"123","role":"PASSENGER"}' | 
grep -o '"token":"[^"]*"' | cut -d'"' -f4 | tr -d '\r\n ')
echo "Токен: $TOKEN"`

### 2. Регистрация пассажира
`curl -s -X POST http://localhost:8081/passengers -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{"name":"Тест","email":"test@mail.ru","phone":"+79001112233"}' | jq .`
### 3. Получить пассажира 
`curl -s http://localhost:8081/passengers/1 -H "Authorization: Bearer $TOKEN" | jq .`
### 4. Создание водителя
`curl -s -X POST http://localhost:8081/drivers -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{"name":"Водитель","email":"d@mail.ru","phone":"+79001112244","licenseNumber":"LIC001"}' | jq .`
### 5. Установка статуса AVAILABLE для водителей
`curl -s -X PATCH "http://localhost:8081/drivers/1/status?status=AVAILABLE" -H "Authorization: Bearer $TOKEN" | jq .`

### 6. Создание поездки (система автоматически назначит водителя)
`curl -s -X POST http://localhost:8082/trips -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{"passengerId":1,"origin":"Центр","destination":"Аэропорт","distanceKm":25.5}' | jq .`
### 7. История поездок
`curl -s "http://localhost:8082/trips?passenger_id=1" -H "Authorization: Bearer $TOKEN" | jq .`

### 8. Обновление статуса: водитель принял заказ
`curl -s -X PATCH "http://localhost:8082/trips/1/status?status=IN_PROGRESS" -H "Authorization: Bearer $TOKEN" | jq .`

### 9. Оценка поездки
`curl -s -X PATCH "http://localhost:8082/trips/1/rate?rating=5" -H "Authorization: Bearer $TOKEN" | jq .`

### 10. Статистика за день
`curl -s http://localhost:8082/trips/stats/daily -H "Authorization: Bearer $TOKEN" | jq .`

### 11. Проверка уведомлений
`curl -s "http://localhost:8083/notifications?trip_id=1" -H "Authorization: Bearer $TOKEN" | jq .`

### PostgreSQL: смотрим данные  
docker exec -it taxi-postgres psql -U taxi_user -d taxi_db -c "SELECT id,email,status FROM drivers; SELECT id,status,price FROM trips;"

### Redis: проверяем кэш  
docker exec -it taxi-redis redis-cli SMEMBERS drivers:available
- Должно быть пусто, если водитель занят