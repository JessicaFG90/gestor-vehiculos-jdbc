# 🚗 Gestor de Vehículos — Java JDBC + MySQL + MVC

Actividad evaluable de **Java** desarrollada en el módulo de **Programación** (1º DAM · Curso 2025/2026).

---

## 📋 Descripción

Aplicación de gestión de vehículos conectada a una base de datos **MySQL** mediante **JDBC**. Implementa el patrón de arquitectura **MVC (Modelo-Vista-Controlador)** con separación en capas (Repository, Service, View) y dos interfaces de usuario intercambiables: consola y escritorio con **Java Swing**.

La interfaz activa se selecciona desde el archivo `config.properties` sin necesidad de tocar el código.

---

## ⚙️ Funcionalidades

| Operación | Descripción |
|---|---|
| ➕ Crear | Añade un nuevo vehículo a la base de datos con validación de campos |
| 📋 Listar todos | Muestra todos los vehículos registrados |
| 🔍 Buscar por ID | Localiza un vehículo concreto por su identificador |
| 🔍 Buscar por marca | Filtra vehículos cuya marca contenga el texto buscado |
| ✏️ Modificar | Actualiza los datos de un vehículo existente |
| 🗑️ Eliminar | Borra un vehículo de la base de datos por su ID |

---

## 🏗️ Arquitectura MVC por capas

```
src/
├── controller/
│   └── Main.java               ← Punto de entrada. Lee config y lanza la UI elegida
├── model/
│   ├── configuration/
│   │   └── MiConfiguracion.java  ← Singleton. Lee config.properties (URL, user, pass, ui)
│   ├── domain/
│   │   └── Vehiculo.java         ← Entidad con sus atributos, getters, setters y toString
│   ├── repository/
│   │   └── VehiculoRepository.java ← Capa de datos: JDBC, SQL y mapeo de ResultSet a objetos
│   └── service/
│       └── VehiculoService.java  ← Capa de lógica: validaciones antes de llamar al Repository
└── view/
    ├── VehiculoConsoleUI.java    ← Interfaz de consola con menú interactivo
    └── VehiculoDesktopUI.java    ← Interfaz de escritorio con Java Swing (JTable, JDialog)
```

---

## 💡 Conceptos de Java aplicados

- **Patrón Singleton** en `MiConfiguracion`, `VehiculoService` y `VehiculoRepository` para garantizar una única instancia de cada clase
- **Patrón MVC** con separación clara entre modelo, vista y controlador
- **JDBC** para la conexión y operaciones sobre MySQL (`DriverManager`, `PreparedStatement`, `ResultSet`)
- **`PreparedStatement`** en todas las consultas para prevenir inyección SQL
- **`Optional<Vehiculo>`** en `findById` para gestionar el caso de que no se encuentre el registro
- **Validación** en la capa Service lanzando `IllegalArgumentException` con mensajes descriptivos
- **`config.properties`** + `Properties` + `BufferedReader` para externalizar la configuración
- **Java Swing** en la UI de escritorio: `JTable`, `JDialog`, `JComboBox`, `JTextField`, renderizador de botones en celda y enum `ModoFormulario` para alternar entre crear y editar

---

## 🛠️ Tecnologías

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Eclipse](https://img.shields.io/badge/Eclipse-FE7A16?style=for-the-badge&logo=Eclipse&logoColor=white)

---

## ▶️ Cómo ejecutarlo

### Requisitos previos

- **Java 17** o superior
- **MySQL** en ejecución local (puerto 3306 por defecto)
- Driver **JDBC de MySQL** (`mysql-connector-j`) en el classpath del proyecto

### 1. Crear la base de datos

Ejecuta el script incluido en `Scripts/bd.sql`:

```sql
CREATE DATABASE m0495_prg_evaluable06;
USE m0495_prg_evaluable06;

CREATE TABLE Vehiculo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    matricula VARCHAR(20) NOT NULL UNIQUE,
    consumoHomologado DOUBLE
);
```

### 2. Configurar la conexión

Edita `config.properties` en la raíz del proyecto con tus credenciales:

```properties
url=jdbc:mysql://localhost:3306/m0495_prg_evaluable06
user=tu_usuario
password=tu_contraseña
# Elige la interfaz: desktop | console
ui=desktop
```

> ⚠️ **No subas tu `config.properties` con credenciales reales a GitHub.** Añádelo al `.gitignore` y usa el archivo `config.properties.example` como plantilla.

### 3. Ejecutar desde Eclipse

1. Importa el proyecto: `File → Import → Existing Projects into Workspace`
2. Añade el conector JDBC al Build Path del proyecto
3. Ejecuta `Main.java` con botón derecho → `Run As → Java Application`

---

## 📁 Estructura del proyecto

```
Vehiculo.Jdbc.MySQL.MVC.ConsoleApp/
├── src/                        ← Código fuente Java
├── bin/                        ← Clases compiladas (ignorar en Git)
├── Scripts/
│   └── bd.sql                  ← Script de creación de la base de datos
├── config.properties           ← Configuración de conexión (no subir credenciales)
└── .classpath / .project       ← Archivos de configuración de Eclipse
```

---

## 🎓 Contexto académico

> Actividad Evaluable 006 · RA9 · Módulo **Programación** · 1º DAM · Curso 2025/2026
