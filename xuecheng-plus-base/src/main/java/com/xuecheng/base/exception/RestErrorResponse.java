package com.xuecheng.base.exception;

import java.io.Serializable;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-02 09:24
 **/
public class RestErrorResponse implements Serializable {
    private String errMsg;


    public RestErrorResponse(String errMsg){

        this.errMsg=errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
