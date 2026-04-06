package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.processor.ProcessorSearchRequest;
import com.vpgh.dms.model.dto.processor.ProcessorSearchResponse;

public interface ProcessorSearchService {

    ProcessorSearchResponse search(ProcessorSearchRequest request);
}
