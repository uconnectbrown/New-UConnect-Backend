package integration;

import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.EventBoardTestUtil;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.postingboard.dao.CounterDAO;
import com.uconnect.backend.postingboard.model.Counter;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.service.EventBoardService;
import com.uconnect.backend.user.model.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// if anyone finds this in the future and tries to optimize: these tests are NOT thread-safe, do not run them in parallel
public class EventBoardSubmitTest extends BaseIntTest {

    private static User verifiedUser;

    private static String token;

    private static String verifiedHost = "Odysseus";

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

            init = false;
        }
    }

    /*
    ---------------------
    --submit anon tests--
    ---------------------
     */
    @Test
    public void testSuccessSubmitAnon() {
        Event event = MockData.generateValidEventBoardEvent();
        event.setAuthor("");
        event.setHost("");
        event.setAnonymous(true);

        testSuccessSubmitAnon(event);
    }

    @Test
    public void testSuccessSubmitAnonWithIndex() {
        Event event = MockData.generateValidEventBoardEvent();
        event.setIndex(5);

        testSuccessSubmitAnon(event);
    }

    @Test
    public void testSuccessSubmitAnonNotEmptyAuthorHost() {
        Event event = MockData.generateValidEventBoardEvent();
        event.setAuthor("John Cena");
        event.setHost("Susususuper Slam");

        testSuccessSubmitAnon(event);
    }

    @Test
    public void testSuccessSubmitAnonFalseIsAnonymous() {
        Event event = MockData.generateValidEventBoardEvent();
        event.setAnonymous(false);

        testSuccessSubmitAnon(event);
    }

    @Test
    @SneakyThrows
    public void testFailureSubmitAnonNotNullId() {
        Event event = MockData.generateValidEventBoardEvent();
        event.setId("naughty string");

        EventBoardTestUtil.submitAnonymous(mockMvc, event)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        containsString("New events are assigned random IDs")));
    }

    /*
    -------------------------
    --submit verified tests--
    -------------------------
     */
    @Test
    public void testSuccessSubmitVerified() {
        Event event = MockData.generateValidEventBoardEvent();
        event.setAuthor(verifiedUser.getUsername());
        event.setHost(verifiedHost);

        testSuccessSubmitVerified(event);
    }

    @Test
    public void testSuccessTrueIsAnonymous() {
        Event event = MockData.generateValidEventBoardEvent();
        event.setAuthor(verifiedUser.getUsername());
        event.setHost(verifiedHost);
        event.setAnonymous(true);

        testSuccessSubmitVerified(event);
    }

    @Test
    @SneakyThrows
    public void testSuccessMultipleSameTitleEvents() {
        Event event = MockData.generateValidEventBoardEvent();
        event.setAuthor(verifiedUser.getUsername());
        event.setHost(verifiedHost);
        event.setAnonymous(false);

        int numDupEvents = 5;
        for (int i = 0; i < numDupEvents; i++) {
            EventBoardTestUtil.submitVerified(mockMvc, event, verifiedUser.getUsername(), token)
                    .andExpect(status().isOk());
        }

        List<Event> events = ddbAdapter.queryGSI(eventBoardEventPublishedTableName, eventBoardTitleIndexName, event, Event.class);
        assertEquals(numDupEvents, events.size());
        Event firstEvent = events.get(0);
        for (int i = 1; i < numDupEvents; i++) {
            Event currEvent = events.get(i);
            EventBoardTestUtil.verifySameEvents(firstEvent, currEvent);
            assertEquals(firstEvent.getIndex() + i, currEvent.getIndex());
        }
    }

    // TODO: add tests for concurrently submitting new events

    @Test
    @SneakyThrows
    public void testFailureNaughtyAuthor() {
        Event event = MockData.generateValidEventBoardEvent();
        event.setAuthor("Jeremiah");

        EventBoardTestUtil.submitVerified(mockMvc, event, verifiedUser.getUsername(), token)
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(
                        containsString("You are not authorized to make that request. We've got our eyes on you!")));
        List<Event> events = ddbAdapter.queryGSI(eventBoardEventPublishedTableName, eventBoardTitleIndexName, event, Event.class);
        assertTrue(events.isEmpty());
    }

    /*
    -----------
    --helpers--
    -----------
     */
    @SneakyThrows
    private void testSuccessSubmitAnon(Event event) {
        EventBoardTestUtil.submitAnonymous(mockMvc, event)
                .andExpect(status().isOk());
        verifyNewAnonEvent(event);
    }

    @SneakyThrows
    private void testSuccessSubmitVerified(Event event) {
        EventBoardTestUtil.submitVerified(mockMvc, event, verifiedUser.getUsername(), token)
                .andExpect(status().isOk());
        verifyNewVerifiedEvent(event);
    }

    /**
     * verify the new anon event is processed correctly, then returns the event fetched directly from the db with the uuid.
     * MIGHT modify certain fields of the input Event
     */
    private Event verifyNewAnonEvent(Event event) {
        List<Event> events = ddbAdapter.queryGSI(eventBoardEventHiddenTableName, eventBoardTitleIndexName, event, Event.class);
        assertFalse(events.isEmpty());
        Event actualEvent = events.get(0);

        event.setAuthor(EventBoardService.ANONYMOUS_AUTHOR);
        event.setHost(EventBoardService.ANONYMOUS_HOST);
        event.setIndex(-1);
        event.setAnonymous(true);
        EventBoardTestUtil.verifySameEvents(event, actualEvent);

        return actualEvent;
    }

    private Event verifyNewVerifiedEvent(Event event) {
        List<Event> events = ddbAdapter.queryGSI(eventBoardEventPublishedTableName, eventBoardTitleIndexName, event, Event.class);
        assertFalse(events.isEmpty());
        Event actualEvent = events.get(0);

        assertFalse(actualEvent.isAnonymous());
        event.setIndex(getNextEventIndex() - 1);
        event.setAnonymous(false);
        EventBoardTestUtil.verifySameEvents(event, actualEvent);

        return actualEvent;
    }

    private long getNextEventIndex() {
        Counter counter = new Counter();
        counter.setName(CounterDAO.EVENT_BOARD_INDEX_NAME);
        return ddbAdapter.query(counterTableName, counter, Counter.class).get(0).getValue();
    }
}
