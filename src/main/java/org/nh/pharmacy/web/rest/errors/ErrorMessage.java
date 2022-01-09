package org.nh.pharmacy.web.rest.errors;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Nitesh on 6/20/17.
 */
public class ErrorMessage implements Serializable {

    private String errorCode;
    private Map<String, Object> source;

    public ErrorMessage(String errorCode){
        this.errorCode = errorCode;
    }

    public ErrorMessage(String errorCode, Map<String, Object> source){
        this.errorCode = errorCode;
        this.source = source;
    }

    public String getErrorCode() {
        return errorCode;
    }


    public Map<String, Object> getSource(){
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErrorMessage that = (ErrorMessage) o;

        if (errorCode != null ? !errorCode.equals(that.errorCode) : that.errorCode != null) return false;
        return source != null ? source.equals(that.source) : that.source == null;
    }

    @Override
    public int hashCode() {
        int result = errorCode != null ? errorCode.hashCode() : 0;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
            "errorCode='" + errorCode + '\'' +
            ", source=" + source +
            '}';
    }
}
