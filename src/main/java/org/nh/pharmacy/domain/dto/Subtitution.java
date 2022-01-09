package org.nh.pharmacy.domain.dto;

import java.io.Serializable;

/**
 * Created by Indrajeet 11/30/2017
 */
public class Subtitution implements Serializable {

    private Boolean allowed;
    private String reason;

    public Boolean getAllowed() {
        return allowed;
    }

    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subtitution that = (Subtitution) o;

        if (allowed != null ? !allowed.equals(that.allowed) : that.allowed != null) return false;
        return reason != null ? reason.equals(that.reason) : that.reason == null;
    }

    @Override
    public int hashCode() {
        int result = allowed != null ? allowed.hashCode() : 0;
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Subtitution{" +
            "allowed=" + allowed +
            ", reason='" + reason + '\'' +
            '}';
    }
}
