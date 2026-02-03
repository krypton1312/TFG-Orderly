package com.yebur.backendorderly.role;

import java.util.List;
import java.util.Optional;

public interface RoleServiceInterface {

    Optional<Role> findRoleById(Long id);

    Optional<RoleResponse> findRoleDTOById(Long id);

    List<RoleResponse> findAllRoleDTO();

    RoleResponse create(RoleRequest roleRequest);

    RoleResponse update(Long id, RoleRequest roleRequest);

    void delete(Long id);
}

