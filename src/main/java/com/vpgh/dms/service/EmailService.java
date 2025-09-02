package com.vpgh.dms.service;

import jakarta.mail.MessagingException;

import java.util.Map;

public interface EmailService {
    void sendHtmlEmail(String to, String subject, Map<String, Object> variables) throws MessagingException;
}
