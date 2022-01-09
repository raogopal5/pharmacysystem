package org.nh.pharmacy.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.Operator;
import org.nh.billing.domain.ReceiptAdjustment;
import org.nh.pharmacy.domain.StockSourceHeader;
import org.nh.pharmacy.repository.StockSourceHeaderRepository;
import org.nh.pharmacy.repository.search.StockSourceHeaderSearchRepository;
import org.nh.pharmacy.service.StockSourceHeaderService;
import org.nh.pharmacy.web.rest.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing StockSourceHeader.
 */
@Service
@Transactional
public class StockSourceHeaderServiceImpl implements StockSourceHeaderService {

    private final Logger log = LoggerFactory.getLogger(StockSourceHeaderServiceImpl.class);

    private final StockSourceHeaderRepository stockSourceHeaderRepository;

    private final StockSourceHeaderSearchRepository stockSourceHeaderSearchRepository;

    private final EntityManager entityManager;

    public StockSourceHeaderServiceImpl(StockSourceHeaderRepository stockSourceHeaderRepository, StockSourceHeaderSearchRepository stockSourceHeaderSearchRepository, EntityManager entityManager) {
        this.stockSourceHeaderRepository = stockSourceHeaderRepository;
        this.stockSourceHeaderSearchRepository = stockSourceHeaderSearchRepository;
        this.entityManager = entityManager;
    }

    /**
     * Save a stockSourceHeader.
     *
     * @param stockSourceHeader the entity to save
     * @return the persisted entity
     */
    @Override
    public StockSourceHeader save(StockSourceHeader stockSourceHeader) {
        log.debug("Request to save StockSourceHeader : {}", stockSourceHeader);
        StockSourceHeader result = stockSourceHeaderRepository.save(stockSourceHeader);
        stockSourceHeaderSearchRepository.save(result);
        return result;
    }

    /**
     * Get all the stockSourceHeaders.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockSourceHeader> findAll(Pageable pageable) {
        log.debug("Request to get all StockSourceHeaders");
        return stockSourceHeaderRepository.findAll(pageable);
    }

    /**
     * Get one stockSourceHeader by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public StockSourceHeader findOne(Long id) {
        log.debug("Request to get StockSourceHeader : {}", id);
        return stockSourceHeaderRepository.findById(id).get();
    }

    /**
     * Delete the stockSourceHeader by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete StockSourceHeader : {}", id);
        stockSourceHeaderRepository.deleteById(id);
        stockSourceHeaderSearchRepository.deleteById(id);
    }

    /**
     * Search for the stockSourceHeader corresponding to the query.
     *
     * @param query the query of the search
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StockSourceHeader> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of StockSourceHeaders for query {}", query);
        Page<StockSourceHeader> result = stockSourceHeaderSearchRepository.search(queryStringQuery(query).defaultOperator(Operator.AND), pageable);
        return result;
    }

    @Override
    public List<Map<String,Object>> getStockSourceHeaderByDocumentNo(String documentNo, String unitCode, String fromDate, String toDate, Long itemid, Integer size, Integer pageNumber){
        log.debug("Request to get stockSourceHeaderDetail for documentNumber : {} unitCode : {}", documentNo,unitCode);

        Map<String, Object> params = new HashMap<>();

        StringBuilder query = new StringBuilder("select ssh.document_number,ssh.unit_code,ssh.transaction_date,count(document_number) from stock_source_header ssh Inner Join stock_source ss on ssh.document_number = SPLIT_PART(ss.transaction_ref_no, '-', 1) And ssh.unit_code = ss.owner where ssh.unit_code =:unitCode");
        StringBuilder countQuery = new StringBuilder("select count(*) as total_count from stock_source_header ssh Inner Join stock_source ss on ssh.document_number = SPLIT_PART(ss.transaction_ref_no, '-', 1) And ssh.unit_code = ss.owner where ssh.unit_code =:unitCode");

        params.put("unitCode", unitCode);
        if (nonNull(documentNo)) {
            query.append(" and ssh.document_number like :documentNo");
            countQuery.append(" and ssh.document_number like :documentNo");
            params.put("documentNo", documentNo + "%");
        }

        if (nonNull(itemid)) {
            query.append(" and ss.item_id =:itemid");
            countQuery.append(" and ss.item_id =:itemid");
            params.put("itemid", itemid);
        }

        if (fromDate != null && toDate != null) {
            query.append(" and ssh.transaction_date BETWEEN ");
            countQuery.append(" and ssh.transaction_date BETWEEN ");
            query.append(":fromDate");
            countQuery.append(":fromDate");
            params.put("fromDate", DateUtil.getLocalDateFromStringDate(fromDate));
            query.append(" AND ");
            countQuery.append(" AND ");
            query.append(":toDate ");
            countQuery.append(":toDate ");
            params.put("toDate", DateUtil.getLocalDateFromStringDate(toDate));
        }

        Query countNativeQuery = entityManager.createNativeQuery(countQuery.toString());
        params.forEach((key, value) -> countNativeQuery.setParameter(key, value));

        Object totalCount   =  countNativeQuery.getSingleResult();
        Map<String, Object> stockSourceHeaderDetail = new HashMap<>();
        stockSourceHeaderDetail.put("totalCount",totalCount.toString());

        query.append(" group by ssh.document_number,ssh.unit_code,ssh.transaction_date  order by ssh.document_number");

        if (nonNull(size)) {
            query.append(" LIMIT :size");
            params.put("size", size);
        }
        if (nonNull(pageNumber)) {
            if(pageNumber!=0)
                pageNumber = pageNumber*20;
            query.append(" OFFSET :pageNumber");
            params.put("pageNumber", pageNumber);
        }
        Query nativeQuery = entityManager.createNativeQuery(query.toString());
        params.forEach((key, value) -> nativeQuery.setParameter(key, value));
        List<Object[]> objects = nativeQuery.getResultList();

        List stockSourceHeaderDetails =new ArrayList<>();
        //stockSourceHeaderDetails.add(stockSourceHeaderDetail);
        List<Map> stockSourceHeaderList =new ArrayList<>();
        for (Object[] object:objects) {
            Map<String,String> stockSourceHeaderDetailMap = new HashMap();
            stockSourceHeaderDetailMap.put("documentNo",object[0].toString());
            stockSourceHeaderDetailMap.put("unitCode",object[1].toString());
            stockSourceHeaderDetailMap.put("date",object[2].toString());
            stockSourceHeaderDetailMap.put("count",object[3].toString());
            stockSourceHeaderList.add(stockSourceHeaderDetailMap);
        }

        stockSourceHeaderDetail.put("barcode",stockSourceHeaderList);
        stockSourceHeaderDetails.add(stockSourceHeaderDetail);
        return stockSourceHeaderDetails;
    }
    @Override
    public void deleteIndex() {
        log.debug("trying to delete elastic search index for receipt adjustment");
        stockSourceHeaderSearchRepository.deleteAll();
        log.debug("elastic search index is deleted for receipt adjustment");
    }
    @Override
    public void doIndex(int pageNo, int pageSize, LocalDate fromDate, LocalDate toDate) {
        log.debug("Request to create index for receipt adjustment");
        List<StockSourceHeader> content=stockSourceHeaderRepository.findByDateRangeSortById(fromDate, toDate, PageRequest.of(pageNo,pageSize));
        if(CollectionUtils.isNotEmpty(content)){
            stockSourceHeaderSearchRepository.saveAll(content);
        }
    }
}
