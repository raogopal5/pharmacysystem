package org.nh.pharmacy.web.rest;

import com.sendgrid.Attachments;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.dto.EmailRequest;
import org.nh.pharmacy.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for the StockResource REST controller.
 *
 * @see StockResource
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class SendGridResourceIntTest {

    private final Logger log = LoggerFactory.getLogger(SendGridResourceIntTest.class);

    private MockMvc restStockMockMvc;

    @Autowired
    private MailService mailService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void mockTestSendingMail() throws Exception {
        log.debug("Tested sending email with sendgrid");
    }

    //@Test
    public void sendingMailWithSendGrid() throws Exception {
        log.debug("Tested sending email with sendgrid");
        EmailRequest emailRequest = new EmailRequest();
        List<String> ccEmail = new ArrayList<>();
        ccEmail.add("janakirami.reddi@narayanahealth.org");
        emailRequest.setCcEmail(ccEmail);
        List<String> bccEmail = new ArrayList<>();
        bccEmail.add("sajith.chandran@narayanahealth.org");
        emailRequest.setBccEmail(bccEmail);
        String fromEmail = "PERIYASAMY.JEGANATHAN@narayanahealth.org";
        emailRequest.setFromEmail(fromEmail);
        List<String> toEmail = new ArrayList<>();
        toEmail.add("sureshkumarreddy.mopuru@narayanahealth.org");
        toEmail.add("sanjit.vimal@narayanahealth.org");
        emailRequest.setToEmail(toEmail);
        String subject = "Testing real Subject ";
        emailRequest.setSubject(subject);
        String content = "Testing with <b>real HTML<b>";
        emailRequest.setContent(content);

        final String txtPath = "temp/Dispenss_Invoice.pdf";
        String contentInfo1 = emailRequest.encoderFile(txtPath);
        Attachments attachments = new Attachments();
        attachments.setContent(contentInfo1);
        attachments.setType("application/pdf");
        attachments.setFilename("balance_001.pdf");
        attachments.setDisposition("attachment");
        attachments.setContentId("Balance Sheet");

        final String imagePath = "temp/sendgridimage.png";
        String contentInfo = emailRequest.encoderFile(txtPath);
        Attachments attachments2 = new Attachments();
        attachments2.setContent(contentInfo);
        attachments2.setType("image/png");
        attachments2.setFilename("banner.png");
        attachments2.setDisposition("attachment");
        attachments2.setContentId("Banner");


        final String txtPath1 = "temp/sendgridtext";
        String contentInfo2 = emailRequest.encoderFile(txtPath);
        Attachments attachments3 = new Attachments();
        attachments3.setContent(contentInfo2);
        attachments3.setType("application/txt");
        attachments3.setFilename("balance_9001.txt");
        attachments3.setDisposition("attachment");
        attachments3.setContentId("Balance Sheet");

        List<Attachments> attachmentsList = new ArrayList<>();
        attachmentsList.add(attachments2);
        attachmentsList.add(attachments);
        attachmentsList.add(attachments3);
        emailRequest.setAttachmentsInfo(attachmentsList);
        mailService.sendMail(emailRequest);


    }
}

