package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.request.SummaryFeedbackReq;
import com.vpgh.dms.model.dto.response.SummaryFeedbackDocumentStatsRes;
import com.vpgh.dms.model.dto.response.SummaryFeedbackModelStatsRes;
import com.vpgh.dms.model.dto.response.SummaryFeedbackRes;
import com.vpgh.dms.model.entity.SummaryFeedback;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.service.SummaryFeedbackService;
import com.vpgh.dms.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secure")
public class SummaryFeedbackController {

    private final SummaryFeedbackService summaryFeedbackService;

    public SummaryFeedbackController(SummaryFeedbackService summaryFeedbackService) {
        this.summaryFeedbackService = summaryFeedbackService;
    }

    @PostMapping("/documents/{id}/summary-feedback")
    public ResponseEntity<SummaryFeedbackRes> submitFeedback(
            @PathVariable Integer id,
            @Valid @RequestBody SummaryFeedbackReq body) {
        User currentUser = SecurityUtil.getCurrentUserFromThreadLocal();
        SummaryFeedback feedback = summaryFeedbackService.submitFeedback(id, currentUser.getId(), body);

        SummaryFeedbackRes response = new SummaryFeedbackRes();
        response.setId(feedback.getId());
        response.setDocumentId(id);
        response.setIsHelpful(feedback.getIsHelpful());
        response.setComment(feedback.getComment());
        response.setModelName(feedback.getModelName());
        response.setPromptVersion(feedback.getPromptVersion());
        response.setCreatedAt(feedback.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/documents/{id}/summary-feedback")
    public ResponseEntity<SummaryFeedbackDocumentStatsRes> getFeedbackForDocument(@PathVariable Integer id) {
        SummaryFeedbackDocumentStatsRes stats = summaryFeedbackService.getFeedbackStats(id);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/documents/summary-feedback/stats")
    public ResponseEntity<List<SummaryFeedbackModelStatsRes>> getModelFeedbackStats() {
        List<SummaryFeedbackModelStatsRes> stats = summaryFeedbackService.getModelFeedbackStats();
        return ResponseEntity.ok(stats);
    }
}
