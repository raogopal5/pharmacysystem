package org.nh.pharmacy.aop.pam;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.nh.billing.domain.*;
import org.nh.billing.domain.dto.SponsorInvoiceDTO;
import org.nh.billing.domain.enumeration.InvoiceType;
import org.nh.billing.service.PamIntegrationService;
import org.nh.common.enumeration.DocumentType;
import org.nh.pharmacy.service.DispenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * By default, it only runs with the "dev" profile.
 */
@Aspect
public class PamIntegrationAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DispenseService dispenseService;

    private final PamIntegrationService pamIntegrationService;

    public PamIntegrationAspect(DispenseService dispenseService,
                                 PamIntegrationService pamIntegrationService) {
        this.dispenseService = dispenseService;
        this.pamIntegrationService = pamIntegrationService;
    }

    /**
     * Pointcut that matches all services.
     */
    @Pointcut("@annotation(org.nh.pharmacy.annotation.PamIntegration)")
    public void pamIntergationPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Advice that call billing api in single transaction.
     */
    @AfterReturning(pointcut = "pamIntergationPointcut()", returning = "returnMapInvoice")
    public void logAfterReturning(Object returnMapInvoice) {
        log.debug("calling log AfterReturing ");
        Map mapObject = (Map) returnMapInvoice;
        //logic to write in map check Invoice object
        PamIntegration pamIntegration = null;
        List<Long> docList=new ArrayList<Long>();
        Iterator iterator = mapObject.keySet().iterator();
         while(iterator.hasNext()){
            String keyDocument = iterator.next().toString();
            if(keyDocument.equals("invoice")){
                    List<PamIntegration> pamIntegrationList = pamIntegrationService.findByTxnRefNo(((Invoice) mapObject.get("invoice")).getInvoiceNumber());
                    for(PamIntegration pamIntegration1:pamIntegrationList){
                        if(((Invoice) mapObject.get("invoice")).getInvoiceDocument().getInvoiceType().equals(InvoiceType.REVERSAL) && pamIntegration1.getDocumentType().equals("RECEIPT"))
                            continue;
                        dispenseService.produce(pamIntegration1.getPamDocument());
                        pamIntegration1.setPublished(true);
                        pamIntegrationService.save(pamIntegration1);
                    }
                   continue;
            }else if(keyDocument.equals("receipts")){
                List<Receipt> receiptList = (List) mapObject.get("receipts");
                for (Receipt receipt : receiptList) {
                    PamIntegration pamIntegrationForReceipt = pamIntegrationService.findOneByTxnRefNo(receipt.getReceiptNumber());
                    if (pamIntegrationForReceipt != null) {
                        dispenseService.produce(pamIntegrationForReceipt.getPamDocument());
                        pamIntegrationForReceipt.setPublished(true);
                        pamIntegrationService.save(pamIntegrationForReceipt);
                    }
                }
            }
            else if(keyDocument.equals("receipt")){
                 pamIntegration = pamIntegrationService.findOneByTxnRefNo(((Receipt) mapObject.get("receipt")).getReceiptNumber());
            }
            else if(keyDocument.equals("refund")){
                 pamIntegration = pamIntegrationService.findOneByTxnRefNo(((Refund) mapObject.get("refund")).getRefundNumber());
            }else if(keyDocument.equals("sponsorInvoice")){
                pamIntegration = pamIntegrationService.findOneByTxnRefNo(((SponsorInvoice) mapObject.get("sponsorInvoice")).getSponsorInvoiceNumber(),DocumentType.SPONSOR_INVOICE);
            }else if(keyDocument.equals("sponsorInvoices")){
                if(null != mapObject.get("sponsorInvoices")){
                    List<SponsorInvoiceDTO> sponsorInvoiceList = (List) mapObject.get("sponsorInvoices");
                    for (SponsorInvoiceDTO sponsorInvoice : sponsorInvoiceList) {
                        PamIntegration pamIntegrationForSponsorInvoice = pamIntegrationService.findOneByTxnRefNo(sponsorInvoice.getSponsorInvoiceNumber(),DocumentType.SPONSOR_INVOICE);
                        if (pamIntegrationForSponsorInvoice != null) {
                            dispenseService.produce(pamIntegrationForSponsorInvoice.getPamDocument());
                            pamIntegrationForSponsorInvoice.setPublished(true);
                            pamIntegrationService.save(pamIntegrationForSponsorInvoice);
                        }
                    }
                }
            }
             if(pamIntegration != null) {
                 if(docList.contains(pamIntegration.getPamDocument().getDocumentId()))
                     continue;
                 dispenseService.produce(pamIntegration.getPamDocument());
                 pamIntegration.setPublished(true);
                 pamIntegrationService.save(pamIntegration);
                 docList.add(pamIntegration.getPamDocument().getDocumentId());

             }
         }
    }
}
