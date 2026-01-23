from pathlib import Path
import joblib


BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = BASE_DIR / "ds" / "artifacts" / "model.joblib"


def load_model():
    if not MODEL_PATH.exists():
        raise FileNotFoundError(f"Model not found at {MODEL_PATH}")

    return joblib.load(MODEL_PATH)