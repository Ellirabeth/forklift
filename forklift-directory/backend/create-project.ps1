# create-project.ps1
Write-Host "Creating Forklift Directory project structure..." -ForegroundColor Green

# Create root directory
New-Item -ItemType Directory -Force -Path "forklift-directory" | Out-Null
Set-Location "forklift-directory"

# ==================== CREATE DIRECTORIES ====================
Write-Host "Creating directories..." -ForegroundColor Yellow

# Backend structure
New-Item -ItemType Directory -Force -Path "backend\src\main\java\com\forklift\controller" | Out-Null
New-Item -ItemType Directory -Force -Path "backend\src\main\java\com\forklift\service" | Out-Null
New-Item -ItemType Directory -Force -Path "backend\src\main\java\com\forklift\repository" | Out-Null
New-Item -ItemType Directory -Force -Path "backend\src\main\java\com\forklift\model\dto" | Out-Null
New-Item -ItemType Directory -Force -Path "backend\src\main\java\com\forklift\exception" | Out-Null
New-Item -ItemType Directory -Force -Path "backend\src\main\resources" | Out-Null

# Frontend structure
New-Item -ItemType Directory -Force -Path "frontend\src\api" | Out-Null
New-Item -ItemType Directory -Force -Path "frontend\src\components" | Out-Null
New-Item -ItemType Directory -Force -Path "frontend\src\views" | Out-Null
New-Item -ItemType Directory -Force -Path "frontend\src\router" | Out-Null
New-Item -ItemType Directory -Force -Path "frontend\src\styles" | Out-Null
New-Item -ItemType Directory -Force -Path "frontend\public" | Out-Null

# Database
New-Item -ItemType Directory -Force -Path "database\docker" | Out-Null

# ==================== BACKEND FILES ====================
Write-Host "Creating backend files..." -ForegroundColor Yellow

# ==================== .gitignore ====================
$gitignore = @'
# Java
target/
*.class
*.jar
*.war
*.ear

# Maven
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup

# IDE
.idea/
*.iml
*.iws
.classpath
.project
.settings/
.vscode/

# Node / Vue
frontend/node_modules/
frontend/dist/
frontend/.cache
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Docker
*.tar
*.pid

# OS
.DS_Store
Thumbs.db

# Logs
*.log
log/

# Environment
.env
.env.local
.env.*.local

# Backend специфичное (если есть)
backend/target/
backend/.mvn/

# Railway / Heroku
.railway/
railway.json
'@
Set-Content -Path ".gitignore" -Value $gitignore -Encoding UTF8