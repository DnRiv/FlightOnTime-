# Data Science Service API

API FastAPI que expone el modelo de predicción de retrasos de vuelos.

## Endpoints principales

POST /predict
Recibe información de un vuelo y devuelve si probablemente estará puntual o retrasado.

Ejemplo de entrada (JSON)

{
  "airline": "AZ",
  "origin": "GIG",
  "destination": "GRU",
  "departure_time": "2025-11-10T14:30:00",
  "distance_miles": 350
}


Ejemplo de salida (JSON)

{
  "prediction": "delayed",
  "probability": 0.68
}


## Cómo probar la API

1. Activar el entorno virtual (.venv) y levantar la API:

    uvicorn main:app --reload

2. Abrir otra terminal y usar PowerShell:
    
$flight = @{
    airline = "AZ"
    origin = "GIG"
    destination = "GRU"
    departure_time = "2025-11-10T14:30:00"
    distance_miles = 350
} | ConvertTo-Json

Invoke-RestMethod -Uri http://127.0.0.1:8000/predict -Method Post -Body $flight -ContentType "application/json"

3. Para probar varios vuelos:

$vuelos = @(
    @{ airline = "AZ"; origin = "GIG"; destination = "GRU"; departure_time = "2025-11-10T14:30:00"; distance_miles = 350 },
    @{ airline = "AZ"; origin = "GRU"; destination = "GIG"; departure_time = "2025-11-11T09:15:00"; distance_miles = 350 }
    )

foreach ($vuelo in $vuelos) {
    $body = $vuelo | ConvertTo-Json
    $result = Invoke-RestMethod -Uri http://127.0.0.1:8000/predict -Method Post -Body $body -ContentType "application/json"
    Write-Host "Flight $($vuelo.departure_time): prediction = $($result.prediction), probability = $($result.probability)"
}



