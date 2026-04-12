# Technology Stack

**Analysis Date:** 2026-04-12

## Backend

- **Language:** Java 17
- **Framework:** Spring Boot 3.5.5 (parent POM: `spring-boot-starter-parent`)
- **Build Tool:** Maven (`backendOrderly/mvnw`, `backendOrderly/pom.xml`)
- **Server Port:** 8080 (configured in `backendOrderly/src/main/resources/application.yml`)
- **Key Dependencies:**
  - `spring-boot-starter-web` — REST API via `@RestController`
  - `spring-boot-starter-data-jpa` — Hibernate ORM / Spring Data repositories
  - `spring-boot-starter-security` — Spring Security (JWT-based stateless)
  - `spring-boot-starter-websocket` — Plain WebSocket (not STOMP)
  - `spring-boot-starter-validation` — Bean Validation (`jakarta.validation`)
  - `org.postgresql:postgresql` (runtime) — JDBC driver
  - `org.projectlombok:lombok` — `@Data`, `@RequiredArgsConstructor`, etc.
  - `io.jsonwebtoken:jjwt-api:0.11.5` + `jjwt-impl` + `jjwt-jackson` — JWT generation/validation
  - `com.fasterxml.jackson.core:jackson-databind` — JSON serialization
  - `spring-boot-starter-test` — JUnit 5 / Mockito testing

## Tablet Frontend (OrderlyTablet)

- **Language:** Kotlin 2.0.21
- **Framework:** Android (Jetpack Compose)
- **Build Tool:** Gradle with AGP 8.12.3 (`frontend/OrderlyTablet/app/build.gradle.kts`)
- **Min SDK:** 24 / **Target SDK:** 35 / **Compile SDK:** 35
- **JVM Target:** 17
- **Key Libraries:**
  - `androidx.compose.ui:ui:1.7.4` + `foundation:1.7.4` + `material3:1.3.0` — Compose UI
  - `androidx.activity:activity-compose:1.9.3` — Compose activity integration
  - `androidx.navigation:navigation-compose:2.8.3` — Compose navigation
  - `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6` — ViewModel + Compose
  - `com.squareup.retrofit2:retrofit:2.11.0` + `converter-gson:2.11.0` — REST HTTP client
  - `com.squareup.okhttp3:logging-interceptor:4.12.0` — HTTP logging
  - `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1` — Coroutines
  - `com.google.accompanist:accompanist-systemuicontroller:0.34.0` — System UI control
  - `androidx.compose.material:material-icons-extended:1.7.4` — Extended icon set
  - `androidx.recyclerview:recyclerview:1.3.2`, `androidx.cardview:cardview:1.0.0` — Legacy views (partial usage)
  - OkHttp WebSocket (via OkHttp3) — Used directly in `OrderWebSocketClient.kt` for real-time updates

## Phone Frontend (OrderlyPhone)

- **Language:** Kotlin 2.0.21
- **Framework:** Android (Jetpack Compose with Compose BOM)
- **Build Tool:** Gradle with AGP 8.13.2 (`frontend/OrderlyPhone/app/build.gradle.kts`)
- **Min SDK:** 24 / **Target SDK:** 36 / **Compile SDK:** 36
- **JVM Target:** 17
- **Key Libraries:**
  - `androidx.compose:compose-bom:2024.09.00` — Compose Bill of Materials
  - `androidx.compose.material3:material3:1.4.0` — Material 3 UI components
  - `androidx.compose.foundation:foundation:1.10.2` — Foundation layout
  - `androidx.navigation:navigation-compose:2.9.7` — Navigation
  - `com.squareup.retrofit2:retrofit:2.11.0` + `converter-gson:2.11.0` — REST HTTP client
  - `com.squareup.okhttp3:okhttp:4.12.0` — HTTP client with `AuthInterceptor`
  - `com.google.dagger:hilt-android:2.52` + `hilt-compiler:2.52` — Dependency injection (Hilt/Dagger)
  - `androidx.hilt:hilt-navigation-compose:1.2.0` — Hilt + Compose navigation integration
  - `androidx.datastore:datastore-preferences:1.1.1` — Persistent JWT token storage
  - `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6` + `lifecycle-runtime-compose:2.8.6`
  - `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1` — Coroutines
  - `androidx.compose.material:material-icons-extended` — Extended icons
  - `com.android.tools:desugar_jdk_libs:2.0.4` — Core library desugaring (Java 8+ APIs on older Android)

## PC Frontend (orderly)

- **Language:** Java 23
- **Framework:** JavaFX 21.0.2 (MVC pattern with FXML views, CSS styling)
- **Build Tool:** Maven (`frontend/orderly/pom.xml`), run via `mvn javafx:run`
- **Key Libraries:**
  - `org.openjfx:javafx-controls:21.0.2` — JavaFX controls
  - `org.openjfx:javafx-fxml:21.0.2` — FXML view loading
  - `org.openjfx:javafx-swing:21.0.2` — Swing interop
  - `com.fasterxml.jackson.core:jackson-databind:2.17.2` + `jackson-core` + `jackson-annotations` — JSON parsing
  - `com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2` — Java 8 date/time (LocalDate, LocalDateTime) support
  - `org.apache.pdfbox:pdfbox:2.0.29` — PDF receipt and report generation
  - `org.projectlombok:lombok:1.18.32`
- **HTTP Client:** Plain `java.net.HttpURLConnection` wrapped in `ApiClient.java` (`frontend/orderly/src/main/java/com/yebur/service/ApiClient.java`)
- **Base URL:** `http://localhost:8080` (hardcoded in `ApiClient.java`)

## Database

- **Type:** Relational
- **Technology:** PostgreSQL
- **Connection URL:** `jdbc:postgresql://localhost:5432/db_orderly` (configured in `backendOrderly/src/main/resources/application.yml`)
- **ORM / Access Layer:** Hibernate via Spring Data JPA; `ddl-auto: update`; dialect: `org.hibernate.dialect.PostgreSQLDialect`
- **Schema management:** Hibernate auto-updates schema on startup

## API Layer

- **Style:** REST (HTTP/JSON) — all controllers use `@RestController` + `@RequestMapping`
- **Serialization:** JSON via Jackson (`jackson-databind`)
- **Real-time:** Plain WebSocket (Spring `@EnableWebSocket`) at endpoint `/ws/overview/tablet`
- **Auth:** JWT (Bearer token) issued at `/auth/login`, validated via `JwtAuthFilter` on all non-public endpoints

---

*Stack analysis: 2026-04-12*
