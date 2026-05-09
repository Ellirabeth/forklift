#!/bin/bash
# ============================================
# Forklift Directory — Стартовый скрипт
# ============================================
# Использование:
#   ./start.sh              — полный запуск (БД + бэкенд + фронтенд)
#   ./start.sh --backend    — только бэкенд
#   ./start.sh --frontend   — только фронтенд
#   ./start.sh --db         — только БД
#   ./start.sh --test       — запустить тесты
#   ./start.sh --stop       — остановить всё
# ============================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
DATABASE_DIR="$ROOT_DIR/database/docker"

start_db() {
    info "Запуск PostgreSQL через Docker..."
    if [ -f "$DATABASE_DIR/docker-compose.yml" ]; then
        cd "$DATABASE_DIR" && docker-compose up -d
        info "PostgreSQL запущен на порту 5432"
    else
        # Пробуем docker-compose.yaml
        cd "$DATABASE_DIR" && docker-compose -f docker-compose.yaml up -d
        info "PostgreSQL запущен на порту 5432"
    fi
}

start_backend() {
    info "Сборка и запуск бэкенда..."
    cd "$BACKEND_DIR"
    
    # Проверяем, есть ли уже собранный jar
    JAR_FILE=$(ls -t target/*.jar 2>/dev/null | head -1)
    
    if [ -z "$JAR_FILE" ]; then
        warn "JAR не найден. Выполняю сборку..."
        mvn clean package -DskipTests -q
        JAR_FILE=$(ls -t target/*.jar | head -1)
    fi
    
    info "Запуск: $JAR_FILE"
    java -jar "$JAR_FILE" &
    BACKEND_PID=$!
    echo $BACKEND_PID > "$ROOT_DIR/.backend.pid"
    info "Бэкенд запущен (PID: $BACKEND_PID)"
    info "API доступно по адресу: http://localhost:8080"
}

start_frontend() {
    info "Запуск фронтенда..."
    cd "$FRONTEND_DIR"
    
    if [ ! -d "node_modules" ]; then
        warn "Устанавливаю зависимости npm..."
        npm install
    fi
    
    npm run dev &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > "$ROOT_DIR/.frontend.pid"
    info "Фронтенд запущен (PID: $FRONTEND_PID)"
    info "Приложение доступно по адресу: http://localhost:5173"
}

stop_all() {
    info "Остановка всех процессов..."
    
    if [ -f "$ROOT_DIR/.backend.pid" ]; then
        kill $(cat "$ROOT_DIR/.backend.pid") 2>/dev/null || true
        rm "$ROOT_DIR/.backend.pid"
        info "Бэкенд остановлен"
    fi
    
    if [ -f "$ROOT_DIR/.frontend.pid" ]; then
        kill $(cat "$ROOT_DIR/.frontend.pid") 2>/dev/null || true
        rm "$ROOT_DIR/.frontend.pid"
        info "Фронтенд остановлен"
    fi
    
    if [ -f "$DATABASE_DIR/docker-compose.yml" ] || [ -f "$DATABASE_DIR/docker-compose.yaml" ]; then
        cd "$DATABASE_DIR" && docker-compose down 2>/dev/null || true
        info "PostgreSQL остановлен"
    fi
    
    info "Всё остановлено."
}

run_tests() {
    info "Запуск тестов..."
    cd "$BACKEND_DIR"
    mvn test
    info "Тесты завершены."
}

# === Главная логика ===

case "${1:-}" in
    --db)
        start_db
        ;;
    --backend)
        start_db
        start_backend
        ;;
    --frontend)
        start_frontend
        ;;
    --test)
        run_tests
        ;;
    --stop)
        stop_all
        ;;
    *)
        info "=== Полный запуск Forklift Directory ==="
        start_db
        start_backend
        start_frontend
        info ""
        info "============================================"
        info "  Приложение: http://localhost:5173"
        info "  API:        http://localhost:8080"
        info "============================================"
        info "  Для остановки: ./start.sh --stop"
        info "============================================"
        ;;
esac