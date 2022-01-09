package org.nh.pharmacy.dto;

import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Map;

/**
 * Created by Nitesh on 6/23/17.
 */
@Document(indexName = "configurations")
public class Configurations {

    private Long id;
    private String applicableType;
    private Long applicableTo;
    private Map<String, Object> configuration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicableType() {
        return applicableType;
    }

    public void setApplicableType(String applicableType) {
        this.applicableType = applicableType;
    }

    public Long getApplicableTo() {
        return applicableTo;
    }

    public void setApplicableTo(Long applicableTo) {
        this.applicableTo = applicableTo;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Configurations that = (Configurations) o;

        if (applicableType != null ? !applicableType.equals(that.applicableType) : that.applicableType != null)
            return false;
        if (applicableTo != null ? !applicableTo.equals(that.applicableTo) : that.applicableTo != null) return false;
        return configuration != null ? configuration.equals(that.configuration) : that.configuration == null;
    }

    @Override
    public int hashCode() {
        int result = applicableType != null ? applicableType.hashCode() : 0;
        result = 31 * result + (applicableTo != null ? applicableTo.hashCode() : 0);
        result = 31 * result + (configuration != null ? configuration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Configurations{" +
            "applicableType='" + applicableType + '\'' +
            ", applicableTo=" + applicableTo +
            ", configuration=" + configuration +
            '}';
    }
}
