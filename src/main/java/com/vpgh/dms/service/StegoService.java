package com.vpgh.dms.service;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public interface StegoService {
    ByteArrayOutputStream hideData(InputStream input, String content, String password) throws Exception;

    String extractData(InputStream input, String password) throws Exception;
}
