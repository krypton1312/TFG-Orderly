package com.yebur.backendorderly.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 30, message = "must have between 2 and 30 characters")
    private String name;
}
