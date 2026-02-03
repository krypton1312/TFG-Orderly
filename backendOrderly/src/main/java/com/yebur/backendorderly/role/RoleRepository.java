package com.yebur.backendorderly.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT new com.yebur.backendorderly.role.RoleResponse(r.id, r.name) FROM Role r")
    List<RoleResponse> findAllDTO();

    @Query("SELECT new com.yebur.backendorderly.role.RoleResponse(r.id, r.name) FROM Role r WHERE r.id = :id")
    Optional<RoleResponse> findDTOById(Long id);

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}

