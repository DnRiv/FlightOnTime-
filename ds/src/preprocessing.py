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
    df = df.copy()

    #Limpiar target
    df = df.dropna(subset=["ArrDelay"])
    df = create_target(df)

    #Separar X / y
    y = df["is_delayed"]

    columns_to_drop = [
        "Unnamed: 0", "is_delayed", "ArrDelay",
        "CancellationCode", "Diverted",
        "CarrierDelay", "WeatherDelay", "NASDelay",
        "SecurityDelay", "LateAircraftDelay",
        "TailNum", "FlightNum"
    ]

    X = df.drop(columns=[c for c in columns_to_drop if c in df.columns])

    #Columnas
    numerical_features = [
        "DepTime", "CRSDepTime", "ArrTime", "CRSArrTime",
        "ActualElapsedTime", "CRSElapsedTime",
        "AirTime", "DepDelay", "Distance", "TaxiIn", "TaxiOut"
    ]

    categorical_features = [
        "UniqueCarrier", "Origin", "Dest",
        "DayOfWeek", "Month", "DayofMonth", "Year"
    ]

    numeric_transformer = Pipeline(
        steps=[
            ("imputer", SimpleImputer(strategy="mean")),
            ("scaler", StandardScaler())
        ]
    )

    categorical_transformer = Pipeline(
        steps=[
            ("imputer", SimpleImputer(strategy="most_frequent")),
            ("onehot", OneHotEncoder(handle_unknown="ignore"))
        ]
    )

    preprocessor = ColumnTransformer(
        transformers=[
            ("num", numeric_transformer, numerical_features),
            ("cat", categorical_transformer, categorical_features),
        ],
        remainder="drop"
    )

    return X, y, preprocessor