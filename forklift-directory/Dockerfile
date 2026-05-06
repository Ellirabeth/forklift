# ============================================
# 1. СТАДИЯ: СБОРКА VUE (ФРОНТЕНД)
# ============================================
FROM node:18-alpine AS vue-builder
WORKDIR /frontend

# Копируем настройки фронтенда
# Здесь важно указать правильную папку с твоим Vue-проектом
COPY frontend/package*.json ./

# Ставим зависимости
RUN npm ci

# Копируем весь исходный код фронтенда и собираем его
COPY frontend/ ./
RUN npm run build

# ============================================
# 2. СТАДИЯ: СБОРКА JAVA (БЭКЕНД) C MAVEN
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS java-builder

WORKDIR /app

## Копируем pom.xml, чтобы Docker закешировал зависимости
#COPY pom.xml .

# Копируем ВЕСЬ проект (включая backend папку)
COPY . .

# Работаем внутри папки backend
WORKDIR /app/backend

RUN mvn dependency:go-offline -B

# Забираем собранный фронтенд с предыдущей стадии
# и кладём в папку, откуда Spring Boot будет его раздавать
COPY --from=vue-builder /frontend/dist ./src/main/resources/static

# Собираем финальный JAR-файл
RUN mvn clean package -DskipTests

# ============================================
# 3. ФИНАЛЬНАЯ СТАДИЯ: ЛЁГКИЙ ЗАПУСК ПРИЛОЖЕНИЯ
# ============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем готовый JAR-файл
COPY --from=java-builder /app/backend/target/*.jar app.jar

# Создаём обычного пользователя (безопасность)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Порт по умолчанию — Railway подставит свой через переменную PORT
EXPOSE 8080

# Запуск: Railway сам даст переменную PORT, а мы её подхватим
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]