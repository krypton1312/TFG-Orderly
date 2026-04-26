<div align="center">

# 🍽️ Orderly

**Sistema de Gestión de Restaurante y Punto de Venta (POS)**

[![Java](https://img.shields.io/badge/Java-17%2F23-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-0078D4?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![Android](https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-View_Only-red?style=for-the-badge)](./LICENSE)

*Trabajo de Fin de Grado · Aplicación multiplataforma de gestión integral para restaurantes*

</div>

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Arquitectura](#-arquitectura)
- [Stack Tecnológico](#-stack-tecnológico)
- [Funcionalidades](#-funcionalidades)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Requisitos Previos](#-requisitos-previos)
- [Configuración y Arranque](#-configuración-y-arranque)
- [Variables de Entorno](#-variables-de-entorno)
- [API REST](#-api-rest)
- [WebSocket](#-websocket)
- [Licencia](#-licencia)

---

## 📖 Descripción

**Orderly** es una solución completa de gestión de restaurante desarrollada como Trabajo de Fin de Grado. Permite gestionar pedidos, empleados, turnos, caja y menú desde múltiples dispositivos de forma simultánea y en tiempo real.

El sistema está compuesto por **cuatro módulos** que trabajan conjuntamente:

| Módulo | Plataforma | Rol |
|--------|------------|-----|
| **Backend API** | Spring Boot | Servidor central, lógica de negocio, base de datos |
| **Orderly PC** | JavaFX (Desktop) | Terminal POS principal — caja y gestión |
| **Orderly Phone** | Android (Kotlin) | Interfaz de camareros — toma de pedidos móvil |
| **Orderly Tablet** | Android (Kotlin) | Pantalla de cocina — visualización de pedidos en tiempo real |

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENTES                                │
├──────────────┬──────────────────┬───────────────────────────────┤
│  Orderly PC  │  Orderly Phone   │       Orderly Tablet          │
│  (JavaFX)    │  (Android)       │       (Android)               │
│  Terminal POS│  Camareros       │       Cocina / KDS            │
└──────┬───────┴────────┬─────────┴──────────────┬────────────────┘
       │                │    REST API / JWT        │ WebSocket
       └────────────────┴──────────────────────────┘
                                │
                    ┌───────────▼────────────┐
                    │    Backend Orderly      │
                    │   Spring Boot 3.5.5     │
                    │      Puerto 8080        │
                    └───────────┬────────────┘
                                │ JPA/Hibernate
                    ┌───────────▼────────────┐
                    │      PostgreSQL         │
                    │      db_orderly         │
                    └────────────────────────┘
```

Toda la comunicación entre clientes y servidor se realiza mediante **REST API con autenticación JWT**. La tablet de cocina además recibe actualizaciones en tiempo real mediante **WebSocket**.

---

## 🛠️ Stack Tecnológico

### Backend
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 17 | Lenguaje del servidor |
| Spring Boot | 3.5.5 | Framework principal |
| Spring Security | — | Autenticación y autorización |
| Spring WebSocket | — | Actualizaciones en tiempo real |
| Spring Data JPA | — | Acceso a base de datos |
| PostgreSQL | Latest | Base de datos relacional |
| JWT (JJWT) | 0.11.5 | Tokens de autenticación |
| Lombok | — | Reducción de código repetitivo |
| Maven | — | Gestión de dependencias |

### Frontend PC
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 23 | Lenguaje de la aplicación |
| JavaFX | 21.0.2 | Interfaz gráfica de escritorio |
| FXML | — | Definición de vistas |
| Apache PDFBox | 2.0.29 | Generación de recibos PDF |
| Jackson | 2.17.2 | Comunicación con la API |
| Lombok | 1.18.32 | Reducción de código repetitivo |
| Maven | — | Gestión de dependencias |

### Frontend Android (Phone & Tablet)
| Tecnología | Versión | Uso |
|------------|---------|-----|
| Kotlin | — | Lenguaje de las apps |
| Jetpack Compose | 1.7.4 | Interfaz declarativa |
| Material 3 | — | Sistema de diseño |
| Retrofit | 2.11.0 | Cliente HTTP para la API |
| Kotlin Coroutines | 1.8.1 | Programación asíncrona |
| Navigation Compose | 2.8.3 | Navegación entre pantallas |
| Gradle | — | Gestión de dependencias |
| Android minSdk | 24 (7.0) | Compatibilidad mínima |
| Android targetSdk | 35–36 | SDK objetivo |

---

## ✨ Funcionalidades

### 🖥️ Terminal POS (PC)
- **Inicio de sesión** con autenticación JWT
- **Pantalla POS principal** — selección de productos por categorías, carrito de pedido
- **Gestión de suplementos** — extras y modificadores de productos
- **Pago parcial / split payment** — dividir cuenta entre varios clientes
- **Generación e impresión de recibos** en PDF
- **Apertura y cierre de turno** con resumen de caja
- **Arqueo de caja** — recuento físico de efectivo
- **Libro diario** — registro de operaciones de caja
- **Informe de cierre de turno**
- **Panel de gestión de datos** — productos, categorías, empleados, mesas

### 📱 App Camarero (Phone)
- Toma de pedidos desde el móvil
- Selección de mesa, productos y suplementos
- Actualización del estado de pedidos
- Comunicación en tiempo real con cocina y caja

### 📟 Pantalla Cocina (Tablet)
- Visualización de pedidos entrantes en **tiempo real (WebSocket)**
- Gestión del estado de cada pedido (pendiente → en preparación → listo)
- Interfaz optimizada para uso en cocina con pantalla táctil

### 🔐 Seguridad y Empleados
- Autenticación JWT con tokens de acceso (60 min) y refresh (30 días)
- Control de acceso basado en **roles**
- Gestión de empleados y sus turnos
- Historial de turnos y auditoría

### 💰 Gestión de Caja
- Sesiones de caja diarias con fecha contable
- Operaciones de apertura/cierre de caja
- Efectivo total, entradas y salidas
- Arqueo con diferencia entre caja esperada y real

---

## 📁 Estructura del Proyecto

```
TFG-Orderly/
│
├── backendOrderly/                  # API REST Spring Boot
│   └── src/main/java/com/yebur/backendorderly/
│       ├── auth/                    # Login, registro, JWT
│       ├── security/                # Configuración de seguridad
│       ├── config/                  # CORS, WebSocket config
│       ├── order/                   # Gestión de pedidos
│       ├── orderdetail/             # Líneas de pedido
│       ├── product/                 # Productos del menú
│       ├── category/                # Categorías de productos
│       ├── supplements/             # Suplementos / extras
│       ├── resttable/               # Mesas del restaurante
│       ├── client/                  # Clientes
│       ├── employee/                # Empleados
│       ├── role/                    # Roles y permisos
│       ├── cashsessions/            # Sesiones de caja
│       ├── cashcount/               # Arqueo de caja
│       ├── cashoperations/          # Operaciones de caja
│       ├── shiftrecord/             # Turnos de empleados
│       ├── shiftrecordstory/        # Historial de turnos
│       ├── overview/                # Datos de dashboard
│       └── websocket/               # Notificaciones tiempo real
│
├── frontend/
│   │
│   ├── orderly/                     # App de escritorio JavaFX
│   │   └── src/main/java/com/yebur/
│   │       ├── app/                 # Punto de entrada
│   │       ├── controller/          # Controladores FXML
│   │       ├── service/             # Clientes de API
│   │       ├── model/               # DTOs y modelos
│   │       └── ui/                  # Componentes UI personalizados
│   │
│   ├── OrderlyPhone/                # App Android para camareros
│   │   └── app/src/main/java/
│   │       └── com/example/orderlyphone/
│   │
│   └── OrderlyTablet/               # App Android para cocina
│       └── app/src/main/java/
│           └── com/example/orderlytablet/
│
├── diseño/                          # Diagramas y diseños
│   ├── Casos de uso.drawio
│   ├── Diagrama.drawio
│   └── splitpayment.drawio
│
└── LICENSE
```

---

## 📦 Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- **JDK 17+** (para el backend) y **JDK 23** (para el frontend PC)
- **PostgreSQL** con una base de datos llamada `db_orderly`
- **Maven** (o usar los wrappers incluidos `mvnw`)
- **Android Studio** + SDK Android 35/36 (para las apps móviles)
- **Android Emulator** con AVD configurado (o dispositivo físico)

---

## ⚙️ Configuración y Arranque

### 1. Base de Datos

Crea la base de datos en PostgreSQL:
```sql
CREATE DATABASE db_orderly;
```

### 2. Variables de Entorno

Crea un archivo `.env` en la carpeta `backendOrderly/`:
```env
DB_PASSWORD=tu_contraseña_postgres
JWT_SECRET=tu_clave_secreta_jwt_muy_larga
```

Para las apps Android, configura `local.properties` en cada proyecto:
```properties
SERVER_HOST=192.168.x.x   # IP de tu máquina en la red local
```

### 3. Arrancar el Backend

```powershell
cd backendOrderly
./mvnw spring-boot:run
```
El servidor arrancará en `http://localhost:8080`

### 4. Arrancar la App de Escritorio (PC)

```powershell
cd frontend/orderly
mvn javafx:run
```

### 5. Arrancar las Apps Android

```powershell
# Phone
cd frontend/OrderlyPhone
./gradlew assembleDebug

# Tablet
cd frontend/OrderlyTablet
./gradlew assembleDebug
```

O usa las **tareas de VS Code** incluidas:
- `Backend: Start`
- `PC: Start`
- `Phone: Build & Launch`
- `Tablet: Build & Launch`
- `Launch All` — lanza todo en paralelo

---

## 🔑 Variables de Entorno

| Variable | Descripción | Requerida |
|----------|-------------|-----------|
| `DB_PASSWORD` | Contraseña de PostgreSQL | ✅ Backend |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT | ✅ Backend |
| `SERVER_HOST` | IP del servidor backend | ✅ Android |

---

## 🌐 API REST

El backend expone los siguientes endpoints en `http://localhost:8080`:

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/auth/login` | POST | Inicio de sesión |
| `/auth/register` | POST | Registro de usuario |
| `/auth/refresh` | POST | Renovar token JWT |
| `/orders` | GET / POST | Listar / crear pedidos |
| `/orders/{id}` | GET / PUT / DELETE | Gestión de pedido |
| `/orderdetail` | GET / POST / PUT / DELETE | Líneas de pedido |
| `/products` | GET / POST / PUT / DELETE | Productos del menú |
| `/categories` | GET / POST / PUT / DELETE | Categorías |
| `/supplements` | GET / POST / PUT / DELETE | Suplementos |
| `/tables` | GET / POST / PUT / DELETE | Mesas |
| `/employees` | GET / POST / PUT / DELETE | Empleados |
| `/cashSession` | GET / POST / PUT / DELETE | Sesiones de caja |
| `/cashCount` | GET / POST / PUT | Arqueo de caja |
| `/cashOperations` | GET / POST / PUT | Operaciones de caja |
| `/shiftRecord` | GET / POST / PUT | Turnos |
| `/roles` | GET / POST | Roles |
| `/overview` | GET | Datos del dashboard |

> Todos los endpoints (excepto `/auth`) requieren cabecera `Authorization: Bearer <token>`

---

## 🔌 WebSocket

La tablet de cocina se conecta mediante WebSocket para recibir actualizaciones de pedidos en tiempo real:

```
ws://[servidor]:8080/orders-tablet
```

Cada vez que un pedido es creado o actualizado desde el POS o la app de camarero, la cocina recibe la notificación instantáneamente.

---

## 📄 Licencia

Este proyecto está protegido bajo una **licencia de solo visualización**. Consulta el archivo [LICENSE](./LICENSE) para más detalles.

El código fuente se publica únicamente con fines de referencia y evaluación académica. Queda prohibido su uso, copia, modificación o distribución sin autorización expresa del autor.

---

<div align="center">

*Desarrollado como Trabajo de Fin de Grado*

</div>
