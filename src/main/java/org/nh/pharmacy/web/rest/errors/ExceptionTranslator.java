package org.nh.pharmacy.web.rest.errors;

import io.github.jhipster.web.util.HeaderUtil;

import org.hibernate.exception.ConstraintViolationException;
import org.jbpm.services.api.TaskNotFoundException;
import org.nh.jbpm.exception.BusinessRuleViolationException;
import org.nh.pharmacy.domain.SystemAlert;
import org.nh.pharmacy.exception.FieldValidationException;
import org.nh.pharmacy.exception.StockException;
import org.nh.pharmacy.service.SystemAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;
import org.zalando.problem.violations.ConstraintViolationProblem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807).
 */
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling, SecurityAdviceTrait {

    private static final String FIELD_ERRORS_KEY = "fieldErrors";
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final String VIOLATIONS_KEY = "violations";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final Logger log = LoggerFactory.getLogger(ExceptionTranslator.class);

    @Autowired
    SystemAlertService systemAlertService;


    /**
     * Post-process the Problem payload to add the message key for the front-end if needed.
     */
    @Override
    public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity, NativeWebRequest request) {
        if (entity == null) {
            return entity;
        }
        Problem problem = entity.getBody();
        if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
            return entity;
        }
        ProblemBuilder builder = Problem.builder()
            .withType(Problem.DEFAULT_TYPE.equals(problem.getType()) ? ErrorConstants.DEFAULT_TYPE : problem.getType())
            .withStatus(problem.getStatus())
            .withTitle(problem.getTitle())
            .with(PATH_KEY, request.getNativeRequest(HttpServletRequest.class).getRequestURI());

        if (problem instanceof ConstraintViolationProblem) {
            builder
                .with(VIOLATIONS_KEY, ((ConstraintViolationProblem) problem).getViolations())
                .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION);
        } else {
            builder
                .withCause(((DefaultProblem) problem).getCause())
                .withDetail(problem.getDetail())
                .withInstance(problem.getInstance());
            problem.getParameters().forEach(builder::with);
            if (!problem.getParameters().containsKey(MESSAGE_KEY) && problem.getStatus() != null) {
                builder.with(MESSAGE_KEY, "error.http." + problem.getStatus().getStatusCode());
            }
        }
        return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @Nonnull NativeWebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldErrorVM> fieldErrors = result.getFieldErrors().stream()
            .map(f -> new FieldErrorVM(f.getObjectName().replaceFirst("DTO$", ""), f.getField(), f.getCode()))
            .collect(Collectors.toList());

        Problem problem = Problem.builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Method argument not valid")
            .withStatus(defaultConstraintViolationStatus())
            .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .with(FIELD_ERRORS_KEY, fieldErrors)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleNoSuchElementException(NoSuchElementException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.NOT_FOUND)
            .with(MESSAGE_KEY, ErrorConstants.ENTITY_NOT_FOUND_TYPE)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleBadRequestAlertException(BadRequestAlertException ex, NativeWebRequest request) {
        return create(ex, request, HeaderUtil.createFailureAlert(applicationName, false, ex.getEntityName(), ex.getErrorKey(), ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleConcurrencyFailure(ConcurrencyFailureException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.CONFLICT)
            .with(MESSAGE_KEY, ErrorConstants.ERR_CONCURRENCY_FAILURE)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Problem> handleConstraintViolationException(DataIntegrityViolationException ex, NativeWebRequest request) {
        Throwable cause = ex.getRootCause();
        saveErroMessageInDatabase(ex);

        if (cause instanceof ConstraintViolationException) {
            Problem problem = Problem.builder()
                .withStatus(Status.CONFLICT)
                .with("message", ErrorConstants.ERR_CONCURRENCY_FAILURE)
                .build();
            return create(ex, problem, request);
        } else {
            Problem problem = Problem.builder()
                .withStatus(Status.INTERNAL_SERVER_ERROR)
                .with("message", ErrorConstants.ERR_INTERNAL_SERVER_ERROR)
                .build();
            return create(ex, problem, request);
        }
    }

    @ExceptionHandler(StockException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorVM stockException(StockException exception) {
        List<ErrorMessage> errorMessages = new ArrayList<>(3);
        Map<String, Object> params = new HashMap<>();
        params.put("itemName", exception.getItemName());
        params.put("batchNo", exception.getBatchNo());
        params.put("stockId", exception.getStockId());
        params.put("itemId", exception.getItemId());
        params.put("storeId", exception.getStoreId());
        params.put("availableQuantity", exception.getAvailableQuantity());
        params.put("requestedQuantity", exception.getRequestQuantity());
        params.put("storeName", exception.getStoreName());
        errorMessages.add(new ErrorMessage(exception.getErrorCode(), params));
        return new CustomErrorVM(ErrorConstants.ERR_INTERNAL_SERVER_ERROR, exception.getMessage(),
            errorMessages);
    }

    @ExceptionHandler(FieldValidationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorVM ValidationException(FieldValidationException exception) {
        saveErroMessageInDatabase(exception);
        return new CustomErrorVM(ErrorConstants.ERR_VALIDATION, exception.getMessage(), exception.getErrorMessages());
    }


    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseBody
    public ResponseEntity<Problem> JbpmException(TaskNotFoundException ex, NativeWebRequest request) {
        saveErroMessageInDatabase(ex);
        Problem problem = Problem.builder()
            .withStatus(Status.INTERNAL_SERVER_ERROR)
            .with("message", "error.http.500")
            .withDetail(ex.getMessage())
            .build();
        return process(create(ex, problem, request));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Problem> handleException(Exception ex, NativeWebRequest request) throws Exception{
        saveErroMessageInDatabase(ex);
        return process(create(ex, request));
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Problem> handleBadRequestException(Exception ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.BAD_REQUEST)
            .withTitle("test response status")
            .with("message","error.http.400")
            .build();
        saveErroMessageInDatabase(ex);
        return create(ex, problem, request);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Problem> handleInternalServerErrorException(Exception ex, NativeWebRequest request) {
        saveErroMessageInDatabase(ex);
        return process(create(ex, request));
    }

    public void saveErroMessageInDatabase(Exception ex) {
        try {
            log.error("AppError : ", ex);
            StringWriter stack = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stack);

            ex.printStackTrace(printWriter);
            SystemAlert alert = new SystemAlert().fromClass("Pharmacy").onDate(ZonedDateTime.now()).message(String.valueOf(ex.getMessage())).description(stack.toString());

            systemAlertService.save(alert);
        } catch (Exception e) {
            log.error("Error while saving alert", e);
        }
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomErrorVM BusinessRuleViolationException(BusinessRuleViolationException exception) {
        saveErroMessageInDatabase(exception);
        List<ErrorMessage> errorMessages = new ArrayList<>();
        String errorMsgString = exception.getMessage();
        if (!errorMsgString.isEmpty() && errorMsgString.length() >= 2) {
            errorMsgString = errorMsgString.substring(1, errorMsgString.length() - 1);//i.e.  [10176] OR [10176, 10174]
            String[] errorCodes = errorMsgString.split(",");
            for (String errorCode : errorCodes) {
                if (errorCode.trim().length() == 5) {//Exception code length is 5
                    errorMessages.add(new ErrorMessage(errorCode.trim(), null));
                }
            }
        }
        return new CustomErrorVM(ErrorConstants.ERR_VALIDATION, exception.getMessage(), errorMessages);
    }

}
