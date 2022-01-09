package org.nh.pharmacy.service.impl;

import org.kie.api.task.UserGroupCallback;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * A DBUserGroupCallbackImpl.
 */
@Primary
@Service
@Transactional
public class DBUserGroupCallbackImpl implements UserGroupCallback {

    private static final Logger log = LoggerFactory.getLogger(DBUserGroupCallbackImpl.class);

    private final UserService userService;

    private final GroupService groupService;

    public DBUserGroupCallbackImpl(UserService userService, GroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
    }

    @Override
    public boolean existsUser(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null" );
        }
        return userService.userExists(userId);
    }

    @Override
    public boolean existsGroup(String groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("GroupId cannot be null" );
        }
        return groupService.groupExists(groupId);
    }

    @Override
    public List<String> getGroupsForUser(String userId) {
        List<String> groupList = new ArrayList<>();
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null" );
        }
        groupList.addAll(groupService.groupsForUser(userId));
        return groupList;
    }
}
