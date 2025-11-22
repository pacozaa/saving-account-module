# Docker Compose Setup - Fixed Issues

## Issues Fixed

### 1. ✅ Missing Docker Profile Configuration
**Problem:** Services used `SPRING_PROFILES_ACTIVE=docker` but no `application-docker.yml` files existed.

**Solution:** Created `application-docker.yml` for all 8 services with:
- Proper Eureka hostname: `eureka-server:8761` instead of `localhost:8761`
- Container-compatible hostnames for each service
- Database paths changed to `/data/` for volume mounting
- Changed `ddl-auto` from `create-drop` to `update` for data persistence

### 2. ✅ Dockerfile Build Context Issues
**Problem:** Each Dockerfile tried to copy from parent directory (`COPY ../pom.xml`) which doesn't work with docker compose build context.

**Solution:** Simplified all Dockerfiles to use pre-built JARs:
- Removed multi-stage build with Maven
- Now expects JARs to be built before `docker compose build`
- Updated `docker-build.sh` to run `./mvnw clean package` first

### 3. ✅ Database Volume Mounts
**Problem:** No volumes defined for H2 database persistence.

**Solution:** Added Docker volumes for all services with databases:
- `auth-data` for auth-service
- `register-data` for register-service  
- `account-data` for account-service
- `transaction-data` for transaction-service

## How to Use Docker Compose

### Build and Start (Recommended)
```bash
# Build JARs and Docker images
./docker-build.sh

# Start all services
./docker-start.sh
```

### Manual Steps
```bash
# 1. Build all Maven modules first
./mvnw clean package -DskipTests

# 2. Build Docker images
docker compose build

# 3. Start all services
docker compose up -d

# 4. View logs
docker compose logs -f

# 5. Stop all services
docker compose down
```

### Verify Services
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## What Changed

### New Files Created
- `eureka-server/src/main/resources/application-docker.yml`
- `api-gateway/src/main/resources/application-docker.yml`
- `auth-service/src/main/resources/application-docker.yml`
- `register-service/src/main/resources/application-docker.yml`
- `account-service/src/main/resources/application-docker.yml`
- `transaction-service/src/main/resources/application-docker.yml`
- `deposit-service/src/main/resources/application-docker.yml`
- `transfer-service/src/main/resources/application-docker.yml`

### Modified Files
- All 8 Dockerfiles (simplified to use pre-built JARs)
- `docker compose.yml` (added volume mounts)
- `docker-build.sh` (added Maven build step)

## Key Configuration Changes

### Eureka Configuration
- **Local:** `http://localhost:8761/eureka/`
- **Docker:** `http://eureka-server:8761/eureka/`

### Database Paths
- **Local:** `jdbc:h2:file:./data/bankingdb`
- **Docker:** `jdbc:h2:file:/data/authdb` (separate DB per service)

### DDL Strategy
- **Local:** `create-drop` (recreates schema each run)
- **Docker:** `update` (preserves data across restarts)

## Verification Checklist

- [x] All services have `application-docker.yml` with correct Eureka URL
- [x] All Dockerfiles work with docker compose build context
- [x] Database services have volume mounts for persistence
- [x] Service dependencies properly configured
- [x] Health checks configured for Eureka
- [x] Build script includes Maven build step

## Docker Compose Should Now Work! 🎉

All critical issues have been resolved. The setup should now:
1. Build JARs locally with Maven
2. Copy JARs into Docker images
3. Start services with proper networking
4. Services register with Eureka using container hostnames
5. Persist database data across restarts
