package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.processor.ProcessorModelsListResponse;
import com.vpgh.dms.model.dto.processor.ProcessorReloadModelResponse;

public interface ProcessorModelService {

    ProcessorModelsListResponse listModels();

    ProcessorReloadModelResponse reloadModel();
}
