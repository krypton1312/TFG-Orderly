package com.yebur.backendorderly.resttable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestTableRequest {
    private int number;
    private String position;
    private String status;
}
