package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.SchedulerEntity;

import java.util.List;

/**
 * Service Interface for managing Scheduler.
 */
public interface SchedulerService {

    public List<SchedulerEntity> getAllInfo();

    public SchedulerEntity findBySchedulerName(String schedulerName);

    public SchedulerEntity updateScheduler(SchedulerEntity schedulerEntity);

}
