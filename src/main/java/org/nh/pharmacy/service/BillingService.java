package org.nh.pharmacy.service;

import org.nh.billing.domain.SponsorInvoice;
import org.nh.billing.domain.dto.SponsorInvoiceDTO;
import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.DispenseReturn;
import org.nh.seqgen.exception.SequenceGenerateException;

import java.util.List;
import java.util.Map;

/**
 * Created by Nirbhay on 6/23/17.
 */
public interface BillingService {

    /**
     * Save a dispense.
     *
     * @param dispense the entity to save
     * @return the persisted entity
     */
    Map<String, Object> saveDispenseWithAction(Dispense dispense, String action) throws Exception;

    /**
     * Confirm dispense
     *
     * @param dispenseId
     * @return result map
     * @throws Exception
     */
    Map<String, Object> confirmDispense(Long dispenseId) throws Exception;

    /**
     * process creating billing document
     * @param dispenseId
     * @return
     * @throws Exception
     */
    Map<String, Object> approveBillingProcess(Long dispenseId) throws Exception;

    /**
     * Re-index all created entity
     * @param dispense
     */
    void reIndexBilling(Dispense dispense);

    /**
     * Verify user discount amount for approval
     *
     * @param dispenseId
     * @return boolean
     */
    Boolean isApprovalRequired(Long dispenseId);

    /**
     * Execute workflow
     *
     * @param dispense the entity to save
     * @param transition to be performed
     * @param taskId     task Id
     * @return result map
     */
    Map<String, Object> executeWorkflow(Dispense dispense, String transition, Long taskId) throws Exception;

    /**
     * Get task constraints
     *
     * @param documentNumber
     * @param userId
     * @param taskId
     * @return taskId, constraints
     **/
    Map<String, Object> getTaskConstraints(String documentNumber, String userId, Long taskId);

    /**
     *
     * @param dispense
     * @throws Exception
     */
    public List<SponsorInvoiceDTO> createSponsorInvoiceForDispenseReturn(DispenseReturn dispense) throws Exception;

    /**
     * Generate Transaction Number for payment
     * @return
     */
    String generateTransactionNumber() throws SequenceGenerateException;

    void regenerateWorkflow(String documentNumber);
}
