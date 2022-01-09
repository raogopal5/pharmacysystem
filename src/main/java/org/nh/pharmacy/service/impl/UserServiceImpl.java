package org.nh.pharmacy.service.impl;

import org.apache.commons.lang.StringUtils;
import org.nh.pharmacy.config.ApplicationProperties;
import org.nh.pharmacy.config.Channels;
import org.nh.pharmacy.config.Constants;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.domain.User;
import org.nh.pharmacy.repository.UserRepository;
import org.nh.pharmacy.repository.search.UserSearchRepository;
import org.nh.pharmacy.service.PharmacyRedisCacheService;
import org.nh.pharmacy.service.SystemAlertService;
import org.nh.pharmacy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Objects;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.Operator.AND;

/**
 * Service Implementation for managing User.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    private final UserSearchRepository userSearchRepository;

    private final PharmacyRedisCacheService pharmacyRedisCacheService;

    private final ApplicationProperties applicationProperties;

    private final ElasticsearchOperations elasticsearchTemplate;

    @Autowired
    SystemAlertService systemAlertService;

    public UserServiceImpl(UserRepository userRepository, UserSearchRepository userSearchRepository, PharmacyRedisCacheService pharmacyRedisCacheService, ApplicationProperties applicationProperties, ElasticsearchOperations elasticsearchTemplate) {
        this.userRepository = userRepository;
        this.userSearchRepository = userSearchRepository;
        this.pharmacyRedisCacheService = pharmacyRedisCacheService;
        this.applicationProperties = applicationProperties;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    /**
     * Save a user.
     *
     * @param user the entity to save
     * @return the persisted entity
     */
    @Override
    public User save(User user) {
        log.debug("Request to save User : {}", user);
        User result = userRepository.save(user);
        userSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the users.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        log.debug("Request to get all Users");
        Page<User> result = userRepository.findAll(pageable);
        return result;
    }

    /**
     * Get one user by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public User findOne(Long id) {
        log.debug("Request to get User : {}", id);
        User user = userRepository.findById(id).get();
        return user;
    }

    /**
     * Check existence of user
     *
     * @param login
     * @return boolean
     */
    @Override
    @Transactional(readOnly = true)
    public boolean userExists(String login) {
        if(applicationProperties.getRedisCache().isCacheEnabled() && StringUtils.isNotBlank(login))
        {
            String cacheKey = Constants.USER_LOGIN+login;
            return Objects.nonNull(pharmacyRedisCacheService.getUserData(cacheKey, elasticsearchTemplate));
        }else {
            Iterator<User> userItr = userSearchRepository.search(boolQuery().must(termQuery("login.raw", login)).must(termQuery("active", true))).iterator();
            return userItr.hasNext() ? true : false;
        }
    }

    /**
     * Retrieve user detail using login
     *
     * @param login
     * @return user entity
     */
    @Override
    @Transactional(readOnly = true)
    public User findUserByLogin(String login) {
        log.debug("Request to find user for login : {}", login);

        if(applicationProperties.getRedisCache().isCacheEnabled() && StringUtils.isNotBlank(login)) {
            String cacheKey = Constants.PHR_ENTITY + Constants.USER_LOGIN + login;
            return pharmacyRedisCacheService.getUserEntityData(cacheKey,login, elasticsearchTemplate);
        }

        Iterator<User> userItr = userSearchRepository.search(boolQuery().must(termQuery("login.raw", login)).must(termQuery("active", true))).iterator();
        if (userItr.hasNext()) {
            return userItr.next();
        } else {
            return null;
        }
    }

    /**
     * Delete the  user by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete User : {}", id);
        userRepository.deleteById(id);
        userSearchRepository.deleteById(id);
    }

    /**
     * Search for the user corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<User> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Users for query {}", query);
        Page<User> result = userSearchRepository.search(queryStringQuery(query)
            .defaultOperator(AND), pageable);
        return result;
    }

    /**
     * subscriber and save user
     *
     * @param user
     */
    @ServiceActivator(inputChannel = Channels.USER_INPUT)
    @Override
    public void consume(User user) {
        try {
            log.debug("Request to consume User code : {}", user.getId());
            userRepository.save(user);
        } catch (Exception ex) {
            systemAlertService.save(new SystemAlert()
                .fromClass(this.getClass().getName())
                .onDate(ZonedDateTime.now())
                .message("Error while processing message ")
                .addDescription(ex));
        }
    }

    /**
     * Retrieve user detail using email
     *
     * @param email
     * @return user entity
     */
    @Override
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        log.debug("Request to find user for email : {}", email);
        Iterator<User> userItr = userSearchRepository.search(boolQuery().must(termQuery("email.raw", email)).must(termQuery("active", true))).iterator();
        if (userItr.hasNext()) {
            return userItr.next();
        } else {
            return null;
        }
    }
}
