package org.nh.pharmacy.domain.dto;

import java.io.Serializable;
import java.util.Comparator;

public class FlowTypeEnumDto implements Comparable<FlowTypeEnumDto>, Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlowTypeEnumDto that = (FlowTypeEnumDto) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return displayName != null ? displayName.equals(that.displayName) : that.displayName == null;
    }

    @Override
    public String toString() {
        return "FlowTypeEnumDto{" +
            "name='" + name + '\'' +
            ", displayName='" + displayName + '\'' +
            '}';
    }

    public static final Comparator<FlowTypeEnumDto> displayNameComparator = new Comparator<FlowTypeEnumDto>() {
        @Override
        public int compare(FlowTypeEnumDto o1, FlowTypeEnumDto o2) {
            return o1.displayName.compareTo(o2.displayName);
        }
    };

    @Override
    public int compareTo(FlowTypeEnumDto o) {
        return this.name.compareToIgnoreCase(o.name);
    }

}
