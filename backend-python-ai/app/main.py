from fastapi import FastAPI

from app.schemas import (
    SummarizeRequest,
    SummarizeResponse,
    TextToSqlRequest,
    TextToSqlResponse,
)
from app.services import generate_sql, summarize_result

app = FastAPI(title="Inventory Python AI Service", version="0.1.0")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": "backend-python-ai"}


@app.post("/ai/text-to-sql", response_model=TextToSqlResponse)
def text_to_sql(request: TextToSqlRequest) -> TextToSqlResponse:
    sql, reason = generate_sql(request)
    return TextToSqlResponse(sql=sql, reason=reason)


@app.post("/ai/summarize-result", response_model=SummarizeResponse)
def summarize(request: SummarizeRequest) -> SummarizeResponse:
    return SummarizeResponse(summary=summarize_result(request))

