package com.yebur.backendorderly.websocket;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class WsEvent {
    private WsEventType type;

    private Long orderId;
    private String overviewId;
    private List<Long> detailIds;

    private Set<String> destinations;

    private Long sessionId;          // nullable — set for SESSION_OPENED events

    private Instant ts = Instant.now();

    // Full constructor (backwards-compatible with existing order call sites that pass null ts)
    public WsEvent(WsEventType type, Long orderId, String overviewId, List<Long> detailIds,
                   Set<String> destinations, Long sessionId) {
        this.type = type;
        this.orderId = orderId;
        this.overviewId = overviewId;
        this.detailIds = detailIds;
        this.destinations = destinations;
        this.sessionId = sessionId;
        this.ts = Instant.now();
    }

    // Convenience constructor for SESSION_OPENED — sessionId only, other order fields null
    public WsEvent(WsEventType type, Long orderId, String overviewId, List<Long> detailIds,
                   Long sessionId) {
        this(type, orderId, overviewId, detailIds, null, sessionId);
    }
}

