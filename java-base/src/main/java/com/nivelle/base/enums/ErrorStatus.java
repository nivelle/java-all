package com.nivelle.base.enums;

public enum ErrorStatus {


    BADREQUEST(400,"非法请求"),



    PARAMSMISS(10000,"参数缺失");

    private Integer errorCode;

    private String errorMsg;


    ErrorStatus(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
