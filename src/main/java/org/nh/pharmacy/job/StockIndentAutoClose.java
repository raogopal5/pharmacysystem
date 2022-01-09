package org.nh.pharmacy.job;

import org.nh.pharmacy.service.StockIndentService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class StockIndentAutoClose implements Job {

    @Autowired
    private StockIndentService stockIndentService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        stockIndentService.doCloseBySystem();
    }
}
