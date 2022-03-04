package integration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.EventBoardTestUtil;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.postingboard.model.Comment;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.model.ReactionCollection;
import com.uconnect.backend.user.model.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventBoardReactionTest extends BaseIntTest {

    private static List<User> verifiedUsers;
    private static List<String> tokens;
    private static User viewer;
    private static String viewerToken;
    private static Set<String> verifiedViewerSet;
    private static Set<String> anonViewerSet;

    private static final Class<ReactionCollection> REACTION_COLLECTION_CLASS = ReactionCollection.class;

    private static final List<String> reactionTypes = ImmutableList.of(
            "like",
            "love",
            "interested"
    );

    private static boolean init = true;

    @BeforeEach
    public void setup() throws Exception {
        if (init) {
            setupDdb();
            verifiedUsers = ImmutableList.of(MockData.generateValidUser(), MockData.generateValidUser());
            viewer = verifiedUsers.get(0);
            tokens = ImmutableList.of(UserTestUtil.getTokenForTraditionalUser(mockMvc, verifiedUsers.get(0), true, ddbAdapter, userTableName),
                    UserTestUtil.getTokenForTraditionalUser(mockMvc, verifiedUsers.get(1), true, ddbAdapter, userTableName));
            viewerToken = tokens.get(0);
            verifiedViewerSet = ImmutableSet.of(viewer.getUsername());
            anonViewerSet = ImmutableSet.of();

            init = false;
        }
    }

    /*
    ---------------
    --react tests--
    ---------------
     */
    @Test
    @SneakyThrows
    public void testSuccessReact() {
        // create a new event and comment
        EventBoardTestUtil.submitEventVerified(mockMvc, MockData.generateValidEventBoardEvent(viewer.getUsername()),
                        viewer.getUsername(), viewerToken)
                .andExpect(status().isOk());
        Event expectedVerifiedEvent = EventBoardTestUtil.getEventObjByIndex(mockMvc, 0, viewer.getUsername(), viewerToken);
        String eventId = expectedVerifiedEvent.getId();
        Comment expectedVerifiedComment = MockData.generateValidEventBoardComment(eventId);
        expectedVerifiedComment.setAuthor(viewer.getUsername());

        EventBoardTestUtil.submitCommentVerified(mockMvc, expectedVerifiedComment, viewer.getUsername(), viewerToken)
                .andExpect(status().isOk());
        expectedVerifiedEvent = EventBoardTestUtil.getEventObjByIndex(mockMvc, 0, viewer.getUsername(), viewerToken);
        expectedVerifiedComment = expectedVerifiedEvent.getComments().get(0);
        Event expectedAnonEvent = EventBoardTestUtil.getEventObjByIndex(mockMvc, 0, "", "");
        Comment expectedAnonComment = expectedAnonEvent.getComments().get(0);

        try {
            for (String type : reactionTypes) {
                // verify pristine conditions
                assertEquals(new HashSet<>(), getUsernamesByType(expectedVerifiedEvent.getReactions(), type));
                assertEquals(0, getCountByType(expectedVerifiedEvent.getReactions(), type));

                testReact(expectedVerifiedEvent, expectedVerifiedComment, expectedAnonEvent, expectedAnonComment, type);
            }
        } finally {
            ddbAdapter.delete(eventBoardEventPublishedTableName, expectedVerifiedEvent);
            ddbAdapter.delete(eventBoardCommentPublishedTableName, expectedVerifiedComment);
        }
    }

    @Test
    public void testFailureUnknownId() {

    }

    @Test
    public void testFailureUnknownType() {

    }

    @Test
    public void testFailureRepeatedReactions() {

    }

    @SneakyThrows
    private void testReact(Event expectedVerifiedEvent, Comment expectedVerifiedComment,
                           Event expectedAnonEvent, Comment expectedAnonComment,
                           String type) {
        for (int i = 0; i < 2; i++) {
            EventBoardTestUtil.react(mockMvc, expectedVerifiedEvent.getId(), type, verifiedUsers.get(i).getUsername(), tokens.get(i))
                    .andExpect(status().isOk());
            EventBoardTestUtil.react(mockMvc, expectedVerifiedComment.getId(), type, verifiedUsers.get(i).getUsername(), tokens.get(i))
                    .andExpect(status().isOk());

            // logged-in viewer
            Event verifiedView = EventBoardTestUtil.getEventObjByIndex(mockMvc, 0, viewer.getUsername(), viewerToken);
            setUsernamesByType(expectedVerifiedEvent.getReactions(), verifiedViewerSet, type);
            setCountByType(expectedVerifiedEvent.getReactions(), i + 1, type);
            setUsernamesByType(expectedVerifiedComment.getReactions(), verifiedViewerSet, type);
            setCountByType(expectedVerifiedComment.getReactions(), i + 1, type);
            EventBoardTestUtil.verifySameEventsSkipReactions(expectedVerifiedEvent, verifiedView);
            EventBoardTestUtil.haveSameReactions(expectedVerifiedEvent.getReactions(), verifiedView.getReactions());
            EventBoardTestUtil.haveSameReactions(expectedVerifiedComment.getReactions(), verifiedView.getComments().get(0).getReactions());

            // anon viewer
            Event anonView = EventBoardTestUtil.getEventObjByIndex(mockMvc, 0, "", "");
            setUsernamesByType(expectedAnonEvent.getReactions(), anonViewerSet, type);
            setCountByType(expectedAnonEvent.getReactions(), i + 1, type);
            setUsernamesByType(expectedAnonComment.getReactions(), anonViewerSet, type);
            setCountByType(expectedAnonComment.getReactions(), i + 1, type);
            EventBoardTestUtil.verifySameEventsSkipReactions(expectedAnonEvent, anonView);
            EventBoardTestUtil.haveSameReactions(expectedAnonEvent.getReactions(), anonView.getReactions());
            EventBoardTestUtil.haveSameReactions(expectedAnonComment.getReactions(), anonView.getComments().get(0).getReactions());
        }
    }

    @SneakyThrows
    private void setUsernamesByType(ReactionCollection reactionCollection, Set<String> usernames, String type) {
        char[] chars = type.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        type = new String(chars);
        REACTION_COLLECTION_CLASS.getMethod(String.format("set%sUsernames", type), Set.class).invoke(reactionCollection, usernames);
    }

    @SneakyThrows
    private void setCountByType(ReactionCollection reactionCollection, int count, String type) {
        char[] chars = type.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        type = new String(chars);
        REACTION_COLLECTION_CLASS.getMethod(String.format("set%sCount", type), int.class).invoke(reactionCollection, count);
    }

    @SneakyThrows
    private Set<String> getUsernamesByType(ReactionCollection reactionCollection, String type) {
        char[] chars = type.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        type = new String(chars);
        return (Set<String>) REACTION_COLLECTION_CLASS.getMethod(String.format("get%sUsernames", type)).invoke(reactionCollection);
    }

    @SneakyThrows
    private int getCountByType(ReactionCollection reactionCollection, String type) {
        char[] chars = type.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        type = new String(chars);
        return (int) REACTION_COLLECTION_CLASS.getMethod(String.format("get%sCount", type)).invoke(reactionCollection);
    }
}
