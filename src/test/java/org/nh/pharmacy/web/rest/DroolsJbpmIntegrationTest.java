package org.nh.pharmacy.web.rest;

import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.nh.jbpm.service.WorkflowService;
import org.nh.pharmacy.PharmacyApp;
import org.nh.pharmacy.config.SecurityBeanOverrideConfiguration;
import org.nh.pharmacy.config.SpringLiquibaseOverrideConfguration;
import org.nh.pharmacy.domain.Item;
import org.nh.pharmacy.service.impl.DBUserGroupCallbackImpl;
import org.nh.pharmacy.service.impl.DBUserInfoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.List;

/**
 * DroolsJbpmIntegrationTest
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PharmacyApp.class, SecurityBeanOverrideConfiguration.class, SpringLiquibaseOverrideConfguration.class})
public class DroolsJbpmIntegrationTest {

    @Autowired
    private KieBase kieBase;

    @Autowired
    private DBUserGroupCallbackImpl dbUserGroupCallback;

    @Autowired
    private DBUserInfoImpl dbUserInfoImpl;

    @Autowired
    private WorkflowService workflowService;

    @Test
    public void checkRuleExecution() {
        KieSession kieSession = kieBase.newKieSession();
        kieSession.insert(new Item().code("123").name("Cotton"));
        kieSession.getAgenda().getAgendaGroup("rule1").setFocus();
        int count;
        try {
            count = kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }
        Assert.assertEquals("Rules executed", 1, count);
    }

    @Test
    public void checkProcessExecution() {
        RuntimeManager runtimeManager = workflowService.getDeployedUnit().getRuntimeManager();
        KieSession kieSession = runtimeManager.getRuntimeEngine(EmptyContext.get()).getKieSession();
        kieSession.insert(new Item().code("123").name("Cotton"));
        Long processInstanceId = workflowService.startProcess(workflowService.getDeployedUnit(), "sample_process", null);
        ProcessInstanceDesc processInstance = workflowService.getRuntimeDataService().getProcessInstanceById(processInstanceId);
        int state = processInstance.getState();
        Assert.assertEquals("Process completed", 2, state);
    }

    @Test
    public void checkDBUserGroupCallbackImplementation() {
        //check userExists
        boolean userExists = dbUserGroupCallback.existsUser("admin");
        Assert.assertTrue(userExists);

        //check groupExists
        boolean exists = dbUserGroupCallback.existsGroup("Manangers");
        Assert.assertTrue(exists);

        //check userGroups
        List<String> groups = dbUserGroupCallback.getGroupsForUser("admin");
        Assert.assertNotNull(groups);
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals("Administrators", groups.get(0));
    }

    @Test
    public void checkDBUserInfoImplementation() {

        //check displayName
        String displayName = dbUserInfoImpl.getDisplayName(organizationalEntity());
        Assert.assertEquals("Administrator", displayName);

        //check hasEmail
        boolean hasEmail = dbUserInfoImpl.hasEmail(group());
        Assert.assertFalse(hasEmail);

        //check userEmail
        String email = dbUserInfoImpl.getEmailForEntity(organizationalEntity());
        Assert.assertEquals("admin@localhost", email);

        //check userLanguage
        String language = dbUserInfoImpl.getLanguageForEntity(organizationalEntity());
        Assert.assertEquals("en-UK", language);

        //check userGroups
        Iterator<OrganizationalEntity> members = dbUserInfoImpl.getMembersForGroup(group());

        Assert.assertEquals(members.hasNext(), true);
        while (members.hasNext()) {
            Assert.assertEquals("admin", members.next().getId());
        }
    }

    public OrganizationalEntity organizationalEntity() {
        return new OrganizationalEntity() {
            @Override
            public String getId() {
                return "admin";
            }

            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
            }

            @Override
            public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            }
        };
    }

    public Group group() {
        return new Group() {
            @Override
            public String getId() {
                return "Administrators";
            }

            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
            }

            @Override
            public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            }
        };
    }
}

