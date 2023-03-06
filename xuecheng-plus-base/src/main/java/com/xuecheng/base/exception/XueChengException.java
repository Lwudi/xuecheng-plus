package com.xuecheng.base.exception;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-01 20:30
 **/
public class XueChengException extends RuntimeException {

    private String errMessage;

    public XueChengException(){
        super();
    }
    public XueChengException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }
    public String getErrMessage() {
        return errMessage;
    }

    public static void err(CommonError commonError){
        throw new XueChengException(commonError.getErrMessage());
    }
    public static void err(String errMessage){
        throw new XueChengException(errMessage);
    }

}
