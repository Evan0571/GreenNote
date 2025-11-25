package com.evan.framework.common.response;

import com.evan.framework.common.exception.BaseExceptionInterface;
import com.evan.framework.common.exception.BizException;
import java.io.Serializable;

public class Response<T> implements Serializable {
    private boolean success=true;
    private String message;
    private String errorCode;
    private T data;
    public Response() {}
    public boolean isSuccess() {return success;}
    public void setSuccess(boolean success) {this.success = success;}
    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}
    public String getErrorCode() {return errorCode;}
    public void setErrorCode(String errorCode) {this.errorCode = errorCode;}
    public T getData() {return data;}
    public void setData(T data) {this.data = data;}
    //=======成功响应=======
    public static <T> Response<T> success() {
        Response<T> response=new Response<T>();
        return response;
    }
    public static <T> Response<T> success(T data) {
        Response<T> response=new Response<T>();
        response.setData(data);
        return response;
    }
    //=======失败响应=======
    public static <T> Response<T> fail() {
        Response<T> response=new Response<T>();
        response.setSuccess(false);
        return response;
    }
    public static <T> Response<T> fail(String errorMessage) {
        Response<T> response=new Response<T>();
        response.setSuccess(false);
        response.setMessage(errorMessage);
        return response;
    }
    public static <T> Response<T> fail(String errorCode,String errorMessage) {
        Response<T> response=new Response<T>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(errorMessage);
        return response;
    }
    public static <T> Response <T> fail(BizException bizException){
        Response<T> response=new Response<T>();
        response.setSuccess(false);
        response.setErrorCode(bizException.getErrorCode());
        response.setMessage(bizException.getErrorMessage());
        return response;
    }
    public static <T> Response<T> fail(BaseExceptionInterface baseExceptionInterface) {
        Response<T> response=new Response<T>();
        response.setSuccess(false);
        response.setErrorCode(baseExceptionInterface.getErrorCode());
        response.setMessage(baseExceptionInterface.getErrorMessage());
        return response;
    }
}
