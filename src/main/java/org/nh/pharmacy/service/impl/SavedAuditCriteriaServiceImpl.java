package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.Operator;
import org.nh.pharmacy.domain.SavedAuditCriteria;
import org.nh.pharmacy.repository.SavedAuditCriteriaRepository;
import org.nh.pharmacy.repository.search.SavedAuditCriteriaSearchRepository;
import org.nh.pharmacy.service.SavedAuditCriteriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing SavedAuditCriteria.
 */
@Service
@Transactional
public class SavedAuditCriteriaServiceImpl implements SavedAuditCriteriaService {

    private final Logger log = LoggerFactory.getLogger(SavedAuditCriteriaServiceImpl.class);

    private final SavedAuditCriteriaRepository savedAuditCriteriaRepository;

    private final SavedAuditCriteriaSearchRepository savedAuditCriteriaSearchRepository;

    public SavedAuditCriteriaServiceImpl(SavedAuditCriteriaRepository savedAuditCriteriaRepository, SavedAuditCriteriaSearchRepository savedAuditCriteriaSearchRepository) {
        this.savedAuditCriteriaRepository = savedAuditCriteriaRepository;
        this.savedAuditCriteriaSearchRepository = savedAuditCriteriaSearchRepository;
    }

    /**
     * Save a savedAuditCriteria.
     *
     * @param savedAuditCriteria the entity to save
     * @return the persisted entity
     */
    @Override
    public SavedAuditCriteria save(SavedAuditCriteria savedAuditCriteria) {
        log.debug("Request to save SavedAuditCriteria : {}", savedAuditCriteria);
        SavedAuditCriteria result = savedAuditCriteriaRepository.save(savedAuditCriteria);
        savedAuditCriteriaSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the savedAuditCriteria.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<SavedAuditCriteria> findAll() {
        log.debug("Request to get all SavedAuditCriteria");
        List<SavedAuditCriteria> result = savedAuditCriteriaRepository.findAll();

        return result;
    }

    /**
     * Get one savedAuditCriteria by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public SavedAuditCriteria findOne(Long id) {
        log.debug("Request to get SavedAuditCriteria : {}", id);
        SavedAuditCriteria savedAuditCriteria = savedAuditCriteriaRepository.findById(id).get();
        return savedAuditCriteria;
    }

    /**
     * Delete the  savedAuditCriteria by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete SavedAuditCriteria : {}", id);
        savedAuditCriteriaRepository.deleteById(id);
        savedAuditCriteriaSearchRepository.deleteById(id);
    }

    /**
     * Search for the savedAuditCriteria corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<SavedAuditCriteria> search(String query) {
        log.debug("Request to search SavedAuditCriteria for query {}", query);
        return StreamSupport
            .stream(savedAuditCriteriaSearchRepository.search(queryStringQuery(query)
                .defaultOperator(Operator.AND)).spliterator(), false)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SavedAuditCriteria> search(String query, Pageable pageable, String[] includeFields, String[] excludeFields) {
        log.debug("Request to search for a page of Dispenses for query {}", query);
        Query searchQuery = new NativeSearchQueryBuilder().
            withQuery(queryStringQuery(query).defaultOperator(Operator.AND))
            .withSourceFilter(new FetchSourceFilter(includeFields, excludeFields))
            .withPageable(pageable).build();
        return savedAuditCriteriaSearchRepository.search(searchQuery);
    }
    @Override
    public void deleteIndex() {
        log.debug("trying to delete elastic search index for saved audit criteria");
        savedAuditCriteriaSearchRepository.deleteAll();
        log.debug("elastic search index is deleted for saved audit criteria");
    }
    @Override
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to create index for saved audit criteria");
        List<SavedAuditCriteria> content=savedAuditCriteriaRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo,pageSize));
        if(CollectionUtils.isNotEmpty(content)){
            savedAuditCriteriaSearchRepository.saveAll(content);
        }
    }
}
