from fastapi import FastAPI
from pathlib import Path 
import joblib
import pandas as pd

app = FastAPI(title="Flight On Time API")

#Carga modelo
BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = BASE_DIR / "ds" / "artifacts" / "model.joblib"

model = joblib.load(MODEL_PATH)

@app.post("/predict")
def predict(flight: dict):
    df = pd.DataFrame([flight])
    prob = model.predict_proba(df)[0][1]
    prediction = int(prob >= 0.5)

    return {
        "prediction": "delayed" if prediction == 1 else "on schedule",
        "probability": round(float(prob), 2)
    }
