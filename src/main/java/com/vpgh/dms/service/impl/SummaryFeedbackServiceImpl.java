package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.dto.request.SummaryFeedbackReq;
import com.vpgh.dms.model.dto.response.SummaryFeedbackDocumentStatsRes;
import com.vpgh.dms.model.dto.response.SummaryFeedbackModelStatsRes;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.SummaryFeedback;
import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.repository.SummaryFeedbackRepository;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.service.SummaryFeedbackService;
import com.vpgh.dms.service.UserService;
import com.vpgh.dms.util.exception.ForbiddenException;
import com.vpgh.dms.util.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SummaryFeedbackServiceImpl implements SummaryFeedbackService {

    private final SummaryFeedbackRepository feedbackRepository;
    private final DocumentRepository documentRepository;
    private final UserService userService;
    private final DocumentService documentService;

    public SummaryFeedbackServiceImpl(SummaryFeedbackRepository feedbackRepository,
            DocumentRepository documentRepository,
            UserService userService,
            DocumentService documentService) {
        this.feedbackRepository = feedbackRepository;
        this.documentRepository = documentRepository;
        this.userService = userService;
        this.documentService = documentService;
    }

    @Override
    @Transactional
    public SummaryFeedback submitFeedback(Integer documentId, Integer userId, SummaryFeedbackReq req) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("error.document.notFoundOrDeleted"));
        if (Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("error.document.notFoundOrDeleted");
        }
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("error.user.notFound");
        }
        if (!documentService.isOwnerDocument(doc, user)) {
            throw new ForbiddenException("error.document.forbidden");
        }

        Optional<SummaryFeedback> existing = feedbackRepository.findByDocumentAndUser(doc, user);
        SummaryFeedback feedback;
        if (existing.isPresent()) {
            feedback = existing.get();
        } else {
            feedback = new SummaryFeedback();
            feedback.setDocument(doc);
            feedback.setUser(user);
        }
        feedback.setIsHelpful(req.getIsHelpful());
        feedback.setComment(req.getComment());
        feedback.setModelName(doc.getModelName());
        feedback.setPromptVersion(doc.getPromptVersion());

        return feedbackRepository.save(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public SummaryFeedbackDocumentStatsRes getFeedbackStats(Integer documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("error.document.notFoundOrDeleted"));
        if (Boolean.TRUE.equals(doc.getDeleted())) {
            throw new NotFoundException("error.document.notFoundOrDeleted");
        }

        long helpful = feedbackRepository.countHelpfulByDocumentId(documentId);
        long notHelpful = feedbackRepository.countNotHelpfulByDocumentId(documentId);
        SummaryFeedbackDocumentStatsRes stats = new SummaryFeedbackDocumentStatsRes();
        stats.setDocumentId(documentId);
        stats.setHelpfulCount(helpful);
        stats.setNotHelpfulCount(notHelpful);
        stats.setTotalCount(helpful + notHelpful);
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SummaryFeedbackModelStatsRes> getModelFeedbackStats() {
        List<String> names = feedbackRepository.findDistinctModelNames();
        List<SummaryFeedbackModelStatsRes> rows = new ArrayList<>();
        for (String mv : names) {
            if (mv == null) {
                continue;
            }
            long helpful = feedbackRepository.countHelpfulByModelName(mv);
            long notHelpful = feedbackRepository.countNotHelpfulByModelName(mv);
            SummaryFeedbackModelStatsRes row = new SummaryFeedbackModelStatsRes();
            row.setModelName(mv);
            row.setTotalCount(helpful + notHelpful);
            row.setHelpfulCount(helpful);
            row.setNotHelpfulCount(notHelpful);
            rows.add(row);
        }
        return rows;
    }
}
