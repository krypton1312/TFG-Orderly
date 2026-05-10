package com.yebur.backendorderly.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductEntry {
    private String name;
    private long quantity;
}
