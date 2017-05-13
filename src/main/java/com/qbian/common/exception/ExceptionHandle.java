package com.qbian.common.exception;

import com.qbian.common.dto.ResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Qbian on 2017/5/12.
 */
@ControllerAdvice
public class ExceptionHandle {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandle.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseDto handlerServiceException(Exception e) {
        LOG.error("[系统错误]", e);

        return new ResponseDto(-999, e.getMessage());
    }

}
