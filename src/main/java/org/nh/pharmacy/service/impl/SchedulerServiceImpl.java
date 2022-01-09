package org.nh.pharmacy.service.impl;

import org.nh.pharmacy.domain.SchedulerEntity;
import org.nh.pharmacy.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Service Implementation for managing scheduler.
 */
@Service
@Transactional
public class SchedulerServiceImpl implements SchedulerService {

    private final Logger log = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    @Autowired
    private EntityManager enitityManager;

    public SchedulerServiceImpl() {
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchedulerEntity> getAllInfo() {

        log.debug("Request to get all Scheduler Info");

        String getSql = "select qt.job_group as instanceName, qt.trigger_name as scheduler, " +
            "qt.description as description, \n" +
            "qct.cron_expression as expression, qst.repeat_interval as repeatInterval, " +
            "qt.start_time as startTime, qt.next_fire_time as nextFireTime, qt.prev_fire_time as previousFireTime, qt.trigger_state as triggerState\n" +
            "from qrtz_triggers qt left outer join qrtz_cron_triggers qct on qct.trigger_name = qt.trigger_name and qct.trigger_group = qt.trigger_group\n" +
            "and qct.sched_name = qt.sched_name\n" + "left outer join qrtz_simple_triggers qst on qst.trigger_name = qt.trigger_name and qst.trigger_group = qt.trigger_group\n" + "and qst.sched_name = qt.sched_name\n" +
            "order by 1";

        Query query = enitityManager.createNativeQuery(getSql, "schedulerMapping");
        return query.getResultList();
    }

    @Override
    public SchedulerEntity findBySchedulerName(String schedulerName) {

        String searchSql = "select qt.job_group as instanceName, qt.trigger_name as scheduler, \n" +
            "qt.description as description, \n" +
            "qct.cron_expression as expression, qst.repeat_interval as repeatInterval, \n" +
            "qt.start_time as startTime, qt.next_fire_time as nextFireTime, qt.prev_fire_time as previousFireTime, qt.trigger_state as triggerState\n" +
            "from qrtz_triggers qt left outer join qrtz_cron_triggers qct on qct.trigger_name =qt.trigger_name and qct.trigger_group = qt.trigger_group\n" +
            "and qct.sched_name = qt.sched_name\n" + "left outer join qrtz_simple_triggers qst on qst.trigger_name = qt.trigger_name and qst.trigger_group = qt.trigger_group\n" +
            "and qst.sched_name = qt.sched_name\n" + "where qt.trigger_name =?1\n" +
            "order by 1";

        Query query = enitityManager.createNativeQuery(searchSql, "schedulerMapping");
        query.setParameter(1, schedulerName);
        List<SchedulerEntity> schedulerEntityList = query.getResultList();
        SchedulerEntity schedulerEntity = null;
        try {
            schedulerEntity = schedulerEntityList.listIterator().next();
        } catch (Exception ex) {
            log.error("No scheduler found with the schedulerName: {}", schedulerName);
            return schedulerEntity;
        }
        return schedulerEntity;
    }

    @Override
    @Transactional
    public SchedulerEntity updateScheduler(SchedulerEntity schedulerEntity) {
        String updateSql;
        Query query;
        String schedulerName = schedulerEntity.getScheduler();
        if (schedulerEntity.getExpression() == null) {
            updateSql = "UPDATE qrtz_simple_triggers qst SET repeat_interval= ?1 WHERE (qst.trigger_name= ?2)";
            query = enitityManager.createNativeQuery(updateSql);
            query.setParameter(1, schedulerEntity.getRepeatInterval());
            query.setParameter(2, schedulerName);
        } else {
            updateSql = "UPDATE qrtz_cron_triggers qct SET cron_expression= ?1 WHERE (qct.trigger_name= ?2)";
            query = enitityManager.createNativeQuery(updateSql);
            query.setParameter(1, schedulerEntity.getExpression());
            query.setParameter(2, schedulerName);
        }
        query.executeUpdate();
        String queryFireTimeQuery = "UPDATE qrtz_triggers qt SET next_fire_time= ?1 WHERE (qt.trigger_name= ?2)";
        Query setFireQuery = enitityManager.createNativeQuery(queryFireTimeQuery);
        setFireQuery.setParameter(1, schedulerEntity.getNextFireTime());
        setFireQuery.setParameter(2, schedulerName);
        setFireQuery.executeUpdate();
        return schedulerEntity;
    }
}


