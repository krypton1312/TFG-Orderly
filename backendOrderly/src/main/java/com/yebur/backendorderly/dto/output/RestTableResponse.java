package com.yebur.backendorderly.dto.output;

import com.yebur.backendorderly.enums.TableStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RestTableResponse {
    private Long id;
    
    private int number;

    private String name;

    private String status;

    public RestTableResponse(Long id, int number, TableStatus statusEnum) {
        this.id = id;
        this.number = number;
        this.status = statusEnum.toString();

    }
    
    
}