package com.vpgh.dms.util.exception;

public interface LocalizableException {

    String getMessageCode();

    Object[] getMessageArgs();
}
