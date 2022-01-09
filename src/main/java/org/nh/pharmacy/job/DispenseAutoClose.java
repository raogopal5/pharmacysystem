package org.nh.pharmacy.job;

import org.nh.pharmacy.domain.Dispense;
import org.nh.pharmacy.domain.enumeration.DispenseStatus;
import org.nh.pharmacy.service.DispenseService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by Nirbhay on 10/17/17.
 */
public class DispenseAutoClose implements Job {

    private Logger logger = LoggerFactory.getLogger(DispenseAutoClose.class);

    @Autowired
    DispenseService dispenseService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        String uniqueId = UUID.randomUUID().toString();
        logger.info("Dispense Auto close job {} started at {}", uniqueId,currentDateTime);
        Page<Dispense> page = dispenseService.search("document.dispenseStatus.raw:(" + DispenseStatus.DRAFT + " OR " + DispenseStatus.APPROVED + " OR " + DispenseStatus.REJECTED + " OR " + DispenseStatus.PENDING_APPROVAL + ")", PageRequest.of(0, 10000));
        Iterator<Dispense> dispenseIterator = page.iterator();
        while (dispenseIterator.hasNext()) {
            Dispense dispense = dispenseIterator.next();
            try {
                dispenseService.autoCloseDispenseDocument(currentDateTime, dispense);
            } catch (Exception e) {
                logger.error("Dispense indexing issue for Dispense number: {}", dispense.getDocumentNumber(), e);
                dispenseService.reIndex(dispense.getId());
            }
        }
        logger.info("Dispense Auto close job {} completed at {}", uniqueId, LocalDateTime.now());
    }
}
