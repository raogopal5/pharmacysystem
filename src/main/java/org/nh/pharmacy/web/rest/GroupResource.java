package org.nh.pharmacy.web.rest;


import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.nh.pharmacy.domain.Group;
import org.nh.pharmacy.service.GroupService;
import org.nh.pharmacy.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Group.
 */
@RestController
@RequestMapping("/api")
public class GroupResource {

    private final Logger log = LoggerFactory.getLogger(GroupResource.class);

    private static final String ENTITY_NAME = "group";

    private final GroupService groupService;

    public GroupResource(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * GET  /groups : get all the groups.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of groups in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/groups")
    //@Timed
    public ResponseEntity<List<Group>> getAllGroups(@ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Groups");
        Page<Group> page = groupService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/groups");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /groups/:id : get the "id" group.
     *
     * @param id the id of the group to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the group, or with status 404 (Not Found)
     */
    @GetMapping("/groups/{id}")
    //@Timed
    public ResponseEntity<Group> getGroup(@PathVariable Long id) {
        log.debug("REST request to get Group : {}", id);
        Group group = groupService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(group));
    }

    /**
     * SEARCH  /_search/groups?query=:query : search for the group corresponding
     * to the query.
     *
     * @param query the query of the group search
     * @param pageable the pagination information
     * @return the result of the search
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/_search/groups")
    //@Timed
    public ResponseEntity<List<Group>> searchGroups(@RequestParam String query, @ApiParam Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to search for a page of Groups for query {}", query);
        Page<Group> page = groupService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/groups");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
