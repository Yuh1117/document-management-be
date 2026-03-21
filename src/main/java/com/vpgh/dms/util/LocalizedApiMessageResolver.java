package com.vpgh.dms.util;

import com.vpgh.dms.util.annotation.ApiMessage;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.Locale;

@Component
public class LocalizedApiMessageResolver {

    private static final String RESULT_SUCCESS = "api.result.success";
    private static final String RESULT_FAILURE = "api.result.failure";
    private static final String ERROR_INTERNAL = "api.error.internal";

    private final MessageSource messageSource;

    public LocalizedApiMessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getSuccessMessage(ApiMessage apiMessage) {
        return formatWithResult(apiMessage, RESULT_SUCCESS);
    }

    public String getFailedMessage(ApiMessage apiMessage) {
        return formatWithResult(apiMessage, RESULT_FAILURE);
    }

    public String getInternalServerErrorMessage() {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(ERROR_INTERNAL, null, "Internal server error", locale);
    }

    private String formatWithResult(ApiMessage apiMessage, String resultKey) {
        if (apiMessage == null) {
            return messageSource.getMessage("api.message.empty", null, "hehe", LocaleContextHolder.getLocale());
        }
        Locale locale = LocaleContextHolder.getLocale();
        String action = resolveAction(apiMessage, locale);
        String pattern = messageSource.getMessage(resultKey, null, locale);
        return MessageFormat.format(pattern, action);
    }

    private String resolveAction(ApiMessage apiMessage, Locale locale) {
        if (StringUtils.hasText(apiMessage.key())) {
            return messageSource.getMessage(apiMessage.key(), null, apiMessage.message(), locale);
        }
        return apiMessage.message() != null ? apiMessage.message() : "";
    }
}
