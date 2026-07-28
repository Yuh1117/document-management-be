package com.vpgh.dms.service;
import java.util.UUID;

import com.vpgh.dms.model.dto.request.SummaryFeedbackReq;
import com.vpgh.dms.model.dto.response.SummaryFeedbackDocumentStatsRes;
import com.vpgh.dms.model.dto.response.SummaryFeedbackModelStatsRes;
import com.vpgh.dms.model.entity.SummaryFeedback;

import java.util.List;

public interface SummaryFeedbackService {

    SummaryFeedback submitFeedback(UUID documentId, UUID userId, SummaryFeedbackReq req);

    SummaryFeedbackDocumentStatsRes getFeedbackStats(UUID documentId);

    List<SummaryFeedbackModelStatsRes> getModelFeedbackStats();
}