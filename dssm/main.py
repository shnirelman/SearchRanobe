from fastapi import FastAPI
from pydantic import BaseModel
 
class ScoreseRequest(BaseModel):
    query: str

app = FastAPI()
 
@app.post("/scores")
def get_scores(request: ScoreseRequest):
    scores = [0, 0.1, 0.2, 0.15]
    return {"scores": scores, "query": request.query}
