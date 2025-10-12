package com.yebur.backendorderly.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestTableRequest {
    private int number;

    private String status;
}
