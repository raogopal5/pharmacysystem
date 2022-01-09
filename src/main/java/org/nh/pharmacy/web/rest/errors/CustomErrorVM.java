package org.nh.pharmacy.web.rest.errors;

import java.util.List;

/**
 * Created by vagrant on 6/21/17.
 */
public class CustomErrorVM extends ErrorVM {

    private List<ErrorMessage> errorMessages;

    public CustomErrorVM( String message, String description, List<ErrorMessage> errorMessages){
        super(message, description);
        this.errorMessages = errorMessages;
    }

    public List<ErrorMessage> getErrorMessages(){
        return errorMessages;
    }
}
