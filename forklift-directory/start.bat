@echo off
chcp 65001 >nul
:: ============================================
:: Forklift Directory — Стартовый скрипт (Windows)
:: ============================================
:: Использование:
::   start.bat              — полный запуск (БД + бэкенд + фронтенд)
::   start.bat --backend    — только бэкенд
::   start.bat --frontend   — только фронтенд
::   start.bat --db         — только БД
::   start.bat --test       — запустить тесты
::   start.bat --stop       — остановить всё
:: ============================================

setlocal enabledelayedexpansion

set RED=[31m
set GREEN=[32m
set YELLOW=[33m
set NC=[0m

set ROOT_DIR=%~dp0
set ROOT_DIR=%ROOT_DIR:~0,-1%
set BACKEND_DIR=%ROOT_DIR%\backend
set FRONTEND_DIR=%ROOT_DIR%\frontend
set DATABASE_DIR=%ROOT_DIR%\database\docker

if "%1"=="--stop" goto :stop_all
if "%1"=="--db" goto :start_db
if "%1"=="--backend" goto :start_backend_with_db
if "%1"=="--frontend" goto :start_frontend
if "%1"=="--test" goto :run_tests
goto :full_start

:start_db
echo [INFO] Запуск PostgreSQL через Docker...
cd /d "%DATABASE_DIR%"
if exist "docker-compose.yml" (
    docker-compose up -d
) else if exist "docker-compose.yaml" (
    docker-compose -f docker-compose.yaml up -d
)
echo [INFO] PostgreSQL запущен на порту 5432
goto :eof

:start_backend
echo [INFO] Сборка и запуск бэкенда...
cd /d "%BACKEND_DIR%"

:: Ищем jar-файл
set JAR_FILE=
for /f "tokens=*" %%f in ('dir /b /o-d target\*.jar 2^>nul') do (
    if not defined JAR_FILE set "JAR_FILE=%%f"
)

if not defined JAR_FILE (
    echo [WARN] JAR не найден. Выполняю сборку...
    call mvn clean package -DskipTests -q
    for /f "tokens=*" %%f in ('dir /b /o-d target\*.jar') do set "JAR_FILE=%%f"
)

echo [INFO] Запуск: target\%JAR_FILE%
start "Forklift Backend" cmd /c "java -jar target\%JAR_FILE%"
echo %! > "%ROOT_DIR%\.backend.pid"
echo [INFO] Бэкенд запущен
echo [INFO] API доступно по адресу: http://localhost:8080
goto :eof

:start_backend_with_db
call :start_db
call :start_backend
goto :eof

:start_frontend
echo [INFO] Запуск фронтенда...
cd /d "%FRONTEND_DIR%"

if not exist "node_modules" (
    echo [WARN] Устанавливаю зависимости npm...
    call npm install
)

start "Forklift Frontend" cmd /c "npm run dev"
echo [INFO] Фронтенд запущен
echo [INFO] Приложение доступно по адресу: http://localhost:5173
goto :eof

:run_tests
echo [INFO] Запуск тестов...
cd /d "%BACKEND_DIR%"
call mvn test
echo [INFO] Тесты завершены.
goto :eof

:stop_all
echo [INFO] Остановка всех процессов...
taskkill /f /fi "WINDOWTITLE eq Forklift Backend" 2>nul
taskkill /f /fi "WINDOWTITLE eq Forklift Frontend" 2>nul
cd /d "%DATABASE_DIR%"
if exist "docker-compose.yml" (
    docker-compose down 2>nul
) else if exist "docker-compose.yaml" (
    docker-compose -f docker-compose.yaml down 2>nul
)
if exist "%ROOT_DIR%\.backend.pid" del "%ROOT_DIR%\.backend.pid"
if exist "%ROOT_DIR%\.frontend.pid" del "%ROOT_DIR%\.frontend.pid"
echo [INFO] Всё остановлено.
goto :eof

:full_start
echo.
echo === Полный запуск Forklift Directory ===
echo.
call :start_db
call :start_backend
call :start_frontend
echo.
echo ============================================
echo   Приложение: http://localhost:5173
echo   API:        http://localhost:8080
echo ============================================
echo   Для остановки: start.bat --stop
echo ============================================
goto :eof