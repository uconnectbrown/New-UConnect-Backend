package integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.EventBoardTestUtil;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.postingboard.model.Comment;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.service.EventBoardService;
import com.uconnect.backend.user.model.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.uconnect.backend.postingboard.service.EventBoardService.EMPTY_REACTION_COLLECTION;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventBoardCommentTest extends BaseIntTest {

    private static User verifiedUser;

    private static User verifiedUserNoPassword;

    private static String token;

    private static String parentEventId;

    private static long parentEventIndex;

    private static boolean init = true;

    @Autowired
    private String eventBoardTitleIndexName;

    @Autowired
    private String eventBoardCommentParentIdIndexName;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    public void setup() throws Exception {
        if (init) {
            setupDdb();

            verifiedUser = MockData.generateValidUser();
            verifiedUserNoPassword = User.builder()
                    .username(verifiedUser.getUsername())
                    .firstName(verifiedUser.getFirstName())
                    .lastName(verifiedUser.getLastName())
                    .imageUrl(verifiedUser.getImageUrl())
                    .password("")
                    .build();

            token = UserTestUtil.getTokenForTraditionalUser(mockMvc, verifiedUser, true, ddbAdapter, userTableName);

            // get the auto generated id from ddb
            Event parentEvent = MockData.generateValidEventBoardEvent();
            parentEvent.setAuthor(verifiedUser.getUsername());
            EventBoardTestUtil.submitEventVerified(mockMvc, parentEvent, verifiedUser.getUsername(), token)
                    .andExpect(status().isOk());
            List<Event> events = ddbAdapter.queryGSI(eventBoardEventPublishedTableName, eventBoardTitleIndexName, parentEvent, Event.class);
            assertEquals(events.size(), 1);
            parentEventId = events.get(0).getId();
            parentEventIndex = events.get(0).getIndex();

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
        Comment comment = MockData.generateValidEventBoardComment(parentEventId);
        comment.setAuthor("");
        comment.setIsAnonymous(true);

        testSuccessSubmitAnon(comment);
    }

    @Test
    public void testSuccessSubmitAnonNotEmptyAuthor() {
        Comment comment = MockData.generateValidEventBoardComment(parentEventId);
        comment.setAuthor("John Cena");

        testSuccessSubmitAnon(comment);
    }

    @Test
    public void testSuccessSubmitAnonFalseIsAnonymous() {
        Comment comment = MockData.generateValidEventBoardComment(parentEventId);
        comment.setIsAnonymous(false);

        testSuccessSubmitAnon(comment);
    }

    @Test
    @SneakyThrows
    public void testFailureSubmitAnonNotNullId() {
        Comment comment = MockData.generateValidEventBoardComment(parentEventId);
        comment.setId("naughty string");

        EventBoardTestUtil.submitCommentAnonymous(mockMvc, comment)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        containsString("New comments are assigned random IDs")));
    }

    /*
    -------------------------
    --submit verified tests--
    -------------------------
     */
    @Test
    public void testSuccessSubmitVerified() {
        Comment comment = MockData.generateValidEventBoardComment(parentEventId);
        comment.setIsAnonymous(false);
        comment.setAuthor(verifiedUser.getUsername());

        testSuccessSubmitVerified(comment);
    }

    @Test
    public void testSuccessTrueIsAnonymous() {
        Comment comment = MockData.generateValidEventBoardComment(parentEventId);
        comment.setAuthor(verifiedUser.getUsername());
        comment.setIsAnonymous(true);

        testSuccessSubmitVerified(comment);
    }

    @Test
    @SneakyThrows
    public void testFailureNaughtyAuthor() {
        Comment comment = MockData.generateValidEventBoardComment(parentEventId);
        comment.setAuthor("Jeremiah");

        EventBoardTestUtil.submitCommentVerified(mockMvc, comment, verifiedUser.getUsername(), token)
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(
                        containsString("You are not authorized to make that request. We've got our eyes on you!")));
        List<Comment> events = ddbAdapter.queryGSI(eventBoardCommentPublishedTableName, eventBoardCommentParentIdIndexName, comment, Comment.class);
        assertTrue(events.isEmpty());
    }

    /*
    -------------
    --get tests--
    -------------
     */
    @Test
    @SneakyThrows
    public void testSuccessMultipleSingleComments() {
        int numComments = 5;
        List<Comment> expectedComments = new ArrayList<>();
        for (int i = 0; i < numComments; i++) {
            Comment comment = MockData.generateValidEventBoardComment(parentEventId);
            comment.setAuthor(verifiedUser.getUsername());
            comment.setAuthorInfo(verifiedUserNoPassword);
            comment.setIsAnonymous(false);
            expectedComments.add(comment);

            EventBoardTestUtil.submitCommentVerified(mockMvc, comment, verifiedUser.getUsername(), token)
                    .andExpect(status().isOk());
        }

        Event actualEvent = EventBoardTestUtil.getEventObjByIndex(mockMvc, parentEventIndex, verifiedUser.getUsername(), token);
        List<Comment> actualComments = new ArrayList<>(actualEvent.getComments());
        assertEquals(numComments, actualComments.size());
        actualComments.sort(Comparator.comparing(Comment::getTimestamp));
        try {
            for (int i = 1; i < numComments; i++) {
                EventBoardTestUtil.verifySameCommentsSkipReactions(expectedComments.get(i), actualComments.get(i));
            }
        } finally {
            for (Comment comment : actualComments) {
                ddbAdapter.delete(eventBoardCommentPublishedTableName, comment);
            }
        }
    }

    @Test
    @SneakyThrows
    public void testSuccessComplexCommentTrees() {
        int numTrees = 5;
        for (int i = 0; i < numTrees; i++) {
            testOneComplexCommentTree(5, 30, 3, 20);
        }
    }

    /*
    -----------
    --helpers--
    -----------
     */
    @SneakyThrows
    private void testSuccessSubmitAnon(Comment comment) {
        MvcResult result = EventBoardTestUtil.submitCommentAnonymous(mockMvc, comment)
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        comment = mapper.treeToValue(node.get("entity"), Comment.class);

        Comment actualComment = verifyNewAnonComment(comment);
        ddbAdapter.delete(eventBoardCommentHiddenTableName, actualComment);
    }

    @SneakyThrows
    private void testSuccessSubmitVerified(Comment comment) {
        MvcResult result = EventBoardTestUtil.submitCommentVerified(mockMvc, comment, verifiedUser.getUsername(), token)
                .andExpect(status().isOk()).andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        comment = mapper.treeToValue(node.get("entity"), Comment.class);

        Comment actualComment = verifyNewVerifiedComment(comment);
        ddbAdapter.delete(eventBoardCommentPublishedTableName, actualComment);
    }

    private Comment verifyNewAnonComment(Comment comment) {
        List<Comment> comments = ddbAdapter.query(eventBoardCommentHiddenTableName, comment, Comment.class);
        assertFalse(comments.isEmpty());
        Comment actualComment = comments.get(0);

        comment.setAuthor(EventBoardService.ANONYMOUS_AUTHOR);
        comment.setIsAnonymous(true);
        EventBoardTestUtil.verifySameCommentsSkipReactions(comment, actualComment);

        return actualComment;
    }

    private Comment verifyNewVerifiedComment(Comment comment) {
        List<Comment> comments = ddbAdapter.query(eventBoardCommentPublishedTableName, comment, Comment.class);
        assertFalse(comments.isEmpty());
        Comment actualComment = comments.get(0);

        assertFalse(actualComment.getIsAnonymous());
        comment.setIsAnonymous(false);
        EventBoardTestUtil.verifySameCommentsSkipReactions(comment, actualComment);

        return actualComment;
    }

    private List<Comment> buildRandomCommentTree(int minDepth, int maxDepth, int maxNumChildren, int newLevelPercentage) {
        return buildRandomCommentTree(minDepth, maxDepth, maxNumChildren, newLevelPercentage, 0);
    }

    private List<Comment> buildRandomCommentTree(int minDepth, int maxDepth, int maxNumChildren, int newLevelPercentage, int currDepth) {
        if (currDepth >= maxDepth) {
            return new ArrayList<>();
        }

        // unlucky, no children for you :(
        if (currDepth >= minDepth && (int) (Math.random() * 100) >= newLevelPercentage) {
            return new ArrayList<>();
        }

        int numChildren = (int) (Math.random() * maxNumChildren + 1);
        List<Comment> comments = new ArrayList<>(numChildren);
        for (int i = 0; i < numChildren; i++) {
            Comment comment = MockData.generateValidEventBoardComment();
            comment.setAuthor(verifiedUser.getUsername());
            comment.setAuthorInfo(verifiedUserNoPassword);
            comment.setIsAnonymous(false);
            comment.setIsCommentPresent(true);
            comment.setReactions(EMPTY_REACTION_COLLECTION);
            comments.add(comment);
            comment.setComments(buildRandomCommentTree(minDepth, maxDepth, maxNumChildren, newLevelPercentage, currDepth + 1));
        }

        return comments;
    }

    /**
     * Recursively submits all comments in the comment tree. Will fill the ID field of each comment after submission.
     */
    @SneakyThrows
    private void submitComments(List<Comment> comments, String parentId) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        for (int i = 0; i < comments.size(); i++) {
            Comment comment = comments.get(i);
            comment.setParentId(parentId);

            EventBoardTestUtil.submitCommentVerified(mockMvc, comment, verifiedUser.getUsername(), token)
                    .andExpect(status().isOk());
            List<Comment> dbComments = new ArrayList<>(ddbAdapter.queryGSI(eventBoardCommentPublishedTableName,
                    eventBoardCommentParentIdIndexName, comment, Comment.class));
            dbComments.sort(Comparator.comparing(Comment::getTimestamp));
            String thisId = dbComments.get(i).getId();
            comment.setId(thisId);
            submitComments(comment.getComments(), thisId);
        }
    }

    /**
     * Recursively deletes all comments in the comment tree
     */
    @SneakyThrows
    private void deleteComments(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        for (Comment comment : comments) {
            ddbAdapter.delete(eventBoardCommentPublishedTableName, comment);
            deleteComments(comment.getComments());
        }
    }

    @SneakyThrows
    private void testOneComplexCommentTree(int minDepth, int maxDepth, int maxNumChildren, int newLevelPercentage) {
        List<Comment> expectedCommentTree = buildRandomCommentTree(minDepth, maxDepth, maxNumChildren, newLevelPercentage);
        submitComments(expectedCommentTree, parentEventId);
        Event event = EventBoardTestUtil.getEventObjByIndex(mockMvc, parentEventIndex, verifiedUser.getUsername(), token);
        EventBoardTestUtil.haveSameComments(expectedCommentTree, event.getComments());
        deleteComments(event.getComments());
    }
}
