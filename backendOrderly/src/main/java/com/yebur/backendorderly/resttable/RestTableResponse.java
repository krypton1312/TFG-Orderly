package com.yebur.backendorderly.resttable;

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
    
    public RestTableResponse(Long id) {
        this.id = id;
    }
}