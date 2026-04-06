package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.processor.ProcessorSummarizeRequest;
import com.vpgh.dms.model.dto.processor.ProcessorSummarizeResponse;

public interface ProcessorSummarizeService {

    ProcessorSummarizeResponse summarize(ProcessorSummarizeRequest request);
}
