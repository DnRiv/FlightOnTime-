from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import joblib

# Definir esquema con todas las columnas que el modelo espera
class FlightData(BaseModel):
    UniqueCarrier: str
    origin: str
    dest: str
    day_of_week: str
    part_of_day: str
    distance: float
    air_time: float
    dep_delay: float
    year: int
    month: int
    day: int
    hour: int
    flight: int
    sched_dep_time: int
    minute: int
    dep_time: int

app = FastAPI()

# Cargar el modelo
try:
    model = joblib.load("modelo_rf_pipeline.pkl")
except Exception as e:
    print("Error al cargar el modelo:", e)
    model = None

@app.post("/predict")
def predict(flight: FlightData):
    if model is None:
        return {"error": "Modelo no disponible. Verifica el archivo modelo_rf_pipeline.pkl"}

    try:
        # Convertir datos a DataFrame
        df = pd.DataFrame([flight.dict()])
        
        # Predicción de clase
        prediction = model.predict(df)[0]
        
        # Probabilidades (si el modelo lo soporta)
        if hasattr(model, "predict_proba"):
            proba = model.predict_proba(df)[0]
            # Asumimos que la clase '1' corresponde a "delayed"
            prob_delayed = round(float(proba[1]), 2)
        else:
            prob_delayed = None

        return {
            "delayed": int(prediction),
            "probability_delayed": prob_delayed
        }
    except Exception as e:
        return {"error": f"Error en la predicción: {str(e)}"}