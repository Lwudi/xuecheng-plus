package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.xml.bind.ValidationException;
import java.util.List;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-02 09:17
 **/
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler  {
    //对自定义异常的处理
    @ExceptionHandler(XueChengException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrorResponse docustomException(XueChengException e){
            log.error("【系统异常】{}",e.getErrMessage(),e);

        String errMessage = e.getErrMessage();

        return new RestErrorResponse(errMessage);
    }

    //对其他异常的处理
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody

    public RestErrorResponse doException(Exception e){
        log.error("【系统异常】{}",e.getMessage(),e);
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

    //对JSR303校验工具校验失败异常的处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuffer errMsg = new StringBuffer();
        fieldErrors.forEach(error->{
            errMsg.append(error.getDefaultMessage()).append(",");
        });
        log.error("【系统异常】{}",errMsg.toString());
        return new RestErrorResponse(errMsg.toString());


    }
}
