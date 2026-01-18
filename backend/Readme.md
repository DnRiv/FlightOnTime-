# ✈️ FlightOnTime - Predicción de Puntualidad o Retraso de Vuelos de Pasajeros

**FlightOnTime** es una solución integral para predecir la puntualidad de vuelos comerciales utilizando Inteligencia Artificial. El sistema permite a los usuarios consultar predicciones individuales o procesar lotes masivos de vuelos mediante archivos CSV, almacenando el historial de consultas en una base de datos relacional.

## 🚀 Arquitectura del Sistema

El proyecto sigue una arquitectura de microservicios desacoplada:

1.  **Backend Core (Spring Boot 3.3.6):**
    * Gestiona la API REST y la validación de negocio.
    * Valida datos maestros (Aerolíneas, Rutas, Aeropuertos) contra MySQL.
    * Orquesta la comunicación con el servicio de IA.
2.  **Servicio de Inteligencia Artificial (Python/FastAPI):**
    * Microservicio independiente que aloja el modelo de Machine Learning (`model.joblib`).
    * Recibe los datos del vuelo y retorna la probabilidad de retraso.
3.  **Base de Datos (MySQL):**
    * Persistencia de catálogos (Aerolíneas, Aeropuertos) y registro histórico de predicciones.

## 🛠️ Tecnologías

* **Java 17** (Spring Boot 3.3.6)
* **Maven** 4.0.0 (Gestión de dependencias)
* **MySQL 8** (Base de datos relacional)
* **WorkBench 8** (Administración de Base de datos)
* **JPA / Hibernate** (Persistencia de datos)
* **Python 3.12 + FastAPI** (Motor de IA)
* **JUnit 5 + Mockito** (Pruebas unitarias e integración)
* **HTML5 / CSS3 / JavaScript ES2024** (Frontend ligero integrado)

## 📋 Prerrequisitos

* JDK 17 instalado.
* MySQL Server corriendo en el puerto 3306.
* Python 3.12+ instalado.
* MySQL 8.0
* Conexión a internet (para descargar dependencias Maven/Pip).

## ⚙️ Configuración e Instalación

### 1. Base de Datos (MySQL)
Crea una base de datos llamada `flighton` y configura tu usuario/password en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/flighton?useSSL=false&serverTimezone=UTC
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
```

## 📋 Microservicio de IA (Python)

Navega a la carpeta de tu backend de Python (asegúrate de tener el modelo `model.joblib` presente):

```bash
# Crear entorno virtual
python -m venv .venv
source .venv/bin/activate  # En Windows: .venv\Scripts\activate

# Instalar dependencias
pip install fastapi uvicorn scikit-learn pandas joblib numpy

# Ejecutar el servicio (Debe correr en el puerto 8000)
uvicorn main:app --host 0.0.0.0 --port 8000
```

## 📋 Compilar proyecto Principal (Java)

En mi caso es ~/flight (donde se encuentra el archivo mvnw):

```bash
# Compilar y ejecutar
./mvnw spring-boot:run
```

## 🔌 Uso de la API

### Predicción Individual

**POST** `/predict`

```json
{
  "aerolinea": "AA",
  "origen": "MIA",
  "destino": "JFK",
  "fecha_partida": "2026-12-20T08:30:00",
  "distancia": 1099
}
```

## 🚀 Predicción por Lote

**POST** /predict/batch

	Body: form-data con key file (archivo .csv).

	Formato CSV: aerolinea,origen,destino,fecha_partida,distancia

## ✅Base de Datos

Para la creación de la Base de Datos **flighton** y sus tablas, utilizar los scripts **.sql** que se encuentran en la carpeta sql del proyecto. 

```plaintext
~/sql
├── aeropuertos.sql
├── aeropuertos_zonas.sql
├── estructura.txt
├── flighton_aerolineas.sql
└── rutas_validas.sql
```

### Importante:

Configura la conexión en src/main/resources/application.properties:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/flights_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña 
```

## 📂 Estructura del Proyecto

