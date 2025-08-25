package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Document;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentParseService {
    void parseAndIndexAsync(Document doc, MultipartFile file);
}
