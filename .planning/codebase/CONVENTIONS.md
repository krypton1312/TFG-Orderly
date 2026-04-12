# Code Conventions

**Analysis Date:** 2026-04-12

---

## Naming

### Classes
- **PascalCase** throughout all components.
- Backend uses strict suffix patterns per layer:
  - `*Controller` — REST endpoint handler (`EmployeeController`, `OrderController`)
  - `*Service` — concrete service implementation (`EmployeeService`, `OrderService`)
  - `*ServiceInterface` — service contract (`EmployeeServiceInterface`, `OrderServiceInterface`)
  - `*Repository` — Spring Data JPA interface (`EmployeeRepository`, `ProductRepository`)
  - `*Request` — incoming DTO (`EmployeeRequest`, `OrderRequest`)
  - `*Response` — outgoing DTO (`EmployeeResponse`, `OrderResponse`)
  - `*Status` — enum for domain states (`EmployeeStatus`, `OrderStatus`)
- Domain entity classes use singular nouns without suffixes: `Employee`, `Order`, `Product`, `RestTable`
- Auth DTOs use Java `record` types with no suffix: `LoginRequest`, `AuthResponse`, `RegisterRequest`

### Methods
- **camelCase**, verb-first.
- Service methods follow a `findAll*DTO()`, `find*DTOById()`, `create()`, `update()`, `delete()` pattern.
- Private helpers use descriptive verbs: `updateEmployeeFields()`, `notifyOrderChanged()`, `reloadCategories()`.
- Android Kotlin ViewModels: `loadOrders()`, `refreshOrders()`, `login()`, `loadOrdersData()`.

### Variables
- **camelCase** everywhere.
- Boolean flags prefixed with `is` or `has`: `isOverviewMode`, `isTransferMode`, `isRefreshing`.
- Private backing state flows prefixed with `_`: `_state`, `_uiState`.
- Cached page/index state uses `current*` prefix in PC frontend: `currentCategoryPage`, `currentProductPage`.

### Packages
- Backend: `com.yebur.backendorderly.{domain}` — flat per-domain package (all files for a domain in one package).
- PC frontend: `com.yebur.{layer}` — separate top-level packages: `controller`, `service`, `model`, `ui`, `app`.
- Android Phone: `com.example.orderlyphone.{layer}.{domain}` — layered: `data.remote`, `data.local`, `data.repository`, `domain.model.request`, `domain.model.response`, `ui.screen.{screen}`, `di`.
- Android Tablet: `com.example.orderlytablet.{layer}` — simpler: `services`, `response`, `ui`, `ui.screens`.

### Database Tables
- Plural, lowercase: `employees`, `orders`.
- Join table names use underscore: `employee_rol`.
- FK column names use `id_` prefix: `id_employee`, `id_role`, `id_restable`.

---

## Code Style

**Formatting:** No automated formatter config detected (no `.editorconfig`, `.prettierrc`, or checkstyle XML). Indentation is 4 spaces in Java, 4 spaces in Kotlin.

**Line length:** No enforced limit observed. Long lines exist (e.g., multi-condition if in `EmployeeService`).

**Comments style:**
- Mixed-language comments: Russian, Spanish, and English all appear in the same files. Russian dominates in infrastructure/Websocket code; English/Spanish in business logic.
- Inline emoji used in comments to highlight intent: `// 🔹`, `// 🔑`, `// ✅`, `// ❌`.
- `// Given / // When / // Then` structure used in some unit tests.
- Block JSDoc is absent — no Javadoc on public methods.

---

## Backend Patterns

### Layer Separation
Strict three-layer architecture:

```
Controller → ServiceInterface → Service → Repository
```

- Controllers depend only on the interface, not the concrete service (in most modules).
- **Exception:** `OrderController` injects `OrderService` and `OrderDetailService` directly (concrete classes, not interfaces). Prefer interface injection as done in `EmployeeController`.

### DTO Usage
- **Request DTOs** are plain Lombok `@Data` classes with validation annotations from Jakarta (`@NotBlank`, `@Email`, `@NotNull`). Example: `EmployeeRequest`.
- **Response DTOs** are also Lombok `@Data` classes. Each has a static factory method `mapToResponse(Entity entity)` that maps from the JPA entity to the DTO. Example:
  ```java
  // EmployeeResponse.java
  public static EmployeeResponse mapToResponse(Employee employee) {
      return new EmployeeResponse(
          employee.getId(),
          employee.getName(),
          ...
      );
  }
  ```
- Auth DTOs (in `auth/dto/`) use Java `record` types instead of Lombok: `LoginRequest`, `AuthResponse`, `RegisterRequest`.

### Entity Conventions
All JPA entities follow the same structure pattern:
```java
@Entity
@Table(name = "plural_lowercase")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class EntityName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false)
    @ToString.Include
    private SomeType fieldName;
    // ...
}
```
- Enums with Spanish display names are used for status fields (e.g., `EmployeeStatus.ACTIVE` returns `"ACTIVO"` via `getSpanishName()`).
- Lazy fetch for relationships (`FetchType.LAZY`); eager loading via `@EntityGraph` in repository methods.

### Exception Handling
- Service layer throws `IllegalArgumentException` for business rule violations (not found, duplicate email).
- Service layer throws `RuntimeException` for infrastructure errors (table not found).
- Controllers catch exceptions in try/catch blocks and return a `Map<String, String>` body with an `"error"` key and the appropriate HTTP status:
  ```java
  Map<String, String> error = new HashMap<>();
  error.put("error", e.getMessage());
  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  ```
