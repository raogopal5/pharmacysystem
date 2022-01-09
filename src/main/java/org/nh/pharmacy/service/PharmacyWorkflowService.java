package org.nh.pharmacy.service;

import java.util.List;

public interface PharmacyWorkflowService {

    List<Object[]> checkForProcessInstanceIssue();

    List<Object[]> getTasksForProcessInstance(Long processInstanceId);

    void clearProcessInstance(String documentNumber);

}

