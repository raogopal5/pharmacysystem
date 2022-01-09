package org.nh.pharmacy.domain.dto;

import java.io.Serializable;
import java.util.Comparator;

public class StatusEnumDto implements Comparable<StatusEnumDto>, Serializable {

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

    public static Comparator<StatusEnumDto> getDisplayNameComparator() {
        return displayNameComparator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatusEnumDto that = (StatusEnumDto) o;

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
        return "StatusEnumDto{" +
            "name='" + name + '\'' +
            ", displayName='" + displayName + '\'' +
            '}';
    }

    public static final Comparator<StatusEnumDto> displayNameComparator = new Comparator<StatusEnumDto>() {
        @Override
        public int compare(StatusEnumDto o1, StatusEnumDto o2) {
            return o1.displayName.compareTo(o2.displayName);
        }
    };

    @Override
    public int compareTo(StatusEnumDto o) {
        return this.name.compareToIgnoreCase(o.name);
    }
}