```plaintext
.
├── estructura.txt
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
├── proyecto.txt
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── hackathon
│   │   │           └── flights
│   │   │               ├── config
│   │   │               │   └── RestTemplateConfig.java
│   │   │               ├── controller
│   │   │               │   ├── MasterDataController.java
│   │   │               │   └── VueloController.java
│   │   │               ├── dto
│   │   │               │   ├── DsRequest.java
│   │   │               │   ├── PrediccionLoteResponse.java
│   │   │               │   ├── PrediccionResponse.java
│   │   │               │   └── VuelosRequest.java
│   │   │               ├── entity
│   │   │               │   ├── Aerolinea.java
│   │   │               │   ├── Aeropuerto.java
│   │   │               │   ├── AeropuertoZona.java
│   │   │               │   ├── RutaValida.java
│   │   │               │   └── Vuelos.java
│   │   │               ├── exception
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   └── ValidationException.java
│   │   │               ├── FlightsApplication.java
│   │   │               ├── repository
│   │   │               │   ├── AerolineaRepository.java
│   │   │               │   ├── AeropuertoRepository.java
│   │   │               │   ├── AeropuertoZonaRepository.java
│   │   │               │   ├── RutaValidaRepository.java
│   │   │               │   └── VuelosRepository.java
│   │   │               └── service
│   │   │                   └── FlightsService.java
│   │   └── resources
│   │       ├── aerolineas.csv
│   │       ├── aeropuertos_zonas_usa.csv
│   │       ├── application.properties
│   │       ├── destino_valido.csv
│   │       ├── lote.csv
│   │       ├── origen_valido.csv
│   │       ├── rutas_validas.csv
│   │       └── static
│   │           ├── css
│   │           │   └── style.css
│   │           ├── favicon.png
│   │           ├── index.html
│   │           ├── js
│   │           │   └── main.js
│   │           └── proyecto.txt
│   └── test
│       └── java
│           └── com
│               └── hackathon
│                   └── flights
│                       ├── FlightsApplicationTests.java
│                       └── service
│                           └── FlightsServiceTest.java
└── target
    ├── classes
    │   ├── aerolineas.csv
    │   ├── aeropuertos_zonas_usa.csv
    │   ├── application.properties
    │   ├── com
    │   │   └── hackathon
    │   │       └── flights
    │   │           ├── config
    │   │           │   └── RestTemplateConfig.class
    │   │           ├── controller
    │   │           │   ├── MasterDataController.class
    │   │           │   └── VueloController.class
    │   │           ├── dto
    │   │           │   ├── DsRequest.class
    │   │           │   ├── PrediccionLoteResponse.class
    │   │           │   ├── PrediccionResponse.class
    │   │           │   └── VuelosRequest.class
    │   │           ├── entity
    │   │           │   ├── Aerolinea.class
    │   │           │   ├── Aeropuerto.class
    │   │           │   ├── AeropuertoZona.class
    │   │           │   ├── RutaValida.class
    │   │           │   └── Vuelos.class
    │   │           ├── exception
    │   │           │   ├── GlobalExceptionHandler.class
    │   │           │   └── ValidationException.class
    │   │           ├── FlightsApplication.class
    │   │           ├── repository
    │   │           │   ├── AerolineaRepository.class
    │   │           │   ├── AeropuertoRepository.class
    │   │           │   ├── AeropuertoZonaRepository.class
    │   │           │   ├── RutaValidaRepository.class
    │   │           │   └── VuelosRepository.class
    │   │           └── service
    │   │               ├── FlightsService$1.class
    │   │               └── FlightsService.class
    │   ├── destino_valido.csv
    │   ├── lote.csv
    │   ├── origen_valido.csv
    │   ├── rutas_validas.csv
    │   └── static
    │       ├── css
    │       │   └── style.css
    │       ├── favicon.png
    │       ├── index.html
    │       ├── js
    │       │   └── main.js
    │       └── proyecto.txt
    ├── flights-0.0.1-SNAPSHOT.jar
    ├── flights-0.0.1-SNAPSHOT.jar.original
    ├── generated-sources
    │   └── annotations
    ├── generated-test-sources
    │   └── test-annotations
    ├── maven-archiver
    │   └── pom.properties
    ├── maven-status
    │   └── maven-compiler-plugin
    │       ├── compile
    │       │   └── default-compile
    │       │       ├── createdFiles.lst
    │       │       └── inputFiles.lst
    │       └── testCompile
    │           └── default-testCompile
    │               ├── createdFiles.lst
    │               └── inputFiles.lst
    └── test-classes
        └── com
            └── hackathon
                └── flights
                    ├── FlightsApplicationTests.class
                    └── service
                        └── FlightsServiceTest.class

```

##