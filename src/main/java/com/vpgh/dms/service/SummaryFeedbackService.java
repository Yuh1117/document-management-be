package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.request.SummaryFeedbackReq;
import com.vpgh.dms.model.dto.response.SummaryFeedbackDocumentStatsRes;
import com.vpgh.dms.model.dto.response.SummaryFeedbackModelStatsRes;
import com.vpgh.dms.model.entity.SummaryFeedback;

import java.util.List;

public interface SummaryFeedbackService {

    SummaryFeedback submitFeedback(Integer documentId, Integer userId, SummaryFeedbackReq req);

    SummaryFeedbackDocumentStatsRes getFeedbackStats(Integer documentId);

    List<SummaryFeedbackModelStatsRes> getModelFeedbackStats();
}
