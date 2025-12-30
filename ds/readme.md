# Data Science

Contiene notebooks y scripts para análisis de datos, ingeniería de features y entrenamiento del modelo.

## Archivos importantes
- `notebooks/01_eda.ipynb`: Exploración de datos
- `notebooks/02_feature_engineering.ipynb`: Ingeniería de features
- `notebooks/03_train_model.ipynb`: Entrenamiento y exportación del modelo

- `src/preprocessing.py`: Funciones de limpieza y preprocesamiento
- `src/train.py`: Script de entrenamiento
- `artifacts/`: Modelos exportados (`model.joblib`, `preprocessor.joblib`)

## Instrucciones
1. Instalar dependencias:
    pip install -r requirements.txt
2. Entrenar modelo desde `03_train_model.ipynb` o `src/train.py`.
3. Guardar artefactos en `artifacts/`.
