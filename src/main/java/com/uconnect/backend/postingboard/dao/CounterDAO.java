package com.uconnect.backend.postingboard.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.postingboard.model.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class CounterDAO {
    private static final String EVENT_BOARD_INDEX_NAME = "eventBoardIndex";

    private final DdbAdapter ddbAdapter;

    private final String counterTableName;

    @Autowired
    public CounterDAO(DdbAdapter ddbAdapter, String counterTableName) {
        this.ddbAdapter = ddbAdapter;
        this.counterTableName = counterTableName;
    }

    synchronized public long incrementEventBoardIndex() {
        return doIncrement(EVENT_BOARD_INDEX_NAME);
    }

    private void initCounter(String counterName) {
        Counter indexCounter = Counter.builder()
                .name(counterName)
                .build();
        if (ddbAdapter.query(counterTableName, indexCounter, Counter.class).isEmpty()) {
            indexCounter.setValue(0);
            ddbAdapter.save(counterTableName, indexCounter);
        }
    }

    private long doIncrement(String counterName) {
        Counter counter = Counter.builder()
                .name(counterName)
                .build();
        List<Counter> counterList = ddbAdapter.query(counterTableName, counter, Counter.class);

        if (counterList.isEmpty()) {
            log.warn("{} does not exist in the counter table, initializing to default value (0)", counterName);
            initCounter(counterName);
        }

        long currCount = counterList.get(0).getValue();
        counter.setValue(++currCount);
        ddbAdapter.save(counterTableName, counter);

        return currCount;
    }
}
