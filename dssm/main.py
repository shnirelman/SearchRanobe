from fastapi import FastAPI
from pydantic import BaseModel

import torch
import torch.nn as nn
import numpy as np
#import matplotlib.pyplot as plt
#from torch.utils.data import Dataset, DataLoader
import os
import json
from sentence_transformers import SentenceTransformer

print("start")

device = torch.device("cpu")

bert_model = SentenceTransformer('sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2')

print("bert")


bert_emb = 384
class DSSM(nn.Module):
    def __init__(self, hidden_dim=256):
        super().__init__()
        self.fc = nn.Sequential(
            nn.Linear(bert_emb, hidden_dim),
            nn.ReLU(),
            nn.Linear(hidden_dim, 256),
            nn.ReLU(),
            nn.Linear(256, 128),
        )


    def forward(self, query, doc):
        query_embed = self.fc(query)
        doc_embed = self.fc(doc)

        return torch.cosine_similarity(query_embed, doc_embed, dim=1)

#model = torch.load('model.pth')
model = DSSM()
model.load_state_dict(torch.load('model_state_dict.pth', map_location=torch.device('cpu')))
model.eval()
print("model")

all_docs = []
ids = []
doc_dir = "../json/description/"
doc_files = [os.path.join(doc_dir, f) for f in os.listdir(doc_dir) if f.endswith(('.json'))]
cnt = 0
for doc_file in doc_files:
    with open(doc_file) as f:
        data_JSON = f.read()
    data_dict = json.loads(data_JSON)
    book_name = data_dict["name"]
    description = data_dict["description"]
    book_id = data_dict["ranobe_id"]

    doc_str = "Название: " + book_name + " Описание: " + description
    all_docs.append(doc_str)
    ids.append(book_id)

print("read all files")

doc_embeddings = model.fc(bert_model.encode(all_docs, convert_to_tensor=True).to(device))
doc_embeddings = doc_embeddings.cpu().detach().numpy().astype("float32")

print("doc_embeddings")

def calc_scores(query):
    query_emb = bert_model.encode(query, convert_to_tensor=True).to(device)
    res = []
    for i in range(len(all_docs)):
        doc_emb = bert_model.encode(all_docs[i], convert_to_tensor=True).to(device)
        dist = model(doc_emb.view(1, -1), query_emb.view(1, -1)).cpu()
        res.append(dist.item())
    
    return res


class ScoresRequest(BaseModel):
    query: str

app = FastAPI()
 
@app.post("/scores")
def get_scores(request: ScoresRequest):
    scores = calc_scores(request.query)
    return {"scores": scores, "ids": ids}





