
#  1. Configuración actual (Spring Boot):

![[Captura desde 2025-12-20 11-43-08.png]]

| Campo             | Valor                              |
| ----------------- | ---------------------------------- |
| **Project**       | Maven                              |
| **Language**      | Java                               |
| **Spring Boot**   | 3.5.9                              |
| **Group**         | `com.hackathon`                    |
| **Artifact**      | `flights`                          |
| **Name**          | `flights`                          |
| **Description**   | “Predicción de Retrasos de Vuelos” |
| **Package name**  | `com.hackathon.flights`            |
| **Packaging**     | Jar                                |
| **Configuration** | Properties                         |
| **Java**          | 17                                 |

---

### 🔧 Dependencias seleccionadas:

| Dependencia         | ¿Para qué sirve?                                                      |
| ------------------- | --------------------------------------------------------------------- |
| **Spring Web**      | Para crear APIs REST (`@RestController`, `@PostMapping`)              |
| **Spring Data JPA** | Para conectar con MySQL y guardar historial (`@Entity`, repositorios) |
| **MySQL Driver**    | Conector JDBC para MySQL → permite que Spring se comunique con tu BD. |
| **Validation**      | Validación de entrada (`@NotBlank`, `@Future`, etc.)                  |


# 2. Pasos a seguir luego de crear el proyecto en Spring Boot
## Maven

Con IntelliJ llamar al proyecto y luego con Maven presionar "Reload All Maven Projects"

## Iniciamos MySQL local

sudo systemctl start mysql

## Inicializamos Workbench

## Crear la BD flighton

```sql
CREATE DATABASE flighton;
```

# 3. Crear archivo application.properties

**Lo que decide  dónde se crea es la configuración de la conexión es en `application.properties`.**

El archivo `application.properties` en un proyecto Java  sirve para **configurar la aplicación de forma externa al código**, almacenando ajustes clave-valor como credenciales de bases de datos, URLs, puertos del servidor o niveles de logging, permitiendo cambiar el comportamiento del proyecto sin recompilarlo, facilitando la portabilidad y adaptabilidad a diferentes entornos (desarrollo, producción).

En el proyecto ir a:  `src/main/resources/`

Crear si no existe `application.properties`.

```java
# Nombre de la app
spring.application.name=FlightOnTime

# Conexión a MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/qwen?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=cecz_777
spring.datasource.password=TimPam1993
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

# 4. Crear nuestro archivo @Entity Vuelos.java

El cual crea la BD en el servidor para ir creando la oportunidad de realizar consultas y estadísticas.

# 5. Se creo el repositorio VueloRepository.java

La interfaz que actúa como una capa de abstracción para interactuar directamente con la base de datos, permitiendo operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sin escribir SQL, gracias a Spring Data JPA, que genera la implementación automáticamente al extender interfaces como JpaRepository o `CrudRepository`, simplificando enormemente el acceso y manejo de datos en tu aplicación. En otras palabras crea la tablas que nos van ayudar a guardar las solicitudes realizadas por el cliente y los resultados de dichas solicitudes coma para realizar estadísticas y evaluaciones posteriormente.