package org.nh.pharmacy.domain.dto;

import com.sendgrid.Attachments;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Pojo class to hold user request email data.
 */
public class EmailRequest implements Serializable {

    private List<String> ccEmail = new ArrayList<>();
    private List<String> bccEmail = new ArrayList<>();
    private String fromEmail;
    private List<String> toEmail = new ArrayList<>();
    private String subject;
    private String content;
    private List<Attachments> attachmentsInfo = new ArrayList<>();

    public List<String> addToEmail(String toEmailAdd) {
        this.toEmail.add(toEmailAdd);
        return toEmail;
    }

    public List<Attachments> getAttachmentsInfo() {
        return attachmentsInfo;
    }

    public void setAttachmentsInfo(List<Attachments> attachmentsInfo) {
        this.attachmentsInfo = attachmentsInfo;
    }

    public List<String> getToEmail() {
        return toEmail;
    }

    public void setToEmail(List<String> toEmail) {
        this.toEmail = toEmail;
    }

    public List<String> getCcEmail() {
        return ccEmail;
    }

    public void setCcEmail(List<String> ccEmail) {
        this.ccEmail = ccEmail;
    }

    public List<String> getBccEmail() {
        return bccEmail;
    }

    public void setBccEmail(List<String> bccEmail) {
        this.bccEmail = bccEmail;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String encoderFile(String filePath) throws Exception {
        String base64Image = "";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filePath).getFile());
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            // Reading a  file from file system
            byte fileData[] = new byte[(int) file.length()];
            imageInFile.read(fileData);
            base64Image = Base64.getEncoder().encodeToString(fileData);
        }
        return base64Image;
    }
}
