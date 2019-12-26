package com.nivelle.spring.configbean;

import com.nivelle.base.enums.ErrorStatus;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@ControllerAdvice
public class ControllerExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionAdvice.class);

    @ExceptionHandler(value = {MissingServletRequestParameterException.class,
            ConstraintViolationException.class, TypeMismatchException.class, Throwable.class})
    @ResponseBody
    Object handleControllerException(Exception ex) throws IOException {
        int code = 1;
        String message = "";
        if (ex != null) {
            message = ex.getMessage();
        }

        int errorCode = ErrorStatus.BADREQUEST.getErrorCode();
        // 如果请求参数为必填但没有传则抛异常
        if (ex instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException e =
                    (MissingServletRequestParameterException) ex;
            String parameterName = e.getParameterName();
            message = parameterName + " is required";
            code = ErrorStatus.PARAMSMISS.getErrorCode();
            logger.error("code={},message={}", code, message, ex);
            System.out.println(code + message + ex);

        } else if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException e = (ConstraintViolationException) ex;
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            Optional<ConstraintViolation<?>> constrain = violations.stream().findFirst();
            if (constrain.isPresent()) {
                message = constrain.get().getMessage();
                code = errorCode;
            }
            logger.error("code={},message={}", code, message, ex);
            System.out.println(code + message + ex);
        } else if (ex instanceof TypeMismatchException) {
            TypeMismatchException e = (TypeMismatchException) ex;
            message = "parameter value " + e.getValue() + " is typeMismatch";
            code = errorCode;
            logger.error("code={},message={}", code, message, ex);
            System.out.println(code + message + ex);
        } else if (ex instanceof BindException) {
            BindException e = (BindException) ex;
            message = e.getAllErrors().get(0).getDefaultMessage();
            code = errorCode;
            logger.error("code={},message={}", code, message, ex);
            System.out.println(code + message + ex);
        } else if (ex instanceof UnauthorizedException) {

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("403");

            return modelAndView;

        } else {
            message = "未知异常";
            System.err.println(message);
            System.err.println(ex.getCause());
            System.err.println(ex.getMessage());
            System.err.println(ex.getStackTrace());
            System.err.println(ex.getLocalizedMessage());



        }
        return message;
    }
}


