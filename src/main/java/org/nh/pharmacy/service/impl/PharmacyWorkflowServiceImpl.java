package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.nh.jbpm.domain.dto.JBPMTask;
import org.nh.jbpm.repository.search.JBPMTaskSearchRepository;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.service.PharmacyWorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PharmacyWorkflowServiceImpl implements PharmacyWorkflowService {

    private Logger logger = LoggerFactory.getLogger(PharmacyWorkflowServiceImpl.class);

    private final WorkflowService workflowService;

    private final EntityManager entityManager;

    private final JBPMTaskSearchRepository jbpmTaskSearchRepository;

    public PharmacyWorkflowServiceImpl(WorkflowService workflowService, EntityManager entityManager, JBPMTaskSearchRepository jbpmTaskSearchRepository) {
        this.workflowService = workflowService;
        this.entityManager = entityManager;
        this.jbpmTaskSearchRepository = jbpmTaskSearchRepository;
    }

    @Override
    public List<Object[]> checkForProcessInstanceIssue() {
        StringBuilder sb = new StringBuilder("select vi.value documentNumber,pi.instance_id instanceId, pi.process_id processId from process_instance_info pi ")
            .append("inner join variable_instance_log vi on vi.process_instance_id = pi.instance_id and vi.variable_id = 'document_number' ")
            .append("where process_instance_byte_array is null and start_date between now()-interval '1 hour' and now()-interval '10 minute'");
            return entityManager.createNativeQuery(sb.toString()).getResultList();
    }

    @Override
    public List<Object[]> getTasksForProcessInstance(Long processInstanceId) {
        StringBuilder sb = new StringBuilder("select process_id, id as task_id from task where process_instance_id=?");
        return entityManager.createNativeQuery(sb.toString()).setParameter(1, processInstanceId).getResultList();
    }

    @Override
    public void clearProcessInstance(String documentNumber) {

        Object singleResult = null;
        try {
            singleResult = this.entityManager.createNativeQuery("select process_instance_id from variable_instance_log where value = ?").setParameter(1, documentNumber).getSingleResult();
        } catch (Exception e) {
            logger.error("Error while fetching the process instance id");
        }
        if (singleResult == null) {
            return;
        }
        Long processInstanceId = Long.valueOf(singleResult.toString());
        List<Object[]> tasks = this.getTasksForProcessInstance(processInstanceId);

        this.entityManager.createNativeQuery("delete from task_variable_impl where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from task_event where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();

        this.entityManager.createNativeQuery("delete from i18ntext where task_descriptions_id in (select id from task where process_instance_id = ?)").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from i18ntext where task_subjects_id in (select id from task where process_instance_id = ?)").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from i18ntext where task_names_id in (select id from task where process_instance_id = ?)").setParameter(1, processInstanceId).executeUpdate();

        this.entityManager.createNativeQuery("delete from people_assignments_bas where task_id in (select id from task where process_instance_id = ?)").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from people_assignments_pot_owners where task_id in (select id from task where process_instance_id = ?)").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from people_assignments_excl_owners where task_id in (select id from task where process_instance_id = ?)").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from people_assignments_recipients where task_id in (select id from task where process_instance_id = ?)").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from people_assignments_stakeholders where task_id in (select id from task where process_instance_id = ?)").setParameter(1, processInstanceId).executeUpdate();

        this.entityManager.createNativeQuery("delete from task where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from variable_instance_log where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from node_instance_log where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();

        this.entityManager.createNativeQuery("delete from correlation_property_info where correlation_key_key_id in (select key_id from correlation_key_info where process_instance_id = ?)").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from correlation_key_info where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();

        this.entityManager.createNativeQuery("delete from bamtask_summary where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from audit_task_impl where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from work_item_info where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from process_instance_event_info where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from process_instance_info where instance_id = ?").setParameter(1, processInstanceId).executeUpdate();
        this.entityManager.createNativeQuery("delete from process_instance_log where process_instance_id = ?").setParameter(1, processInstanceId).executeUpdate();

        if (CollectionUtils.isNotEmpty(tasks)) {
            jbpmTaskSearchRepository.deleteAll(tasks.stream().map(task -> {
                JBPMTask taskDoc = new JBPMTask();
                taskDoc.setId("phr-" + task[0].toString() + "-" + task[1].toString());
                return taskDoc;
            }).collect(Collectors.toList()));
        }
    }

    /*@Override
    public void printProcessInstanceByteArray(Long processInstanceId) {
        this.workflowService.getProcessService().getProcessInstance(processInstanceId).signalEvent();
        ProcessInstanceInfo processInstanceInfo = this.entityManager.createQuery("select p from ProcessInstanceInfo p where p.processInstanceId = :procInstanceId", ProcessInstanceInfo.class).setParameter("procInstanceId", processInstanceId).getSingleResult();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ProcessMarshallerWriteContext context = new ProcessMarshallerWriteContext(baos,
                (InternalKnowledgeBase)null, (InternalWorkingMemory)null, (Map)null, (ObjectMarshallingStrategyStore)null,
                KieServices.Factory.get().newEnvironment());
            context.setProcessInstanceId(processInstanceInfo.getProcessInstanceId());
            context.setState(processInstanceInfo.getState() == 1 ? 1 : 2);
            String processType = "RuleFlow";
            ObjectOutputStream stream = context.stream;
            stream.writeUTF(processType);

            ProcessInstanceMarshaller marshaller = ProcessMarshallerRegistry.INSTANCE.getMarshaller(processType);
            WorkflowProcessInstanceImpl ruleFlowProcessInstance = new RuleFlowProcessInstance();
            ruleFlowProcessInstance.setId(processInstanceId);
            ruleFlowProcessInstance.setProcessId(processInstanceInfo.getProcessId());
            ruleFlowProcessInstance.setState(processInstanceInfo.getState());
            ruleFlowProcessInstance.setProcess(kieBase.getProcess(processInstanceInfo.getProcessId()));
            ruleFlowProcessInstance.setSignalCompletion(true);

            Object result = marshaller.writeProcessInstance(context, ruleFlowProcessInstance);
            if (marshaller instanceof ProtobufRuleFlowProcessInstanceMarshaller && result != null) {
                org.jbpm.marshalling.impl.JBPMMessages.ProcessInstance _instance = (org.jbpm.marshalling.impl.JBPMMessages.ProcessInstance)result;
                PersisterHelper.writeToStreamWithHeader(context, _instance);
            }

            context.close();
        } catch (IOException var8) {
            throw new IllegalArgumentException("IOException while storing process instance " + processInstanceInfo.getId() + ": " + var8.getMessage(), var8);
        }

        byte[] newByteArray = baos.toByteArray();
        this.entityManager.createQuery("update ProcessInstanceInfo set processInstanceByteArray = ? where processInstanceId = ?")
            .setParameter(0, newByteArray)
            .setParameter(1, processInstanceId)
            .executeUpdate();


    }*/

}
