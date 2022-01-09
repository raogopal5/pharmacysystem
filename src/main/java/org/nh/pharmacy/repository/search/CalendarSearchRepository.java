package org.nh.pharmacy.repository.search;

import org.nh.pharmacy.domain.Calendar;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Calendar entity.
 */
public interface CalendarSearchRepository extends ElasticsearchRepository<Calendar, Long> {
}
