package org.nh.pharmacy.domain;

import java.io.Serializable;

public class GroupMember implements Serializable {

    private Object member;

    private boolean inactive;

    public GroupMember() {
    }

    public GroupMember(Object member, boolean inactive) {
        this.member = member;
        this.inactive = inactive;
    }

    public Object getMember() {
        return member;
    }

    public void setMember(Object member) {
        this.member = member;
    }

    public boolean isInactive() {
        return inactive;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupMember member1 = (GroupMember) o;

        return member.equals(member1.member);
    }

    @Override
    public int hashCode() {
        return member.hashCode();
    }

    @Override
    public String toString() {
        return "GroupMember{" +
            "member=" + member +
            ", inactive=" + inactive +
            '}';
    }
}
