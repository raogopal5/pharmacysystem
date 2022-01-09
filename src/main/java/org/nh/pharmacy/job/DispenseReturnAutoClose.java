package org.nh.pharmacy.job;

import org.nh.pharmacy.service.DispenseReturnService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Indrajeet on 11/08/17.
 */
public class DispenseReturnAutoClose implements Job {

    @Autowired
    DispenseReturnService dispenseReturnService;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        dispenseReturnService.autoClose();
    }
}
