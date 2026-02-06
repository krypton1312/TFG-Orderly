package com.yebur.backendorderly.employee;

import com.yebur.backendorderly.role.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceUpdateTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    public void testUpdatePartialField_ShouldNotOverwriteOthers() {
        // Given
        Long employeeId = 1L;
        Employee existingEmployee = new Employee();
        existingEmployee.setId(employeeId);
        existingEmployee.setName("John");
        existingEmployee.setLastname("Doe");
        existingEmployee.setEmail("john.doe@example.com");
        existingEmployee.setPhoneNumber("123456789");
        existingEmployee.setHireDate(LocalDate.now());
        existingEmployee.setStatus(EmployeeStatus.ACTIVE);
        existingEmployee.setRoles(new HashSet<>());

        EmployeeRequest updateRequest = new EmployeeRequest();
        updateRequest.setName("Jane"); // Only updating name
        // other fields are null in request

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        // When
        EmployeeResponse response = employeeService.update(employeeId, updateRequest);

        // Then
        assertEquals("Jane", response.getName());
        assertEquals("Doe", response.getLastname(), "Lastname should not have changed");
        assertEquals("john.doe@example.com", response.getEmail(), "Email should not have changed");
        assertEquals("123456789", response.getPhoneNumber(), "Phone number should not have changed");
    }

    @Test
    public void testCreate_ShouldSetDefaultValues() {
        // Given
        EmployeeRequest createRequest = new EmployeeRequest();
        createRequest.setName("Alice");
        createRequest.setLastname("Wonderland");
        createRequest.setEmail("alice@example.com");
        createRequest.setPassword("password123");
        createRequest.setHireDate(LocalDate.now());
        // status and roles are null

        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> {
            Employee e = i.getArgument(0);
            e.setId(2L);
            return e;
        });

        // When
        EmployeeResponse response = employeeService.create(createRequest);

        // Then
        assertEquals("Alice", response.getName());
        assertEquals("ACTIVO", response.getStatus()); // EmployeeStatus.ACTIVE.getSpanishName() is likely "ACTIVO"
        // Check if roles is an empty list (not null)
        assertEquals(0, response.getRoles().size());
    }
}
