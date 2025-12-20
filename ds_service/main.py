from fastapi import FastAPI
from pathlib import Path
import joblib
import pandas as pd
from datetime import datetime

app = FastAPI(title="Flight On Time API")

# Load model
BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = BASE_DIR / "ds" / "artifacts" / "model.joblib"

model = joblib.load(MODEL_PATH)

@app.post("/predict")
def predict(flight: dict):
    try:
        # Transform input JSON to DataFrame with columns expected by the model
        df = pd.DataFrame([{
        "UniqueCarrier": flight["airline"],
        "Origin": flight["origin"],
        "Dest": flight["destination"],
        "Distance": flight["distance_miles"],
        "dep_hour": int(datetime.fromisoformat(flight["departure_time"]).hour),
        "DayOfWeek": datetime.fromisoformat(flight["departure_time"]).isoweekday()
        }])

        prob = model.predict_proba(df)[0][1]
        prediction = int(prob >= 0.5)

        return {
            "prediction": "delayed" if prediction == 1 else "on schedule",
            "probability": round(float(prob), 2)
        }

    except KeyError as e:
        return {"error": f"Missing field: {e}"}
    except ValueError as e:
        return {"error": f"Invalid format: {e}"}
    except Exception as e:
        return {"error": f"Internal error: {e}"}