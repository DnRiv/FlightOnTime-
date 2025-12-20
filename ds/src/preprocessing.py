import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder

#funcion de limpieza

def clean_data(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df = df[df["Cancelled"] == 0]
    return df


#Target
def create_target(
    df: pd.DataFrame, delay_threshold: int = 15) -> pd.Series:
    return (df["ArrDelay"] > delay_threshold).astype(int)

#feature engineering final
def prepare_features(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()

    df = df.dropna(
        subset=[
            "CRSDepTime",
            "Distance",
            "UniqueCarrier",
            "Origin",
            "Dest",
            "DayOfWeek",
        ]
    )

    df["dep_hour"] = df["CRSDepTime"] // 100

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

    # limpiar target
    df = df.dropna(subset=["ArrDelay"])

    y = create_target(df)

    X = prepare_features(df)

    # Alinear índices (MUY IMPORTANTE)
    X = X.loc[y.index]
    y = y.loc[X.index]

    preprocessor = build_preprocessor()

    return X, y, preprocessor

X = X.loc[y.index]
y = y.loc[X.index]

