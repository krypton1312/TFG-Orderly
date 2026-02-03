package com.yebur.backendorderly.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Long id;
    private String name;

    public static RoleResponse mapToResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName()
        );
    }
}

