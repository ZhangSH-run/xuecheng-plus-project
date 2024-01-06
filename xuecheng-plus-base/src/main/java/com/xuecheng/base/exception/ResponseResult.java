package com.xuecheng.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult implements Serializable {
    private static final long serialVersionUID = -667710L;
    private String errCode;
    private String errMessage;
}
