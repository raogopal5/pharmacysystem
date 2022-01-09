package org.nh.pharmacy.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.domain.Group;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.repository.GroupRepository;
import org.nh.pharmacy.repository.search.GroupSearchRepository;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.service.SystemAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.nh.pharmacy.domain.enumeration.Context.values;

/**
 * Service Implementation for managing Group.
 */
@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    private final Logger log = LoggerFactory.getLogger(GroupServiceImpl.class);

    private final GroupRepository groupRepository;

    private final ObjectMapper objectMapper;

    private final GroupSearchRepository groupSearchRepository;

    private final ApplicationProperties applicationProperties;

    @Autowired
    SystemAlertService systemAlertService;

    public GroupServiceImpl(GroupRepository groupRepository, ObjectMapper objectMapper, GroupSearchRepository groupSearchRepository,
                            ApplicationProperties applicationProperties) {
        this.groupRepository = groupRepository;
        this.objectMapper = objectMapper;
        this.groupSearchRepository = groupSearchRepository;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Save a group.
     *
     * @param group the entity to save
     * @return the persisted entity
     */
    @Override
    public Group save(Group group) {
        log.debug("Request to save Group : {}", group);
        Group result = groupRepository.save(group);
        groupSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the groups.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Group> findAll(Pageable pageable) {
        log.debug("Request to get all Groups");
        Page<Group> result = groupRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one group by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Group findOne(Long id) {
        log.debug("Request to get Group : {}", id);
        Group group = groupRepository.findById(id).get();
        return group;
    }

    /**
     * Check existence of group
     *
     * @param groupId
     * @return boolean
     */
    @Override
    @Transactional(readOnly = true)
    public boolean groupExists(String groupId) {
        log.debug("Request to check the existence for group : {}", groupId);
        Iterator<Group> groupItr = groupSearchRepository.search(boolQuery().must(termQuery("code.raw", groupId)).must(termQuery("active", true))).iterator();
        return groupItr.hasNext() ? true : false;
    }

    /**
     * Get group(s) associated to user
     *
     * @param userId
     * @return groupList
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> groupsForUser(String userId) {
        log.debug("Request to get the groups associated to the user : {}", userId);
        List<String> groupCodeList = new ArrayList<>();
        Iterable<Group> groupList = groupSearchRepository.search(boolQuery()
            .must(queryStringQuery(stream(values()).map(context -> "context:" + context + " ").collect(Collectors.joining()).concat("code:Administrators")).defaultOperator(Operator.OR))
            .must(termQuery("active", true)).filter(nestedQuery("members", boolQuery().must(matchQuery("members.member.code", userId)).must(matchQuery("members.inactive", false)), ScoreMode.None)));
        groupList.forEach(group -> groupCodeList.add(group.getCode()));
        return groupCodeList;
    }

    /**
     * Get group members
     *
     * @param groupId
     * @return userList
     */
    @Override
    @Transactional(readOnly = true)
    public Set<String> getMembersForGroup(String groupId) {
        log.debug("Request to get the members associated to the group : {}", groupId);
        Set<String> memberList = new HashSet<>();
        Iterable<Group> groupList = groupSearchRepository.search(boolQuery().must(termQuery("code.raw", groupId)).must(termQuery("active", true)).filter(nestedQuery("members", termQuery("members.inactive", false), ScoreMode.None).innerHit(new InnerHitBuilder())));
        groupList.forEach(group -> group.getMembers().stream().map(groupMember -> (String) objectMapper.convertValue(groupMember.getMember(), Map.class).get("code")).forEachOrdered(memberList::add));
        return memberList;
    }

    /**
     * Delete the  group by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Group : {}", id);
        groupRepository.deleteById(id);
        groupSearchRepository.deleteById(id);
    }

    /**
     * Search for the group corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Group> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Groups for query {}", query);
        Page<Group> result = groupSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }

    /**
     * subscriber and save group
     *
     * @param groupInput
     */
    @ServiceActivator(inputChannel = Channels.GROUP_INPUT)
    @Override
    public void consume(String groupInput) {
        try {
            Group group = objectMapper.readValue(groupInput, Group.class);
            log.debug("Request to consume Group code : {}", group.getCode());
            groupRepository.save(group);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }

    @Override
    public void doIndex() {
        log.debug("Request to do elastic index on Group");
        groupSearchRepository.saveAll(groupRepository.findAll());

        long resultCount = groupRepository.findAll().size();
        int pageSize = applicationProperties.getConfigs().geIndexPageSize();
        int lastPageNumber = (int) (Math.ceil(resultCount / pageSize));
        for (int i = 0; i <= lastPageNumber; i++) {
            Page<Group> data = groupRepository.findAllSortById(PageRequest.of(i, pageSize));
            if (data.getTotalElements() != 0) {
                groupSearchRepository.saveAll(data);
            }
        }
        groupSearchRepository.refresh();
    }
}
