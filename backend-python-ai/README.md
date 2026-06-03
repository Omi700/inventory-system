# Backend Python AI Service

FastAPI service for Text-to-SQL and result summarization.

This service is internal. The Vue frontend should call the Java Spring Boot service, and the Java service calls this AI service.

## Run

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

## Endpoints

- `GET /health`
- `POST /ai/text-to-sql`
- `POST /ai/summarize-result`

