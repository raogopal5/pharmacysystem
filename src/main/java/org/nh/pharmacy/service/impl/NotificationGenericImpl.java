package org.nh.pharmacy.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.nh.pharmacy.domain.User;
import org.nh.pharmacy.domain.dto.Member;
import org.nh.pharmacy.domain.dto.Notification;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.UserService;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * A NotificationServiceImpl.
 */
public abstract class NotificationGenericImpl {

    private final MessageChannel notificationChannel;

    private final UserService userService;

    private final GroupService groupService;

    public NotificationGenericImpl(UserService userService, GroupService groupService, MessageChannel notificationChannel) {
        this.userService = userService;
        this.groupService = groupService;
        this.notificationChannel = notificationChannel;
    }

    public List<Member> retrieveMemberDetailsForGroup(String groupIds) {
        Set<Member> memberDetailList = Splitter.on(",").splitToList(groupIds).stream().map(groupService::getMembersForGroup).flatMap(Collection::stream).map(this::retrieveMemberDetail).filter(Objects::nonNull).collect(Collectors.toSet());
        return Lists.newArrayList(memberDetailList);
    }

    public Member retrieveMemberDetail(String userId) {
        User user = userService.findUserByLogin(userId);
        return nonNull(user) ? new Member(user.getId(), user.getLogin(), user.getDisplayName(), user.getEmployeeNo(), user.getOrganizationUnit() == null ? null : user.getOrganizationUnit().getCode(), user.getEmail(), user.getMobileNo()) : null;
    }

    public void publishNotification(Notification notification) {
        notificationChannel.send(MessageBuilder.withPayload(notification).build());
    }
}
