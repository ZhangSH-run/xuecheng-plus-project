package com.xuecheng.base.exception;

public class XueChengPlusException extends RuntimeException{
    private String errMessage;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

    public String getErrMessage() {
        return errMessage;
    }

}
