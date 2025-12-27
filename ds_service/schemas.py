from pydantic import BaseModel, Field
from typing import Literal

#Input
class FlightInput(BaseModel):
    airline: str = Field(..., example="AA", description="Airline IATA code")
    origin: str = Field(..., example="JFK", description="Origin airport code")
    destination: str = Field(..., example="LAX", description="Destination airport code")
    distance_miles: float = Field(..., example=2475, gt=0)
    departure_time: str = Field(..., example="2025-11-10T14:30:00", description="Departure time in ISO 8601 format")

#Output
class PredictionOutput(BaseModel):
    prediction: Literal["delayed", "on schedule"]
    probability: float = Field(..., ge=0, le=1, example=0.78)


