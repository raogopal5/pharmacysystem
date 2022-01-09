package org.nh.pharmacy.web.rest.errors;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class BadRequestException extends  AbstractThrowableProblem {
    public BadRequestException(String message){
        super(ErrorConstants.DEFAULT_TYPE, message, Status.BAD_REQUEST);
    }
}
