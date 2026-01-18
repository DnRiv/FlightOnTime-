# ✈️ Predicción de Retrasos en Vuelos (Flight Delay Prediction)

Este proyecto contiene el flujo de trabajo completo para entrenar un modelo de Machine Learning capaz de predecir si un vuelo llegará a tiempo o sufrirá un retraso.

El código se encuentra en el notebook `03_train_model.ipynb` y abarca desde la carga de datos hasta la exportación del modelo final.

---

## 📋 Descripción del Proyecto

El objetivo es construir una herramienta predictiva que, basada en datos históricos de 2008, pueda anticipar retrasos futuros. El modelo actúa como un "clasificador binario":
* **0:** El vuelo llegará a tiempo.
* **1:** El vuelo se retrasará.

### 🛠️ Flujo de Trabajo (La "Receta")

El proceso sigue una estructura de tubería (*Pipeline*) secuencial:

1.  **Ingredientes (Carga de Datos):** Se obtienen los datos crudos desde un repositorio remoto (`Sample_DelayedFlights.csv`).
2.  **Preparación (Preprocesamiento):** Los datos se limpian y transforman para que la máquina los entienda:
    * **Datos Categóricos (Texto):** Variables como Aerolínea, Origen y Destino se convierten a números usando *OneHotEncoder*.
    * **Datos Numéricos:** Variables como Distancia y Hora se ajustan a una escala común usando *StandardScaler*.
3.  **Entrenamiento (Modelado):** Se utiliza un algoritmo de **Regresión Logística**. La máquina estudia el 80% de los datos para aprender patrones de retraso.
4.  **Evaluación:** Se pone a prueba el modelo con el 20% restante de los datos.
5.  **Empaquetado:** El modelo entrenado se guarda en un archivo `.joblib` para su uso posterior.

---

## 📊 Características Utilizadas

El modelo toma decisiones basándose en las siguientes columnas:

| Tipo de Dato | Variables (Features) | Tratamiento |
| :--- | :--- | :--- |
| **Categórico** | `UniqueCarrier` (Aerolínea), `Origin`, `Dest`, `DayOfWeek` | `OneHotEncoder` (Se crean columnas binarias por cada categoría) |
| **Numérico** | `dep_hour` (Hora de salida), `Distance` (Distancia) | `StandardScaler` (Estandarización de valores) |

---

## 📈 Rendimiento del Modelo

El modelo tiene una personalidad **"precavida y pesimista"**. Es excelente detectando problemas, aunque a veces genera falsas alarmas.

### Tabla de Resultados Simplificada

| Clase | Precisión (Confianza) | Recall (Sensibilidad) | Interpretación |
| :--- | :---: | :---: | :--- |
| **0 (A tiempo)** | 59% | 32% | Al modelo le cuesta confirmar cuándo todo saldrá bien. |
| **1 (Retrasado)** | **68%** | **86%** | **El modelo detecta el 86% de los retrasos reales.** |

> **Conclusión:** Si el modelo dice que tu vuelo se retrasará, hay una alta probabilidad de que sea cierto, y muy rara vez se le escapa un retraso real sin detectar.

---
