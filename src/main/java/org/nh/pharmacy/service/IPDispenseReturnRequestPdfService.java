package org.nh.pharmacy.service;

public interface IPDispenseReturnRequestPdfService {
    byte[] getIpDispenseReturnRequestPdf(Long dispenseReturnId, String dispenseReturnNumber) throws Exception;
}
