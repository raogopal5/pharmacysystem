package org.nh.pharmacy.service;


import java.util.Map;

/**
 * Service Interface for managing Dispense.
 */
public interface IpPrintService {

  /**
     * @param dispenseId
     * @param dispenseNumber
     * @return
     * @throws Exception
     */
    byte[] getIPIssueSlipByDispenseId(Long dispenseId, String dispenseNumber, String original) throws Exception;

    public Map<String, Object> getIPIssueSlipHTMLByDispenseId(Long dispenseId, String dispenseNumber) throws Exception;

    }
