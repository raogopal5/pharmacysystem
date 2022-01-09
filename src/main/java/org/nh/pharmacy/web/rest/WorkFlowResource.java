package org.nh.pharmacy.web.rest;


import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.process.ProcessInstance;
import org.nh.jbpm.service.WorkflowService;
import org.nh.jbpm.web.rest.util.HeaderUtil;
import org.nh.pharmacy.service.StockConsumptionService;
import org.nh.pharmacy.service.StockIndentService;
import org.nh.pharmacy.service.StockIssueService;
import org.nh.pharmacy.service.StockReceiptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * REST controller for managing workflow.
 */
@RestController
@RequestMapping("/api")
public class WorkFlowResource {

    private final Logger log = LoggerFactory.getLogger(WorkFlowResource.class);

    private final StockIndentService stockIndentService;

    private final StockIssueService stockIssueService;

    private final StockReceiptService stockReceiptService;

    private final StockConsumptionService stockConsumptionService;

    private final WorkflowService workflowService;

    public WorkFlowResource(StockIndentService stockIndentService, StockIssueService stockIssueService, StockReceiptService stockReceiptService, StockConsumptionService stockConsumptionService, WorkflowService workflowService) {
        this.stockIndentService = stockIndentService;
        this.stockIssueService = stockIssueService;
        this.stockReceiptService = stockReceiptService;
        this.stockConsumptionService = stockConsumptionService;
        this.workflowService = workflowService;
    }

    @GetMapping("/update-process-instances")
    //@Timed
    public ResponseEntity<Void> updateProcessInstances() throws URISyntaxException {
        log.debug("REST to add unit id for process instances");
        Collection<ProcessInstanceDesc> processInstanceList = workflowService.getRuntimeDataService().getProcessInstances(Arrays.asList(ProcessInstance.STATE_ACTIVE), null, null);
        if (isEmpty(processInstanceList)) {
            log.info("Active process instances are not available");
        } else {
            for (ProcessInstanceDesc processInstanceDesc : processInstanceList) {
                Object unit_id = workflowService.getProcessService().getProcessInstanceVariable(processInstanceDesc.getId(), "unit_id");
                if (isNull(unit_id)) {
                    Map<String, Object> content = (Map<String, Object>) workflowService.getProcessService().getProcessInstanceVariable(processInstanceDesc.getId(), "content");
                    if (nonNull(content)) {
                        Long document_id = (Long) content.get("document_id");
                        Long unit;
                        switch (processInstanceDesc.getProcessId()) {
                            case "stock_indent_document_process":
                                unit = stockIndentService.findOne(document_id).getDocument().getIndentStore().getId();
                                break;
                            case "stock_issue_document_process":
                                unit = stockIssueService.findOne(document_id).getDocument().getIssueUnit().getId();
                                break;
                            case "stock_receipt_document_process":
                                unit = stockReceiptService.findOne(document_id).getDocument().getIndentUnit().getId();
                                break;
                            case "stock_consumption_document_process":
                                unit = stockConsumptionService.findOne(document_id).getDocument().getConsumptionUnit().getId();
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid transaction type");
                        }
                        workflowService.getProcessService().setProcessVariable(processInstanceDesc.getId(), "unit_id", unit);
                        log.info("Added unit {} for the process instance {} contains document id {}", unit, processInstanceDesc.getId(), document_id);
                    }
                }
            }
        }
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Adding unit id variable to process instances are completed", "")).build();
    }
}

