#!/bin/bash
BASE_USER="http://localhost:8081"
BASE_TRIP="http://localhost:8082"
BASE_NOTIF="http://localhost:8083"

GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'; NC='\033[0m'
ok() { echo -e "${GREEN}✓${NC} $1"; }
err() { echo -e "${RED}✗${NC} $1"; }
info() { echo -e "${YELLOW}→${NC} $1"; }

echo "    Taxi Service Test   "
echo ""

# ===== 1. Получить новый токен =====
info "Получение токена..."
TOKEN=$(curl -s -X POST "$BASE_USER/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"ivan","password":"123","role":"PASSENGER"}' | \
  grep -o '"token":"[^"]*"' | cut -d'"' -f4 | tr -d '\r\n ')

if [[ -z "$TOKEN" ]]; then
  err "Не удалось получить токен"
  exit 1
fi
ok "Токен получен - $TOKEN "

# ===== 2. Создать пассажира =====
info "Создание пассажира..."
RESP=$(curl -s -w "|%{http_code}" -X POST "$BASE_USER/passengers" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Иван Петров","email":"ivan@taxi.test","phone":"+79991112233"}')
BODY="${RESP%|*}"
CODE="${RESP##*|}"
if [[ "$CODE" == "200" ]]; then
  ok "Пассажир создан: $BODY"
  PASSENGER_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | cut -d: -f2)
else
  err "Ошибка ($CODE): $BODY"
fi

# ===== 3. Создать водителей =====
for i in 1 2; do
  info "Создание водителя #$i..."
  NAME=$([ $i -eq 1 ] && echo "Алексей" || echo "Дмитрий")
  EMAIL=$([ $i -eq 1 ] && echo "alex@taxi.test" || echo "dmitry@taxi.test")
  PHONE=$([ $i -eq 1 ] && echo "+79994445566" || echo "+79997778899")
  LIC=$([ $i -eq 1 ] && echo "MSK001" || echo "MSK002")

  RESP=$(curl -s -w "|%{http_code}" -X POST "$BASE_USER/drivers" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"name\":\"$NAME\",\"email\":\"$EMAIL\",\"phone\":\"$PHONE\",\"licenseNumber\":\"$LIC\"}")
  BODY="${RESP%|*}"
  CODE="${RESP##*|}"
  if [[ "$CODE" == "200" ]]; then
    ok "Водитель #$i создан $BODY"
    eval "DRIVER${i}_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | cut -d: -f2)"
  else
    err "Водитель #$i: ошибка $CODE"
  fi
done

# ===== 4. Активировать водителей =====
info "Активация водителей..."
for id in "$DRIVER1_ID" "$DRIVER2_ID"; do
  curl -s -X PATCH "$BASE_USER/drivers/$id/status?status=AVAILABLE" \
    -H "Authorization: Bearer $TOKEN" > /dev/null && \
    ok "Водитель #$id — AVAILABLE"
done

# ===== 5. Создать поездку =====
info "Создание поездки..."
RESP=$(curl -s -w "|%{http_code}" -X POST "$BASE_TRIP/trips" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"passengerId\":$PASSENGER_ID,\"origin\":\"Красная площадь\",\"destination\":\"ВДНХ\",\"distanceKm\":12.5}")
BODY="${RESP%|*}"
CODE="${RESP##*|}"
if [[ "$CODE" == "200" ]]; then
  ok "Поездка создана: $BODY"
  TRIP_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | cut -d: -f2)
  DRIVER_ASSIGNED=$(echo "$BODY" | grep -o '"driverId":[0-9]*' | cut -d: -f2)
  [[ -n "$DRIVER_ASSIGNED" ]] && ok "Водитель назначен: #$DRIVER_ASSIGNED" || info "Водитель в очереди"
else
  err "Поездка: ошибка $CODE — $BODY"
  info " Debug: проверяем токен вручную..."
  curl -v -X POST "$BASE_TRIP/trips" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"passengerId\":$PASSENGER_ID,\"origin\":\"Test\",\"destination\":\"Test2\",\"distanceKm\":5.0}" 2>&1 | grep -E "(< HTTP|< |{)"
fi

# ===== 6. Проверить уведомления =====
info "Проверка уведомлений..."
sleep 2
NOTIFS=$(curl -s "$BASE_NOTIF/notifications?trip_id=$TRIP_ID" \
  -H "Authorization: Bearer $TOKEN")
if echo "$NOTIFS" | grep -q '"id"'; then
  ok "Уведомления найдены $BODY"
  echo "$NOTIFS" | grep -o '"message":"[^"]*"' | head -2 | sed 's/"message":"/  • /;s/"$//'
else
  info "Уведомлений пока нет "
fi

# ===== 7. Обновить статусы =====
info "Обновление статусов..."
curl -s -X PATCH "$BASE_TRIP/trips/$TRIP_ID/status?status=IN_PROGRESS" \
  -H "Authorization: Bearer $TOKEN" | grep -q "IN_PROGRESS" && ok "→ IN_PROGRESS"
curl -s -X PATCH "$BASE_TRIP/trips/$TRIP_ID/status?status=COMPLETED" \
  -H "Authorization: Bearer $TOKEN" | grep -q "COMPLETED" && ok "→ COMPLETED"

# ===== 8. Оценить поездку =====
info "Оценка поездки..."
curl -s -X PATCH "$BASE_TRIP/trips/$TRIP_ID/rate?rating=5" \
  -H "Authorization: Bearer $TOKEN" | grep -q '"rating":5' && ok "⭐⭐⭐⭐⭐"

# ===== 9. Статистика =====
info "Статистика за день..."
STATS=$(curl -s "$BASE_TRIP/trips/stats/daily" \
  -H "Authorization: Bearer $TOKEN")
TRIPS_COUNT=$(echo "$STATS" | grep -o '"tripsCount":[0-9]*' | cut -d: -f2)
AVG_PRICE=$(echo "$STATS" | grep -o '"averagePrice":[0-9.]*' | cut -d: -f2)
ok "Поездок: $TRIPS_COUNT | Средняя цена: $AVG_PRICE RUB"

# ===== 10. Проверка защиты =====
info "Проверка защиты (запрос без токена)..."
NO_AUTH=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_USER/passengers" \
  -H "Content-Type: application/json" \
  -d '{"name":"NoAuth","email":"x@x.x","phone":"+70000000000"}')
[[ "$NO_AUTH" == "401" || "$NO_AUTH" == "403" ]] && \
  ok "Защита работает: $NO_AUTH" || err "Ожидался 401/403, получено: $NO_AUTH"

echo ""
echo "    Все тесты завершены  "
