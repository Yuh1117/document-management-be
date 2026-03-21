package com.vpgh.dms.util.exception;

public class FileException extends RuntimeException implements LocalizableException {

    private final String messageCode;
    private final Object[] messageArgs;

    public FileException(String messageCode, Object... messageArgs) {
        super(messageCode);
        this.messageCode = messageCode;
        this.messageArgs = messageArgs != null ? messageArgs : new Object[0];
    }

    @Override
    public String getMessageCode() {
        return messageCode;
    }

    @Override
    public Object[] getMessageArgs() {
        return messageArgs;
    }
}
