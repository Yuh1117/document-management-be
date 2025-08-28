package com.vpgh.dms.service;

import com.vpgh.dms.model.entity.Document;
import java.io.File;

public interface DocumentParseService {
    void parseAndIndexAsync(Document doc, File file);
}
