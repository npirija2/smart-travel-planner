# Service Run Commands

Use these commands from the repo root to start each backend service with local PostgreSQL.
Replace the placeholders with your own database name, username, and password.

## User Service

```bash
cd backend/user-service
DB_URL=jdbc:postgresql://localhost:5432/<user_db_name> \
DB_USERNAME=<db_username> \
DB_PASSWORD=<db_password> \
./mvnw spring-boot:run
```

## Planning Service

```bash
cd backend/planning-service
DB_URL=jdbc:postgresql://localhost:5432/<planning_db_name> \
DB_USERNAME=<db_username> \
DB_PASSWORD=<db_password> \
./mvnw spring-boot:run
```

## Communication Service

```bash
cd backend/communication-service
DB_URL=jdbc:postgresql://localhost:5432/<communication_db_name> \
DB_USERNAME=<db_username> \
DB_PASSWORD=<db_password> \
./mvnw spring-boot:run
```

## Finance Reservation Service

```bash
cd backend/finance-reservation-service
DB_URL=jdbc:postgresql://localhost:5432/<finance_db_name> \
DB_USERNAME=<db_username> \
DB_PASSWORD=<db_password> \
./mvnw spring-boot:run
```

## Notes

- If your password is empty, use `DB_PASSWORD=` rather than `DB_PASSWORD=""`.
- `planning-service` reads the same `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` variables as the other services.
- If you prefer using a `.env` file, source it before running Maven:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```
