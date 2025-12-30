from fastapi import FastAPI
import pandas as pd

from ds_service.model_loader import load_model
from ds_service.schemas import FlightInput, PredictionOutput

app = FastAPI(title="Flight On Time API")

model = load_model()

#Endpoint
@app.post("/predict", response_model=PredictionOutput)
def predict(flight: FlightInput):

    df = pd.DataFrame([{
        "Unique_carrier": flight.airline,
        "Origin": flight.origin,
        "Destination": flight.destination,
        "Distance_miles": flight.distance_miles,
        "Dep_hour": flight.departure_time.hour,
        "Day_of_week": flight.departure_time.isoweekday()
    }])

    prob = model.predict_proba(df)[0][1]

    return {
        "prediction": "delayed" if prob >= 0.5 else "on schedule",
        "probability": round(float(prob), 2)
    }