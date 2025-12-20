import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.pipeline import Pipeline
from sklearn.impute import SimpleImputer

# Limpieza
def clean_data(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df = df[df["Cancelled"] == 0]
    df = df.dropna(subset=["ArrDelay"])
    return df

# Target
def create_target(df: pd.DataFrame, delay_threshold: int = 15) -> pd.DataFrame:
    df = df.copy()
    df["Is_delayed"] = (df["ArrDelay"] > delay_threshold).astype(int)
    return df

# Features
def prepare_features(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()

    # Convert DepTime a hora
    df["Dep_hour"] = df["CRSDepTime"] // 100

    features = [
        "Unique_carrier",
        "Origin",
        "Destination",
        "Day_of_week",
        "Dep_hour",
        "Distance_miles",
    ]

    return df[features]

# Preprocessor
def build_preprocessor():
    categorical_cols = ["Unique_carrier", "Origin", "Destination", "Day_of_week"]
    numerical_cols = ["Dep_hour", "Distance_miles"]

    preprocessor = ColumnTransformer(
        transformers=[
            ("cat", OneHotEncoder(handle_unknown="ignore"), categorical_cols),
            ("num", StandardScaler(), numerical_cols),
        ]
    )

    return preprocessor

# Dataset final
def build_dataset(df: pd.DataFrame):
    df = clean_data(df)
    df = create_target(df)

    X = prepare_features(df)
    y = df["Is_delayed"]

    return X, y, build_preprocessor()
