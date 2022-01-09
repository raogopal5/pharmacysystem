package org.nh.pharmacy.service.impl;

import org.nh.jbpm.domain.dto.JBPMTask;
import org.nh.jbpm.listeners.TaskCommandRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskCommandRunnerImpl implements TaskCommandRunner {


    public TaskCommandRunnerImpl() {

    }

    @Override
    @Transactional
    public void execute(JBPMTask jbpmTask) {
      // write any task info updating logic to domain entities.
    }
}
