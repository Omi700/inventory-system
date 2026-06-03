# Inventory System

商品进销存管理系统，两周 MVP 版本。

## Project Layout

```text
inventory-system/
  backend-java/        # Spring Boot main business service
  backend-python-ai/   # FastAPI AI service for Text-to-SQL
  frontend/            # Vue 3 frontend
```

## Services

- Java main service: `http://localhost:8080`
- Python AI service: `http://localhost:8001`
- Frontend dev server: `http://localhost:5173`
- Dashboard WebSocket: `ws://localhost:8080/ws/dashboard`

## Recommended IDEs

- Open `backend-java/` with IntelliJ IDEA.
- Open `backend-python-ai/` with PyCharm.
- Open `frontend/` with VS Code.

## First Run

Database:

```bash
MYSQL_PWD='your-mysql-password' mysql -uroot < database/init.sql
```

Java:

```bash
cd backend-java
./mvnw spring-boot:run
```

Python:

```bash
cd backend-python-ai
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```
