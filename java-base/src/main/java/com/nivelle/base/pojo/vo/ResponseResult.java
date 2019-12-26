package com.nivelle.base.pojo.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ResponseResult<T> implements Serializable {

    /**
     * 成功：0 失败：其它
     */
    private int code = 0 ;
    /**
     * 成功："ok" 失败：其它
     */
    private String msg = "success";
    /**
     * 数据体
     */
    private T body;

    public static  ResponseResult newResponseResult(){
        return new ResponseResult();
    }

    public ResponseResult addData(T body) {
        this.body = body;
        return this;
    }

    public ResponseResult setSuccess(T body) {
        this.code = 0;
        this.body = body;
        return this;
    }

    public ResponseResult<T> setFail(int code, String msg) {
        this.code = code;
        this.msg = msg;
        return this;
    }

}
