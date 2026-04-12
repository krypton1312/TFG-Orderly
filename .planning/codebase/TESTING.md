# Testing Strategy

**Analysis Date:** 2026-04-12

---

## Backend Tests

**Framework:** JUnit 5 (Jupiter) + Mockito  
**Runner annotation:** `@ExtendWith(MockitoExtension.class)`  
**Test location:** `backendOrderly/src/test/java/com/yebur/backendorderly/`  
**Test reports:** `backendOrderly/target/surefire-reports/`

### Types of Tests

**Unit Tests:** Yes — service-layer logic tested with mocked dependencies.

**Integration Tests:** Yes — one Spring context load test (`BackendOrderlyApplicationTests`).

**Controller Tests:** No — no `@WebMvcTest` or `MockMvc` tests present.

### Test Classes

| Class | Location | Tests | Passes |
|---|---|---|---|
| `BackendOrderlyApplicationTests` | `backendOrderly/src/test/java/com/yebur/backendorderly/` | 1 | 1 |
| `EmployeeServiceUpdateTest` | `backendOrderly/src/test/java/com/yebur/backendorderly/employee/` | 2 | 2 |
| `OrderServiceTest` | `backendOrderly/src/test/java/com/yebur/backendorderly/order/` | 2 | 2 |

**Total: 5 tests, 5 passing, 0 failures** (per surefire reports).

### Test Coverage

Very limited. Covered areas:
- `EmployeeService.update()` — partial field update does not overwrite unchanged fields.
- `EmployeeService.create()` — default status is set; roles list is not null.
- `OrderService.updateOrder()` — no NPE when `orderDetails` is `null`.
- `OrderService.updateOrder()` — state remains `OPEN` when no paid details exist.
- Spring context loads without error.

Not covered:
- All controller endpoints (no MockMvc tests).
- `OrderDetailService` (complex service handling supplements, WebSocket notifications).
- `AuthController` / JWT flow.
- `CashOperationService`, `CashSessionService`, `ShiftRecordService`.
- Repository queries (`@EntityGraph`, custom finders).
- WebSocket handler behavior.
- Delete operations.
- Validation constraint failures.

### Notable Test Patterns

**Structure:** Given/When/Then expressed via inline comments in some tests:
```java
@Test
public void testUpdatePartialField_ShouldNotOverwriteOthers() {
    // Given
    Employee existingEmployee = new Employee();
    ...
    // When
    EmployeeResponse response = employeeService.update(employeeId, updateRequest);
    // Then
    assertEquals("Jane", response.getName());
    assertEquals("Doe", response.getLastname(), "Lastname should not have changed");
}
```

**Mock setup:**
```java
@Mock private EmployeeRepository employeeRepository;
@Mock private RoleRepository roleRepository;
@Mock private PasswordEncoder passwordEncoder;
@InjectMocks private EmployeeService employeeService;
```

**Assertion style:** JUnit 5 `Assertions.assertEquals()` with message strings for clarity. `assertThrows()` for exception testing.

**State verification through return value:** Service methods return DTOs; tests assert on the returned DTO fields rather than on repository mock call counts.

**Test naming convention:** `test{MethodName}_{ExpectedBehavior}` — e.g., `testUpdatePartialField_ShouldNotOverwriteOthers`, `testCreate_ShouldSetDefaultValues`.

**Run commands:**
```bash
# From backendOrderly/
mvn test                     # Run all tests
mvn surefire-report:report   # Generate HTML report
```

---

## Android Tests (Phone — `frontend/OrderlyPhone`)

**Unit test location:** `frontend/OrderlyPhone/app/src/test/java/com/example/orderlyphone/`  
**Instrumented test location:** `frontend/OrderlyPhone/app/src/androidTest/java/com/example/orderlyphone/`

**Unit tests:** Scaffold only — `ExampleUnitTest.kt` with a single `addition_isCorrect()` assertion:
```kotlin
@Test
fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
}
```

**Instrumented tests:** Scaffold only — `ExampleInstrumentedTest.kt` verifies package name:
```kotlin
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.orderlyphone", appContext.packageName)
    }
}
```

**Coverage:** Effectively zero — no domain logic, ViewModel logic, or UI behavior is tested.

**Frameworks declared:** JUnit 4 (`org.junit.Test`, `org.junit.Assert.*`), AndroidJUnit4.

**Run commands:**
```bash
# From frontend/OrderlyPhone/
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests (requires device/emulator)
```

---

## Android Tests (Tablet — `frontend/OrderlyTablet`)

**Unit test location:** `frontend/OrderlyTablet/app/src/test/java/com/example/orderlytablet/`  
**Instrumented test location:** `frontend/OrderlyTablet/app/src/androidTest/java/com/example/orderlytablet/`

**Unit tests:** Scaffold only — `ExampleUnitTest.kt` with `addition_isCorrect()`.

**Instrumented tests:** Scaffold only — `ExampleInstrumentedTest.kt` (not read, same pattern as Phone by structure).

**Coverage:** Effectively zero — `OrdersViewModel` (with WebSocket, Coroutines, StateFlow) and `OrderWebSocketClient` are not tested.

**Run commands:**
```bash
# From frontend/OrderlyTablet/
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests (requires device/emulator)
```

---

## PC Frontend Tests (JavaFX — `frontend/orderly`)

**Framework:** None detected.  
**Test directory:** Not present — no `src/test/java/` content found.  
**Coverage:** Zero.

---

## Overall Assessment

The project has a sparse testing foundation concentrated entirely in the backend.

**Strengths:**
- Backend unit tests are well-structured and use the correct Given/When/Then pattern.
- Mockito and JUnit 5 are correctly set up and tests pass cleanly.
- Test names are descriptive and express intent (`_ShouldNotOverwriteOthers`, `_ShouldSetDefaultValues`).
- Tests target real regression scenarios (NPE from `null` orderDetails, partial-update field preservation), not just happy-path calls.

**Gaps:**
- **No controller tests** — HTTP layer (routing, request parsing, error responses, HTTP status codes) is completely untested.
- **Android apps have zero real tests** — scaffold placeholders remain from project creation. `OrdersViewModel`, `LoginViewModel`, `AuthInterceptor`, `AuthRepository`, `OrderWebSocketClient` are all untested.
- **PC JavaFX frontend has no tests at all** — complex controller and service logic is untested.
- **Coverage is < 5% of codebase** overall by any measure.
- **No integration or E2E tests** — no tests against a real (or testcontainer) database.
- **No coverage enforcement** — no JaCoCo plugin or minimum threshold configured.

**Priority areas for new tests:**
1. Backend controllers via `@WebMvcTest` + `MockMvc` — validate endpoint contract.
2. `OrderDetailService` — the most complex and highest-risk service.
3. `LoginViewModel` and `AuthRepository` (Phone) — core authentication flow.
4. `OrdersViewModel` (Tablet) — WebSocket integration and state transitions.
