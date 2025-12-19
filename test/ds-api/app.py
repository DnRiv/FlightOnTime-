from fastapi import FastAPI
import joblib
import pandas as pd

app = FastAPI(title="Flight Delay Predictor")

# cargar modelo al iniciar la API
model = joblib.load("model.joblib")


@app.post("/predict")
def predict(data: dict):
    # convertir input en DataFrame
    X = pd.DataFrame([{
        "hora": data["hora"],
        "dia_semana": data["dia_semana"],
        "distancia": data["distancia"]
    }])

    # predicción
    prob = model.predict_proba(X)[0][1]
    pred = "Retrasado" if prob > 0.5 else "Puntual"

    return {
        "prevision": pred,
        "probabilidad": round(float(prob), 2)
    }
