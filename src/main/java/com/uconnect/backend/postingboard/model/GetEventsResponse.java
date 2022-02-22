package com.uconnect.backend.postingboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetEventsResponse {
    private List<Event> events;

    private long lastQueriedIndex;
}
