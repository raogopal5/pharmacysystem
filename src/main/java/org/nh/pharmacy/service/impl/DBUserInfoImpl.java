package org.nh.pharmacy.service.impl;

import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.UserInfo;
import org.nh.pharmacy.domain.User;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.UserService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A DBUserInfoImpl
 */
@Primary
@Service
@Transactional
public class DBUserInfoImpl implements UserInfo {

    private final UserService userService;

    private final GroupService groupService;

    public DBUserInfoImpl(UserService userService, GroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
    }

    @Override
    public String getDisplayName(OrganizationalEntity organizationalEntity) {
        User user = userService.findUserByLogin(organizationalEntity.getId());
        return user != null ? user.getDisplayName() : null;
    }

    @Override
    public Iterator<OrganizationalEntity> getMembersForGroup(Group group) {
        List<OrganizationalEntity> roles = new ArrayList<>();
        for (String member : groupService.getMembersForGroup(group.getId())) {
            roles.add(TaskModelProvider.getFactory().newUser(member));
        }
        if (roles != null)
            return roles.iterator();
        else
            return null;
    }

    @Override
    public boolean hasEmail(Group group) {
        return getEmailForEntity(group) != null ? true : false;
    }

    @Override
    public String getEmailForEntity(OrganizationalEntity organizationalEntity) {
        User user = userService.findUserByLogin(organizationalEntity.getId());
        return user != null ? user.getEmail() : null;
    }

    @Override
    public String getLanguageForEntity(OrganizationalEntity organizationalEntity) {
        return "en-UK";
    }

    @Override
    public String getEntityForEmail(String s) {
        User user = userService.findUserByEmail(s);
        return user == null ? null : user.getLogin();
    }
}
