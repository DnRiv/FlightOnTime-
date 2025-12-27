from pydantic import BaseModel, Field
from typing import Literal
from datetime import datetime

#Input
class FlightInput(BaseModel):
    airline: str = Field(..., example="AA")
    origin: str = Field(..., example="JFK")
    destination: str = Field(..., example="LAX")
    distance_miles: float = Field(..., gt=0, example=2475)
    departure_time: datetime = Field(..., example="2025-11-10T14:30:00")

#Output
class PredictionOutput(BaseModel):
    prediction: Literal["delayed", "on schedule"]
    probability: float = Field(..., ge=0, le=1, example=0.78)