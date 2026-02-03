package com.yebur.backendorderly.employee;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);
}
