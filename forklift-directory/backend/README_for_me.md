## Инструкция по запуску:

1. **Сохраните скрипт** как `create-project.ps1`
2. **Запустите PowerShell от имени Администратора** и выполните:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```
Перейдите в папку со скриптом и запустите:
```powershell
.\create-project.ps1
```

# Forklift Directory
## Quick Start
### 1. Start Database
```bash
cd database/docker
docker-compose up -d
```

### 2. Start Backend
```bash
# cd backend
mvn clean install
mvn spring-boot:run
```

### 3. Start Frontend
```bash
cd frontend
npm install
npm run dev
```

### 4. Open Application
Frontend: http://localhost:5173
Backend API: http://localhost:8080

#### API Endpoints
GET /api/forklifts/search?number={number}
POST /api/forklifts
PUT /api/forklifts/{id}
DELETE /api/forklifts/{id}

#### Downtimes
GET /api/downtimes/forklift/{forkliftId}
POST /api/downtimes
PUT /api/downtimes/{id}
DELETE /api/downtimes/{id}

==================== COMPLETE ====================
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Project structure created successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Start Docker Desktop"
Write-Host "2. cd database/docker && docker-compose up -d"
Write-Host "3. cd ../../backend && mvn spring-boot:run"
Write-Host "4. cd ../frontend && npm install && npm run dev"
Write-Host ""
Write-Host "Project path:((PWD.Path)" -ForegroundColor Cyan)

====================  DOCKER ==================== 
если хочешь проверить докер файл локально.

# 0. Удали старый образ (опционально)
docker rmi my-app
# 1. Собрать образ
docker build -t my-app .

# 2.1 Запустить контейнер
docker run -p 8080:8080 -e PORT=8080 my-app
# 2.2 Запустить контейнер через IDEA с БД
docker run -p 8080:8080 -e PORT=8080 -e DATABASE_URL="jdbc:postgresql://host.docker.internal:5432/forklift_db" my-app

# 3. Открыть в браузере
http://localhost:8080
