
from fastapi import FastAPI

from connection import get_employees
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # allow all (for dev)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def test():
    return {"status": "working"}

@app.get("/employees")
def read_employees():
    data = get_employees()
    return {"employees": data}