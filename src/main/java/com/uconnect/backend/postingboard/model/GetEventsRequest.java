package com.uconnect.backend.postingboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetEventsRequest {
    private long startIndex;

    private int eventCount;
}
