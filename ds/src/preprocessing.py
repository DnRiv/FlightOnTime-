import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder

#funcion de limpieza

def clean_data(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()

    # Quitar vuelos cancelados
    df = df[df["Cancelled"] == 0]

    # Eliminar nan
    df = df.dropna(
        subset=[
            "ArrDelay",
            "CRSDepTime",
            "Distance",
            "UniqueCarrier",
            "Origin",
            "Dest",
            "DayOfWeek",
        ]
    )

    return df

#Target
def create_target(df: pd.DataFrame, delay_threshold: int = 15) -> pd.DataFrame:

    df = df.copy()
    df["is_delayed"] = (df["ArrDelay"] > delay_threshold).astype(int)
    return df

#feature engineering final
def prepare_features(df: pd.DataFrame):

    df = df.copy()

    df["dep_hour"] = df["CRSDepTime"] // 100
    df = df.drop(columns=["CRSDepTime"])

    features = [
        "UniqueCarrier",
        "Origin",
        "Dest",
        "DayOfWeek",
        "dep_hour",
        "Distance",
    ]

    return df[features]

#Construccion del preprocessor
def build_preprocessor():
    categorical_cols = ["UniqueCarrier", "Origin", "Dest", "DayOfWeek"]
    numerical_cols = ["dep_hour", "Distance"]

    return ColumnTransformer(
        transformers=[
            ("cat", OneHotEncoder(handle_unknown="ignore"), categorical_cols),
            ("num", "passthrough", numerical_cols),
        ]
    )

#Pipeline completo
def build_dataset(df: pd.DataFrame):

    df = clean_data(df)
    y = create_target(df)
    X = prepare_features(df)
    preprocessor = build_preprocessor()

    return X, y, preprocessor