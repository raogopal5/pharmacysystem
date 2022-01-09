package org.nh.pharmacy.exception;

import org.nh.pharmacy.web.rest.errors.ErrorMessage;

import java.util.List;

/**
 * Created by Nitesh on 6/19/17.
 */
public class FieldValidationException extends RuntimeException {

    private List<ErrorMessage> errorMessages;
    public FieldValidationException(String message){ super(message);}

    public FieldValidationException(List<ErrorMessage> errorMessages, String message){
        super(message);
        this.errorMessages = errorMessages;
    }
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public List<ErrorMessage> getErrorMessages(){ return errorMessages;}

}
