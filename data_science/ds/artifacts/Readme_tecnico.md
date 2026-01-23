README Técnico - Proyecto FlightOnTime

 Objetivo

Desarrollar un modelo predictivo para identificar retrasos en vuelos comerciales utilizando un dataset con más de 300.000 registros. El entregable principal es un Notebook Jupyter/Colab que incluya exploración de datos, limpieza, creación de variables, entrenamiento de modelos, evaluación y exportación.

 Estructura del Notebook

1. Importación de librerías

pandas, numpy → manipulación de datos

matplotlib, seaborn → visualización

sklearn → modelado y métricas

joblib → exportación de modelos

2. Exploración y limpieza de datos (EDA)

Análisis de valores nulos y duplicados

Distribución de retrasos (dep_delay, arr_delay)

Gráficos por aerolínea, aeropuerto, hora del día

Eliminación de columnas que generan fuga de información (arr_delay, arr_time, sched_arr_time, tailnum, time_hour)

3. Creación de variables relevantes

Objetivo: delayed (binaria: retraso > 15 min)

Features derivadas:

Día de la semana (day_of_week)

Hora del día categórica (part_of_day)

Aerolínea (UniqueCarrier)

Aeropuerto origen/destino (origin, dest)

Distancia y duración del vuelo

4. Balanceo de clases

Uso de oversampling para equilibrar vuelos retrasados vs puntuales.

5. División Train/Test

Separación estratificada (70% entrenamiento, 30% prueba).

Validación cruzada (CV=5) para robustez.

6. Modelos entrenados

Logistic Regression → baseline interpretable

Random Forest → modelo robusto y flexible

Opcional: Gradient Boosting (XGBoost/LightGBM) para mejorar rendimiento

7. Evaluación de desempeño

Métricas: Accuracy, Precision, Recall, F1-score

Matriz de confusión

Comparación entre modelos

8. Exportación de modelos

Serialización con joblib.dump()

Archivos: modelo_rf.pkl, modelo_logreg.pkl

 Interpretación de métricas

Accuracy → proporción de predicciones correctas

Precision → confiabilidad al predecir retrasos

Recall → capacidad de detectar vuelos retrasados

F1-score → balance entre precisión y recall

 Recomendaciones

Usar Random Forest como modelo principal por su robustez.

Probar Gradient Boosting para mejorar resultados (en una segunda etapa)

Documentar cada paso con comentarios y visualizaciones.

Mantener un pipeline reproducible y versionado en GitHub.

 Entregables

Notebook Jupyter/Colab con todo el pipeline.

Modelos serializados (.pkl).

Documentación técnica (este README).

📜 Flujo del pipeline (ASCII)

[Dataset] → [EDA & Limpieza] → [Feature Engineering] → [Balanceo] → [Train/Test Split]
       → [Entrenamiento Modelos] → [Evaluación] → [Exportación]

👥 Equipo DS

Responsable de EDA y limpieza

Responsable de Feature Engineering

Responsable de Modelado y Evaluación

Responsable de Documentación y Entregables

===========================================================================================================


