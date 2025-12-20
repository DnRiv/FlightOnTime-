import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder
from sklearn.pipeline import Pipeline
from sklearn.impute import SimpleImputer
from sklearn.preprocessing import StandardScaler



#Limpieza
def clean_data(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df = df[df["Cancelled"] == 0]
    df = df.dropna(subset=["ArrDelay"])
    return df



#Target
def create_target(df: pd.DataFrame, delay_threshold: int = 15) -> pd.DataFrame:
    df = df.copy()
    df["is_delayed"] = (df["ArrDelay"] > delay_threshold).astype(int)
    return df



#Features
def prepare_features(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()

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

#Preprocessor
def build_preprocessor():
    categorical_cols = ["UniqueCarrier", "Origin", "Dest", "DayOfWeek"]
    numerical_cols = ["dep_hour", "Distance"]

    preprocessor = ColumnTransformer(
        transformers=[
            ("cat", OneHotEncoder(handle_unknown="ignore"), categorical_cols),
            ("num", StandardScaler(), numerical_cols),
        ]
    )

    return preprocessor


#Dataset final
def build_dataset(df: pd.DataFrame):
    df = clean_data(df)
    df = create_target(df)

    X = prepare_features(df)
    y = df["is_delayed"]

    return X, y, build_preprocessor()