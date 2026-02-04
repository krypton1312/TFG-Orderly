package com.yebur.backendorderly.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    @Override
    @NonNull
    @EntityGraph(attributePaths = "roles")
    List<Employee> findAll();

    @Override
    @NonNull
    @EntityGraph(attributePaths = "roles")
    Optional<Employee> findById(@NonNull Long id);
}
