package com.uconnect.backend.postingboard.dao;

import com.google.common.collect.ImmutableList;
import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.postingboard.model.Counter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class CounterDAO {
    public static final String EVENT_BOARD_INDEX_NAME = "eventBoardIndex";

    private final DdbAdapter ddbAdapter;

    private final String counterTableName;

    @Autowired
    public CounterDAO(DdbAdapter ddbAdapter, String counterTableName) {
        this.ddbAdapter = ddbAdapter;
        this.counterTableName = counterTableName;
        if ("dev".equals(System.getenv("SPRING_PROFILES_ACTIVE")) &&
                "true".equals(System.getenv("IS_MANUAL_TESTING"))) {
            // mirror prod tables if booting up locally for manual testing
            ddbAdapter.createOnDemandTableIfNotExists(counterTableName, Counter.class);
        }
    }

    synchronized public long incrementEventBoardIndex() {
        return doIncrement(EVENT_BOARD_INDEX_NAME);
    }

    public long getNextEventBoardIndex() {
        return getNextCounterValue(EVENT_BOARD_INDEX_NAME);
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
        Counter counter = new Counter();
        counter.setName(counterName);

        long currCount = getNextCounterValue(counterName);

        counter.setValue(++currCount);
        ddbAdapter.save(counterTableName, counter);

        return currCount;
    }

    private long getNextCounterValue(String counterName) {
        Counter counter = new Counter();
        counter.setName(counterName);
        List<Counter> counterList = ddbAdapter.query(counterTableName, counter, Counter.class);

        if (counterList.isEmpty()) {
            log.warn("{} does not exist in the counter table, initializing to default value (0)", counterName);
            initCounter(counterName);

            counter.setValue(0);
            counterList = ImmutableList.of(counter);
        }

        return counterList.get(0).getValue();
    }
}
