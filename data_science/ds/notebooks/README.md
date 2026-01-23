
# ✈️ Flight Delay Prediction 

Sistema de predicción de retrasos aéreos basado en Machine Learning, diseñado para anticipar si un vuelo llegará con más de 15 minutos de retraso utilizando únicamente información disponible antes del despegue.

---

## 📌 Objetivo del proyecto

Construir un modelo de clasificación binaria que estime la probabilidad de retraso operacional y permita tomar decisiones preventivas.

```
is_delayed = 1  si ArrDelay > 15 minutos
is_delayed = 0  en caso contrario
```

---

## 📂 Estructura del proyecto
```

01_eda.ipynb                 # Análisis exploratorio
02_feature_engineering.ipynb # Validación de variables y pipeline
03_train_model.ipynb         # Entrenamiento y exportación del modelo
```

## 🔍 Dataset

Fuente: Sample_DelayedFlights.csv (GitHub).  
Se filtran vuelos cancelados y se utilizan sólo variables conocidas antes del vuelo.

---

## 🎯 Variable Objetivo

La variable is_delayed convierte el problema en clasificación binaria supervisada.

---

## 🧠 Características utilizadas (MVP)

| Tipo        | Variables |
|------------|-----------|
| Categóricas | UniqueCarrier, Origin, Dest, DayOfWeek |
| Numéricas   | CRSDepTime → dep_hour, Distance |

Feature derivada:

```
dep_hour = CRSDepTime // 100
```

---

## ⚙️ Pipeline de Preprocesamiento

ColumnTransformer:

| Tipo | Transformación |
|------|----------------|
| Categóricas | OneHotEncoder |
| Numéricas   | Passthrough |

---

## 🤖 Modelo – Regresión Logística

Pipeline completo:

- Preprocesamiento
- LogisticRegression(max_iter=1000)

---
## ¿Por qué utilizar este Modelo?
## 📌 Fundamentación del modelo

### Clasificación binaria
El modelo estima directamente:

P(is_delayed = 1 | X)

### Interpretabilidad
Permite analizar impacto de aerolíneas, aeropuertos, horarios y distancia.

### Compatibilidad con OneHot
Excelente desempeño con matrices dispersas de alta dimensionalidad.

### Modelo baseline productivo
- Rápido
- Estable
- Bajo overfitting
- Fácil despliegue en FastAPI

### Predicciones probabilísticas
predict_proba permite implementar umbrales de riesgo.

### Escalabilidad futura
El pipeline permite sustituir fácilmente el clasificador por modelos más complejos.

---

## 📈 Evaluación

Se utiliza clasificación estratificada 80/20 y classification_report.

---

## 💾 Exportación

El pipeline entrenado se guarda como:

ds/artifacts/model.joblib

---

## 🏁 Conclusión

La regresión logística es el mejor modelo baseline para iniciar este sistema productivo por su equilibrio entre rendimiento, interpretabilidad y mantenibilidad.
