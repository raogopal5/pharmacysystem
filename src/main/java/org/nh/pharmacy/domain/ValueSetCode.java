package org.nh.pharmacy.domain;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.io.Serializable;

/**
 * ValueSetCode DTO.
 */
@Document(indexName = "valuesetcode", type = "valuesetcode", createIndex = false)
@Setting(settingPath = "/es/settings.json")
public class ValueSetCode implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String display;
    private Boolean active;

    public ValueSetCode() {

    }

    public ValueSetCode(Long id, String code, String display, Boolean active) {
        this.id = id;
        this.code = code;
        this.display = display;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueSetCode that = (ValueSetCode) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (display != null ? !display.equals(that.display) : that.display != null) return false;
        return active != null ? active.equals(that.active) : that.active == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (display != null ? display.hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ValueSetCode{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", display='" + display + '\'' +
            ", active=" + active +
            '}';
    }
}
