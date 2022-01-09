package org.nh.pharmacy.service.impl;


import com.sendgrid.*;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.dto.EmailRequest;
import org.nh.pharmacy.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Service class is to configure and send email using SendGrid API
 */
@Service
public class SendGridServiceImpl implements MailService {

    private final Logger log = LoggerFactory.getLogger(SendGridServiceImpl.class);

    private final ApplicationProperties applicationProperties;

    public SendGridServiceImpl(ApplicationProperties applicationProperties) {

        this.applicationProperties = applicationProperties;
    }

    /**
     * @param emailRequest
     * @return Mail
     */
    private Mail personalizeEmail(EmailRequest emailRequest) throws Exception {
        Mail mail = new Mail();
        /* From information setting */
        Email fromEmail = new Email();
        fromEmail.setEmail(emailRequest.getFromEmail());
        mail.setFrom(fromEmail);
        mail.setSubject(emailRequest.getSubject());

        /*
         * Personalization setting, we only add recipient info for this particular
         * example
         */
        Personalization personalization = new Personalization();

        for (String ccEmail : emailRequest.getToEmail()) {

            Email to = new Email();
            to.setEmail(ccEmail);
            personalization.addTo(to);

        }

        for (String ccEmail : emailRequest.getCcEmail()) {
            Email cc = new Email();
            cc.setEmail(ccEmail);
            personalization.addCc(cc);
        }

        for (String bccEmail : emailRequest.getBccEmail()) {
            Email bcc = new Email();
            bcc.setEmail(bccEmail);
            personalization.addBcc(bcc);
        }

        mail.addPersonalization(personalization);

        /* Reply to setting */
        Email replyTo = new Email();
        replyTo.setEmail(emailRequest.getFromEmail());
        mail.setReplyTo(replyTo);

        /* Adding Content of the email */
        Content content = new Content();

        /* Adding email message/body */
        content.setType("text/html");
        content.setValue(emailRequest.getContent());
        mail.addContent(content);
        for (Attachments attachments : emailRequest.getAttachmentsInfo()) {
            mail.addAttachments(attachments);
        }

        return mail;

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.nh.pharmacy.service.impl.SendGridService#sendMail(
     * org.nh.pharmacy.domain.dto.emailRequest)
     */
    @Override
    public void sendMail(EmailRequest emailRequest) throws Exception {
        try {
            String sendGridApi = applicationProperties.getSendGrid().getApiKey().toString();
            if (sendGridApi == null || sendGridApi == "") {
                throw new Exception("mail sending failed.send grid API is not configured :");
            }
            String statusCode = "";
            SendGrid sg = new SendGrid(sendGridApi);
            Request request = new Request();
            Mail mail = personalizeEmail(emailRequest);
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            log.debug("Send Grid Response code" + response.getStatusCode());
            statusCode = String.valueOf(response.getStatusCode());
            if (response.getStatusCode() != 202 && response.getStatusCode() != 200) {
                throw new Exception("mail sending failed.send grid error code :" + statusCode);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
