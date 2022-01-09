package org.nh.pharmacy.domain.dto;

import java.io.Serializable;

public class ItemType implements Serializable {

    private Long id;
    private String code;
    private String display;

    public ItemType() {
    }

    public ItemType(Long id, String code, String display) {
        this.id = id;
        this.code = code;
        this.display = display;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemType itemType = (ItemType) o;

        if (id != null ? !id.equals(itemType.id) : itemType.id != null) return false;
        if (code != null ? !code.equals(itemType.code) : itemType.code != null) return false;
        return display != null ? display.equals(itemType.display) : itemType.display == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (display != null ? display.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ItemType{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", display='" + display + '\'' +
            '}';
    }
}
