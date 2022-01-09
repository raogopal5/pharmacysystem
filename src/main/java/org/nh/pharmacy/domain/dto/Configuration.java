package org.nh.pharmacy.domain.dto;

import org.nh.pharmacy.domain.enumeration.ConfigurationLevel;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.io.Serializable;

/**
 * A Configuration.
 */
@Document(indexName = "configuration")
@Setting(settingPath = "/es/settings.json")
public class Configuration implements Serializable {

    private ConfigurationLevel applicableType;

    private Long applicableTo;

    private String applicableCode;

    private String key;

    private String value;

    private Integer level;

    public ConfigurationLevel getApplicableType() {
        return applicableType;
    }

    public void setApplicableType(ConfigurationLevel applicableType) {
        this.applicableType = applicableType;
    }

    public Long getApplicableTo() {
        return applicableTo;
    }

    public void setApplicableTo(Long applicableTo) {
        this.applicableTo = applicableTo;
    }

    public String getApplicableCode() {
        return applicableCode;
    }

    public void setApplicableCode(String applicableCode) {
        this.applicableCode = applicableCode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Configuration applicableTo(Long applicableTo) {
        this.applicableTo = applicableTo;
        return this;
    }

    public Configuration applicableType(ConfigurationLevel applicableType) {
        this.applicableType = applicableType;
        return this;
    }

    public Configuration key(String key) {
        this.key = key;
        return this;
    }

    public Configuration value(String value) {
        this.value = value;
        return this;
    }

    public Configuration applicableCode(String applicableCode) {
        this.applicableCode = applicableCode;
        return this;
    }

    public Configuration level(Integer level) {
        this.level = level;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Configuration)) return false;

        Configuration that = (Configuration) o;

        if (getApplicableType() != that.getApplicableType()) return false;
        if (!getApplicableTo().equals(that.getApplicableTo())) return false;
        if (!getApplicableCode().equals(that.getApplicableCode())) return false;
        if (!getKey().equals(that.getKey())) return false;
        if (!getValue().equals(that.getValue())) return false;
        return getLevel().equals(that.getLevel());
    }

    @Override
    public int hashCode() {
        int result = getApplicableType().hashCode();
        result = 31 * result + getApplicableTo().hashCode();
        result = 31 * result + getApplicableCode().hashCode();
        result = 31 * result + getKey().hashCode();
        result = 31 * result + getValue().hashCode();
        result = 31 * result + getLevel().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Configuration{" +
            ", applicableType=" + applicableType +
            ", applicableTo=" + applicableTo +
            ", applicableCode='" + applicableCode + '\'' +
            ", key='" + key + '\'' +
            ", value='" + value + '\'' +
            ", level=" + level +
            '}';
    }
}
