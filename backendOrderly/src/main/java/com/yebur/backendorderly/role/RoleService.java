package com.yebur.backendorderly.role;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("roleService")
public class RoleService implements RoleServiceInterface {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> findRoleById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public Optional<RoleResponse> findRoleDTOById(Long id) {
        return roleRepository.findDTOById(id);
    }

    @Override
    public List<RoleResponse> findAllRoleDTO() {
        return roleRepository.findAllDTO();
    }

    @Override
    public RoleResponse create(RoleRequest request) {
        String roleName = normalizeName(request.getName());

        if (roleRepository.existsByName(roleName)) {
            throw new IllegalArgumentException("Role already exists with name: " + roleName);
        }

        Role role = new Role();
        role.setName(roleName);

        Role saved = roleRepository.save(role);
        return RoleResponse.mapToResponse(saved);
    }

    @Override
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));

        String roleName = normalizeName(request.getName());

        // Если пытаются переименовать в уже существующую роль — запрещаем
        roleRepository.findByName(roleName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Role already exists with name: " + roleName);
            }
        });

        role.setName(roleName);
        Role updated = roleRepository.save(role);
        return RoleResponse.mapToResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new IllegalArgumentException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim().toUpperCase();
    }
}

