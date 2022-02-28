package com.uconnect.backend.helper;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.uconnect.backend.UConnectBackendApplication;
import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.postingboard.model.Comment;
import com.uconnect.backend.postingboard.model.Counter;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.search.model.Concentration;
import com.uconnect.backend.search.model.CourseRoster;
import com.uconnect.backend.user.model.EmailVerification;
import com.uconnect.backend.user.model.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for all integration tests
 */
@SpringBootTest(classes = UConnectBackendApplication.class)
@AutoConfigureMockMvc
public class BaseIntTest {
    @RegisterExtension
    public static LocalDbCreationExtension localDbCreationExtension = new LocalDbCreationExtension();

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public DdbAdapter ddbAdapter;

    // user info table
    @Autowired
    public String userTableName;
    @Autowired
    public String emailIndexName;

    @Autowired
    public String emailVerificationTableName;

    // event posting board tables
    @Autowired
    public String eventBoardEventHiddenTableName;
    @Autowired
    public String eventBoardEventPublishedTableName;
    @Autowired
    public String eventBoardCommentHiddenTableName;
    @Autowired
    public String eventBoardCommentPublishedTableName;

    // counter table
    @Autowired
    public String counterTableName;

    // course table
    @Autowired
    public String courseTableName;

    // concentration table
    @Autowired
    public String concentrationTableName;

    @MockBean
    public AmazonSimpleEmailService sesClient;

    @SneakyThrows
    public void setupDdb() {
        ddbAdapter.createOnDemandTableIfNotExists(userTableName, User.class);
        ddbAdapter.createOnDemandTableIfNotExists(emailVerificationTableName, EmailVerification.class);
        ddbAdapter.createOnDemandTableIfNotExists(eventBoardCommentPublishedTableName, Comment.class);
        ddbAdapter.createOnDemandTableIfNotExists(eventBoardCommentHiddenTableName, Comment.class);
        ddbAdapter.createOnDemandTableIfNotExists(eventBoardEventPublishedTableName, Event.class);
        ddbAdapter.createOnDemandTableIfNotExists(eventBoardEventHiddenTableName, Event.class);
        ddbAdapter.createOnDemandTableIfNotExists(counterTableName, Counter.class);
        ddbAdapter.createOnDemandTableIfNotExists(courseTableName, CourseRoster.class);
        ddbAdapter.createOnDemandTableIfNotExists(concentrationTableName, Concentration.class);
    }
}
