package com.yebur.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashCountResponse {
    private Long id;
    private Long session_id;

    private LocalDateTime createdAt;

    private Integer c001;
    private Integer c002;
    private Integer c005;
    private Integer c010;
    private Integer c020;
    private Integer c050;
    private Integer c100;
    private Integer c200;

    private Integer b005;
    private Integer b010;
    private Integer b020;
    private Integer b050;
    private Integer b100;
    private Integer b200;
    private Integer b500;
}
