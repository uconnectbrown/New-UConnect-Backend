package com.uconnect.backend.helper;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.uconnect.backend.UConnectBackendApplication;
import com.uconnect.backend.awsadapter.DdbAdapter;
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

    @Autowired
    public String userTableName;

    @Autowired
    public String emailIndexName;

    @Autowired
    public String emailVerificationTableName;

    @MockBean
    public AmazonSimpleEmailService sesClient;

    @SneakyThrows
    public void setupDdb() {
        if (ddbAdapter.createOnDemandTableIfNotExists(userTableName, User.class)
                && ddbAdapter.createOnDemandTableIfNotExists(emailVerificationTableName, EmailVerification.class)) {
            // wait for the new tables to become available
            Thread.sleep(5);
        }
    }
}
