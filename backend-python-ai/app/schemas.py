from pydantic import BaseModel, Field


class TextToSqlRequest(BaseModel):
    question: str = Field(..., min_length=1, max_length=500)
    schema_text: str = Field(..., alias="schema", min_length=1)
    rules: list[str] = Field(default_factory=list)


class TextToSqlResponse(BaseModel):
    sql: str
    reason: str


class SummarizeRequest(BaseModel):
    question: str = Field(..., min_length=1, max_length=500)
    sql: str = Field(..., min_length=1)
    columns: list[str] = Field(default_factory=list)
    rows: list[dict] = Field(default_factory=list)


class SummarizeResponse(BaseModel):
    summary: str

