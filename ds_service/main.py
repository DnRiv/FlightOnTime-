from fastapi import FastAPI
from pathlib import Path
import joblib
import pandas as pd
from datetime import datetime
from pydantic import BaseModel, Field
from typing import Literal

app = FastAPI(title="Flight On Time API")

# Schemas
class FlightInput(BaseModel):
    airline: str = Field(..., example="AA")
    origin: str = Field(..., example="JFK")
    destination: str = Field(..., example="LAX")
    distance_miles: float = Field(..., example=2475, gt=0)
    departure_time: str = Field(..., example="2025-11-10T14:30:00")

class PredictionOutput(BaseModel):
    prediction: Literal["delayed", "on schedule"]
    probability: float = Field(..., ge=0, le=1, example=0.78)

# Load model
BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = BASE_DIR / "ds" / "artifacts" / "model.joblib"

model = joblib.load(MODEL_PATH)

# Endpoint
@app.post("/predict", response_model=PredictionOutput)
def predict(flight: FlightInput):

    df = pd.DataFrame([{
        "Unique_carrier": flight.airline,
        "Origin": flight.origin,
        "Destination": flight.destination,
        "Distance_miles": flight.distance_miles,
        "Dep_hour": datetime.fromisoformat(flight.departure_time).hour,
        "Day_of_week": datetime.fromisoformat(flight.departure_time).isoweekday()
    }])

    prob = model.predict_proba(df)[0][1]
    prediction = "delayed" if prob >= 0.5 else "on schedule"

    return {
        "prediction": prediction,
        "probability": round(float(prob), 2)
    }