- Validation errors from `@Valid` use a shared `validation(BindingResult)` helper in each controller.
- No global `@ControllerAdvice` / `@ExceptionHandler` — error handling is repeated per controller.

### Response Wrapping
- Success: typed `ResponseEntity<List<EmployeeResponse>>` or `ResponseEntity<EmployeeResponse>`.
- `ResponseEntity<?>` wildcard used when method may return different body types (success DTO vs. error map).
- No custom envelope wrapper (no `ApiResponse<T>` wrapper class).

### Transaction Management
- `@Transactional(readOnly = true)` on all read operations.
- `@Transactional` (read-write) on create/update/delete operations.
- Declared on service implementation methods via `@Override`.

### Logging
- No logging framework (no SLF4J/Logback `@Slf4j`). Debugging via `System.out.println` in some files:
  - `backendOrderly/src/main/java/com/yebur/backendorderly/cashoperations/CashOperationService.java`
  - `backendOrderly/src/main/java/com/yebur/backendorderly/websocket/OrdersTabletWebSocketHandler.java`

---

## Android Patterns (Phone & Tablet)

### UI Pattern — MVVM
Both apps use MVVM with Jetpack Compose:
- `ViewModel` holds state and business logic.
- Composable screens collect state via `collectAsState()` and render based on it.
- UI state modeled as sealed classes per screen, e.g.:
  ```kotlin
  // LoginState.kt
  sealed class LoginState {
      data object Idle : LoginState()
      data object Loading : LoginState()
      data object Success : LoginState()
      data class Error(val message: String) : LoginState()
  }
  ```

### State Management
- Private `MutableStateFlow` with underscore prefix; public `StateFlow` without:
  ```kotlin
  private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
  val state: StateFlow<LoginState> = _state
  ```

### Async Pattern
- Kotlin Coroutines throughout. All ViewModel operations use `viewModelScope.launch { }`.
- Retrofit interfaces use `suspend fun`.
- `try/catch` inside coroutine for error handling, updating state to `Error(e.message)`.

### Dependency Injection
- **Phone (`OrderlyPhone`)**: Hilt — `@HiltViewModel`, `@Inject constructor`, `@Module`, `@InstallIn(SingletonComponent::class)`, `@Provides @Singleton`. DI module in `di/AppModule.kt`.
- **Tablet (`OrderlyTablet`)**: No DI. ViewModels created via `viewModel()` factory (Compose). Retrofit client is a singleton object `RetrofitClient` accessed directly.

### Navigation
- Compose Navigation with string route names: `"login"`, `"home"`, `"orders"`, etc.
- `NavHost` / `composable()` blocks in `AppNav.kt` (Phone).
- Tablet has single screen — no navigation.

### Data Layer (Phone)
- Remote API interfaces via Retrofit in `data/remote/`: `AuthApi`, `ProductsApi`, `OverviewApi`, `OrderDetailApi`, `EmployeeApi`, `CategoryApi`.
- Local persistence via DataStore in `data/local/`: `TokenStore` (JWT token), `CashSessionStore`.
- `AuthInterceptor` adds `Authorization: Bearer <token>` header to all non-auth requests.
- Adapters for `LocalDate` and `LocalDateTime` serialization in `data/remote/adapter/`.

### Logging (Android)
- `android.util.Log.d(tag, message)` used in ViewModels for debug output with descriptive tags: `"CheckDraft"`, `"CheckOverviewOrders"`.

---

## PC Frontend Patterns (JavaFX)

**Framework:** JavaFX with FXML view files. Entry point: `App.java` extending `javafx.application.Application`.

**Pattern:** FXML Controller pattern (not formal MVC). Controllers are FXML-annotated classes with `@FXML` fields and an `initialize()` method.

**Services:** Static utility classes with static methods per HTTP verb:
```java
// OrderService.java — example
public static List<OrderResponse> getAllOrders() throws Exception { ... }
public static OrderResponse createOrder(OrderRequest order) throws Exception { ... }
```

**HTTP client:** Custom `ApiClient` wrapping raw `HttpURLConnection`. No OkHttp/Retrofit. Supports GET, POST, PUT, DELETE methods. Throws `ApiException` for non-2xx responses.

**JSON serialization:** Jackson `ObjectMapper` configured manually in each service class (no shared bean).

**State management:** Manual field state in controllers (`currentOrder`, `selectedTable`, `currentCategoryPage`). No reactive state.

**No dependency injection.** Services are called statically. No lifecycle management.

---

## Common Anti-patterns

1. **Authentication disabled:** `SecurityConfig.java` has `anyRequest().permitAll()` — all backend endpoints are publicly accessible without a token.
2. **Hardcoded URLs:** Tablet WebSocket URL is hardcoded as `ws://192.168.1.136:8080/ws/overview/tablet` in `OrdersViewModel.kt`. Phone base URL is hardcoded as `http://10.0.2.2:8080/` in `di/AppModule.kt`.
3. **`System.out.println` in production code:** Present in `CashOperationService.java` and `OrdersTabletWebSocketHandler.java` — no structured logging.
4. **Mixed comment languages:** Russian, Spanish, and English comments coexist in the same files, reducing readability.
5. **Inconsistent DI in controllers:** `EmployeeController` correctly injects via `EmployeeServiceInterface`; `OrderController` injects concrete `OrderService` class directly.
6. **No global exception handler:** Each controller duplicates try/catch and error map construction. A `@ControllerAdvice` class would centralize this.
7. **No Javadoc/KDoc:** Public API surface (controllers, service interfaces) has no documentation.
8. **`OrderDetailService` mixed constructor injection pattern:** Uses manual constructor while most services use `@RequiredArgsConstructor` Lombok injection.
