package org.nh.pharmacy.job;

import org.nh.pharmacy.service.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProcessInstanceRegenerateJob implements Job {

    private Logger logger = LoggerFactory.getLogger(ProcessInstanceRegenerateJob.class);

    @Autowired
    private PharmacyWorkflowService pharmacyWorkflowService;

    @Autowired
    private StockIssueService stockIssueService;

    @Autowired
    private BillingService billingService;

    @Autowired
    private StockReceiptService stockReceiptService;

    @Autowired
    private StockCorrectionService stockCorrectionService;

    @Autowired
    private InventoryAdjustmentService inventoryAdjustmentService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            checkAndRegenerateProcessInstance();
        } catch (Exception e) {
            logger.error("Error while executing job", e);
        }
    }

    private void checkAndRegenerateProcessInstance() {
        List<Object[]> processList = this.pharmacyWorkflowService.checkForProcessInstanceIssue();
        for (int i = 0; i < processList.size(); i++) {
            Object[] record = (Object[]) processList.get(i);
            if ("stock_direct_transfer_document_process".equals(record[2])
                || "stock_issue_document_process".equals(record[2])) {
                stockIssueService.regenerateWorkflow((String)record[0]);
            } else if ("dispense_document_process".equals(record[2])) {
                billingService.regenerateWorkflow((String) record[0]);
            } else if ("stock_receipt_document_process".equals(record[2])) {
                stockReceiptService.regenerateWorkflow((String) record[0]);
            } else if ("stock_correction_document_process".equals(record[2])) {
                stockCorrectionService.regenerateWorkflow((String)record[0]);
            } else if ("inventory_adjustment_document_process".equals(record[2])) {
                inventoryAdjustmentService.regenerateWorkflow((String)record[0]);
            }
        }
    }

}
