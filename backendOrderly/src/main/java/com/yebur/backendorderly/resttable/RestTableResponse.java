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

    private String position;

    public RestTableResponse(Long id, int number, TableStatus statusEnum, RestTablePosition position) {
        this.id = id;
        this.number = number;
        this.status = translateTableStatic(statusEnum);
        this.position = position.toString();
    }

    public RestTableResponse(Long id) {this.id = id;}

    private String translateTableStatic(TableStatus status) {
        switch (status) {
            case AVAILABLE->{
                return "Disponible";
            }
            case OCCUPIED->{
                return "Ocupado";
            }
            case RESERVED->{
                return "Reservado";
            }
            default ->{
                return "Fuera de servicio";
            }
        }
    }
}