package org.nh.pharmacy.service;

import org.nh.pharmacy.domain.Dispense;

public interface PlanExecutionService {
    Dispense addPlanRules(Dispense dispense) throws Exception;
    void validatePlanRule(Dispense dispense) throws Exception;
    void validateDispensePlanRule(Dispense dispense) throws Exception;
    void accumulateAmountAndQty(Dispense dispense);
}
