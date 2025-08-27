package com.vpgh.dms.service;

public interface VietnameseTextService {
    String tokenize(String text);

    String tokenizeAndClean(String text);
}
