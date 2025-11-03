package com.yebur.backendorderly.websocket;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsEvent {
    private WsEventType type;

    private Long orderId;
    private String overviewId;
    private List<Long> detailIds;

    private Set<String> destinations;
    private Instant ts = Instant.now();
}

