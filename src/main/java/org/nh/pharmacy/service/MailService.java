package org.nh.pharmacy.service;


import org.nh.pharmacy.domain.dto.EmailRequest;

/**
 * Service Interface for managing MailService.
 */
public interface MailService {

    /**
     * Send email
     *
     * @param emailRequst
     */
    public void sendMail(EmailRequest emailRequst) throws Exception;
}
