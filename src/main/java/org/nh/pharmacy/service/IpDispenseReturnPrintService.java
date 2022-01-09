package org.nh.pharmacy.service;

public interface IpDispenseReturnPrintService {
    byte[] getIpDispenseReturnPdf(Long dispenseReturnId, String dispenseReturnNumber) throws Exception;
}

