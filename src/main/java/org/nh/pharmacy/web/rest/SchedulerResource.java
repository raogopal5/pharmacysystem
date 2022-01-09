package org.nh.pharmacy.web.rest;


import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.domain.SchedulerEntity;
import org.nh.pharmacy.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;

/**
 * REST controller for managing scheduler
 */
@RestController
@RequestMapping("/api")
public class SchedulerResource {

    private final Logger log = LoggerFactory.getLogger(SchedulerResource.class);

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ApplicationProperties applicationProperties;

    @GetMapping("/schedulers")
    //@Timed
    public ResponseEntity<List<SchedulerEntity>> getAllInfo()
        throws URISyntaxException {
        log.debug("REST request to get a page of Scheduler");
        List<SchedulerEntity> schedulerList = schedulerService.getAllInfo();
        return new ResponseEntity<>(schedulerList, HttpStatus.OK);
    }

    @PutMapping("/schedulers")
    //@Timed
    public ResponseEntity<SchedulerEntity> updateScheduler(@Valid @RequestBody SchedulerEntity schedulerEntity) {

        log.debug("REST request to update Scheduler Expression : {}", schedulerEntity);

        String updatedScheduler = schedulerEntity.getScheduler();
        SchedulerEntity scheduler = schedulerService.findBySchedulerName(updatedScheduler);
        if (scheduler == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        long updatedNextFireTime = schedulerEntity.getNextFireTime();
        long updatedPreviousFireTime = schedulerEntity.getPreviousFireTime();
        long updatedStartTime = schedulerEntity.getStartTime();
        Long updatedRepeatInterval = schedulerEntity.getRepeatInterval();
        Long currentRepeatInterval = scheduler.getRepeatInterval();
        String updatedExpression = schedulerEntity.getExpression();
        Boolean invalidParam = true;

        //    Validate nextFireTime
        if (updatedNextFireTime < updatedPreviousFireTime || updatedNextFireTime < updatedStartTime || (updatedNextFireTime < 0)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (updatedRepeatInterval == null && currentRepeatInterval == null && updatedExpression != null && org.quartz.CronExpression.isValidExpression(updatedExpression)) {
            scheduler.setExpression(updatedExpression);
            invalidParam = false;
        }
        if (updatedRepeatInterval != null && updatedExpression == null && updatedRepeatInterval > 60000) {
            scheduler.setRepeatInterval(updatedRepeatInterval);
            invalidParam = false;
        }
        if (invalidParam) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        scheduler.setNextFireTime(updatedNextFireTime);

        SchedulerEntity schedulerLatest = schedulerService.updateScheduler(scheduler);
        return new ResponseEntity<>(schedulerLatest, HttpStatus.OK);
    }
}
