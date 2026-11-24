package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.response.AiUsageRes;

public interface AiUsageService {

    /** Usage of the last {@code days} days, today included. */
    AiUsageRes getUsage(int days);
}
