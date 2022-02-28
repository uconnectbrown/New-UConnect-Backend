package integration;

import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.EventBoardTestUtil;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.model.GetEventsResponse;
import com.uconnect.backend.postingboard.service.EventBoardService;
import com.uconnect.backend.user.model.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventBoardGetTest extends BaseIntTest {

    private static User verifiedUser;

    private static String token;

    private static String verifiedHost = "Odysseus";

    private static final int numEventsPublished = 100;
    private static final int numEventsHidden = 20;

    private static List<Event> expectedEventsPublished;
    private static List<Event> expectedEventsHidden;

    private static boolean init = true;

    @Autowired
    private String eventBoardTitleIndexName;

    @Autowired
    private String counterTableName;

    @BeforeEach
    public void setup() throws Exception {
        if (init) {
            setupDdb();
            verifiedUser = MockData.generateValidUser();
            token = UserTestUtil.getTokenForTraditionalUser(mockMvc, verifiedUser, true, ddbAdapter, userTableName);

            expectedEventsPublished = new ArrayList<>(numEventsPublished);
            for (int i = 0; i < numEventsPublished; i++) {
                Event e = MockData.generateValidEventBoardEvent();
                e.setAuthor(verifiedUser.getUsername());
                e.setHost(verifiedHost);
                e.setAnonymous(false);
                e.setTitle(String.valueOf(i));
                e.setComments(new ArrayList<>(0));

                EventBoardTestUtil.submitEventVerified(mockMvc, e, verifiedUser.getUsername(), token).andExpect(status().isOk());
                expectedEventsPublished.add(e);
            }

            expectedEventsHidden = new ArrayList<>(numEventsHidden);
            for (int i = 0; i < numEventsHidden; i++) {
                Event e = MockData.generateValidEventBoardEvent();
                e.setAuthor(EventBoardService.ANONYMOUS_AUTHOR);
                e.setHost(EventBoardService.ANONYMOUS_HOST);
                e.setAnonymous(true);
                e.setTitle(String.valueOf(i));
                e.setComments(new ArrayList<>(0));

                EventBoardTestUtil.submitEventAnonymous(mockMvc, e).andExpect(status().isOk());
                expectedEventsHidden.add(e);
            }

            init = false;
        }
    }

    /*
    ---------------------
    --getByIndex tests--
    ---------------------
     */
    @Test
    @SneakyThrows
    public void testSuccessGetByIndex() {
        int index = (int) (Math.random() * numEventsPublished);
        Event foundEvent = EventBoardTestUtil.getEventObjByIndex(mockMvc, index, verifiedUser.getUsername(), token);
        EventBoardTestUtil.verifySameEvents(expectedEventsPublished.get(index), foundEvent);
    }

    @Test
    @SneakyThrows
    public void testFailureIndexTooHigh() {
        EventBoardTestUtil.getEventByIndex(mockMvc, numEventsPublished, verifiedUser.getUsername(), token)
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void testFailureIndexTooLow() {
        EventBoardTestUtil.getEventByIndex(mockMvc, -1, verifiedUser.getUsername(), token)
                .andExpect(status().isNotFound());
    }

    /*
    --------------------
    --get-latest tests--
    --------------------
     */
    @Test
    @SneakyThrows
    public void testSuccessGetLatest25() {
        int count = 25;
        GetEventsResponse response = EventBoardTestUtil.getLatestEventsResponse(mockMvc, -1, count, verifiedUser.getUsername(),
                token);
        verifyPublishedEventList(response.getEvents(), numEventsPublished - 1, count);
        assertEquals(numEventsPublished - count, response.getLastQueriedIndex());
    }

    @Test
    @SneakyThrows
    public void testSuccessGetLatestOverCountLimit() {
        int requestCount = EventBoardService.MAX_SCAN_COUNT + 1;
        int realCount = EventBoardService.MAX_SCAN_COUNT;
        GetEventsResponse response = EventBoardTestUtil.getLatestEventsResponse(mockMvc, -1, requestCount, verifiedUser.getUsername(),
                token);
        verifyPublishedEventList(response.getEvents(), numEventsPublished - 1, EventBoardService.MAX_SCAN_COUNT);
        assertEquals(numEventsPublished - realCount, response.getLastQueriedIndex());
    }

    @Test
    @SneakyThrows
    public void testSuccessGetLatestWithStartIndex() {
        int startIndex = 9;
        int count = 10;
        GetEventsResponse response = EventBoardTestUtil.getLatestEventsResponse(mockMvc, startIndex, count, verifiedUser.getUsername(),
                token);
        verifyPublishedEventList(response.getEvents(), startIndex, count);
        assertEquals(startIndex - count + 1, response.getLastQueriedIndex());
    }

    @Test
    @SneakyThrows
    public void testSuccessGetLatestCountGreaterThanRemaining() {
        int startIndex = 19;
        int count = 28;
        GetEventsResponse response = EventBoardTestUtil.getLatestEventsResponse(mockMvc, startIndex, count, verifiedUser.getUsername(),
                token);
        verifyPublishedEventList(response.getEvents(), startIndex, startIndex + 1);
        assertEquals(0, response.getLastQueriedIndex());
    }

    @Test
    @SneakyThrows
    public void testSuccessGetLatestNegativeCount() {
        int startIndex = 0;
        int count = -5;
        GetEventsResponse response = EventBoardTestUtil.getLatestEventsResponse(mockMvc, startIndex, count, verifiedUser.getUsername(),
                token);
        assertTrue(response.getEvents().isEmpty());
        assertEquals(-1, response.getLastQueriedIndex());
    }

    @Test
    @SneakyThrows
    public void testSuccessGetLatestBrokenIndexStreak() {
        int startIndex = 9;
        int requestedCount = 10;
        int realCount = 5;

        // temporarily remove index 5 to 9
        for (int i = realCount; i < startIndex + 1; i++) {
            Event removed = expectedEventsPublished.get(i);
            List<Event> events = ddbAdapter.queryGSI(eventBoardEventPublishedTableName, eventBoardTitleIndexName, removed, Event.class);
            assertFalse(events.isEmpty());
            removed = events.get(0);
            ddbAdapter.delete(eventBoardEventPublishedTableName, removed);
        }

        GetEventsResponse response = EventBoardTestUtil.getLatestEventsResponse(mockMvc, startIndex, requestedCount, verifiedUser.getUsername(),
                token);
        verifyPublishedEventList(response.getEvents(), realCount - 1, realCount);
        assertEquals(0, response.getLastQueriedIndex());

        // add them back to the db
        for (int i = realCount; i < startIndex + 1; i++) {
            Event removed = expectedEventsPublished.get(i);
            removed.setIndex(i);
            ddbAdapter.save(eventBoardEventPublishedTableName, removed);
        }
    }

    /**
     * actualList should be sorted by index in descending order, expectedList is ascending, so we walk the two lists simultaneously as such:<br>
     * - expectedList: startIndex -> 0 <br>
     * - actualList: 0 -> (count-1)
     */
    private void verifyPublishedEventList(List<Event> actualEvents, int startIndex, int count) {
        assertEquals(count, actualEvents.size());
        assertTrue(startIndex + 1 >= count);

        int actualIndex = 0;
        int expectedIndex = startIndex;
        while (actualIndex < count) {
            Event expected = expectedEventsPublished.get(expectedIndex);
            Event actual = actualEvents.get(actualIndex);
            EventBoardTestUtil.verifySameEvents(expected, actual);

            ++actualIndex;
            --expectedIndex;
        }
    }
}
