.PHONY: help build up down logs clean test build-all restart health

help:
	@echo "CityFix - Microservices Project"
	@echo "================================"
	@echo ""
	@echo "Dostępne komendy:"
	@echo ""
	@echo "  make build          - Budowanie wszystkich serwisów"
	@echo "  make up             - Uruchomienie Docker Compose (build + up)"
	@echo "  make down           - Zatrzymanie wszystkich kontenerów"
	@echo "  make logs           - Wyświetlanie logów wszystkich serwisów"
	@echo "  make logs-user      - Logi user-service"
	@echo "  make logs-report    - Logi report-service"
	@echo "  make logs-location  - Logi location-service"
	@echo "  make logs-gateway   - Logi gateway"
	@echo "  make logs-postgres  - Logi PostgreSQL"
	@echo "  make clean          - Zatrzymanie kontenerów i usunięcie volumów"
	@echo "  make restart        - Restart wszystkich serwisów"
	@echo "  make health         - Sprawdzenie statusu serwisów"
	@echo "  make test           - Uruchomienie testów"
	@echo "  make build-user     - Budowanie user-service"
	@echo "  make build-report   - Budowanie report-service"
	@echo "  make build-location - Budowanie location-service"
	@echo "  make build-gateway  - Budowanie gateway"

build:
	@echo "Budowanie wszystkich serwisów..."
	docker-compose build

up: build
	@echo "Uruchamianie Docker Compose..."
	docker-compose up -d
	@echo ""
	@echo "✅ Wszystkie serwisy zostały uruchomione!"
	@echo ""
	@echo "API Gateway:   http://localhost:8080"
	@echo "User Service:  http://localhost:8081/api/users"
	@echo "Report Service: http://localhost:8082/api/reports"
	@echo "Location Service: http://localhost:8083/api/locations"
	@echo ""

down:
	@echo "Zatrzymywanie Docker Compose..."
	docker-compose down

logs:
	docker-compose logs -f

logs-user:
	docker-compose logs -f user-service

logs-report:
	docker-compose logs -f report-service

logs-location:
	docker-compose logs -f location-service

logs-gateway:
	docker-compose logs -f gateway

logs-postgres:
	docker-compose logs -f postgres

logs-rabbitmq:
	docker-compose logs -f rabbitmq

clean:
	@echo "Czyszczenie wszystkich danych..."
	docker-compose down -v
	@echo "✅ Wszystkie kontenery i volumy zostały usunięte!"

restart:
	@echo "Restart wszystkich serwisów..."
	docker-compose restart
	@echo "✅ Wszystkie serwisy zostały zrestartowane!"

health:
	@echo "Sprawdzanie statusu serwisów..."
	@echo ""
	@echo "API Gateway:"
	curl -s http://localhost:8080/actuator/health | jq . || echo "❌ Niedostępny"
	@echo ""
	@echo "User Service:"
	curl -s http://localhost:8081/actuator/health | jq . || echo "❌ Niedostępny"
	@echo ""
	@echo "Report Service:"
	curl -s http://localhost:8082/actuator/health | jq . || echo "❌ Niedostępny"
	@echo ""
	@echo "Location Service:"
	curl -s http://localhost:8083/actuator/health | jq . || echo "❌ Niedostępny"

test:
	@echo "Uruchamianie testów..."
	./gradlew test

test-user:
	cd user-service && ../gradlew test

test-report:
	cd report-service && ../gradlew test

test-location:
	cd location-service && ../gradlew test

build-user:
	@echo "Budowanie user-service..."
	cd user-service && ../gradlew clean build -x test

build-report:
	@echo "Budowanie report-service..."
	cd report-service && ../gradlew clean build -x test

build-location:
	@echo "Budowanie location-service..."
	cd location-service && ../gradlew clean build -x test

build-gateway:
	@echo "Budowanie gateway..."
	cd gateway && ../gradlew clean build -x test

build-all: build-user build-report build-location build-gateway

ps:
	docker-compose ps

shell-user:
	docker-compose exec user-service bash

shell-report:
	docker-compose exec report-service bash

shell-location:
	docker-compose exec location-service bash

shell-gateway:
	docker-compose exec gateway bash

shell-postgres:
	docker-compose exec postgres psql -U cityfix_user -d cityfix

