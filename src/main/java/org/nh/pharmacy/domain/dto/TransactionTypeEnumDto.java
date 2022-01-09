package org.nh.pharmacy.domain.dto;

import java.io.Serializable;
import java.util.Comparator;

public class TransactionTypeEnumDto implements Comparable<TransactionTypeEnumDto>, Serializable {

    private String name;
    private String displayName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public static Comparator<TransactionTypeEnumDto> getDisplayNameComparator() {
        return displayNameComparator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionTypeEnumDto that = (TransactionTypeEnumDto) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return displayName != null ? displayName.equals(that.displayName) : that.displayName == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TransactionTypeEnumDto{" +
            "name='" + name + '\'' +
            ", displayName='" + displayName + '\'' +
            '}';
    }

    public static final Comparator<TransactionTypeEnumDto> displayNameComparator = new Comparator<TransactionTypeEnumDto>() {
        @Override
        public int compare(TransactionTypeEnumDto o1, TransactionTypeEnumDto o2) {
            return o1.displayName.compareTo(o2.displayName);
        }
    };

    @Override
    public int compareTo(TransactionTypeEnumDto o) {
        return this.name.compareToIgnoreCase(o.name);
    }
}
