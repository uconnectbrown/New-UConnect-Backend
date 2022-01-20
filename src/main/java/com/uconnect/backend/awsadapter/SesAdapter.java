package com.uconnect.backend.awsadapter;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
public class SesAdapter {
    private final AmazonSimpleEmailService sesClient;

    private final String sesFromAddress;

    // TODO: fill these
    private static final String VERIFICATION_EMAIL_SUBJECT = "";
    private static final String VERIFICATION_EMAIL_FORMAT = "";

    @Autowired
    public SesAdapter(AmazonSimpleEmailService sesClient, String sesFromAddress) {
        this.sesClient = sesClient;
        this.sesFromAddress = sesFromAddress;
    }

    public void sendOneEmail(String toAddress, String subject, String htmlBody) {
        sendToMultipleAddresses(ImmutableList.of(toAddress), subject, htmlBody);
    }

    public void sendToMultipleAddresses(Collection<String> toAddresses, String subject, String htmlBody) {
        SendEmailRequest request = new SendEmailRequest()
                .withDestination(
                        new Destination().withToAddresses(toAddresses))
                .withMessage(new Message()
                        .withBody(new Body()
                                .withHtml(new Content()
                                        .withCharset("UTF-8").withData(htmlBody)))
                        .withSubject(new Content()
                                .withCharset("UTF-8").withData(subject)))
                .withSource(sesFromAddress);

        sesClient.sendEmail(request);
        log.info("An email titled \"{}\" was sent to: {}", subject, StringUtils.join(toAddresses, ", "));
    }

    public void sendAccountVerificationEmail(String toAddress) {
        sendOneEmail(toAddress, VERIFICATION_EMAIL_SUBJECT, VERIFICATION_EMAIL_FORMAT);
    }
}
