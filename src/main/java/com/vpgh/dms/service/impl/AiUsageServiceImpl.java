package com.vpgh.dms.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vpgh.dms.model.dto.response.AiUsageRes;
import com.vpgh.dms.model.dto.response.AiUsageRes.DailyRow;
import com.vpgh.dms.model.dto.response.AiUsageRes.QuestionRow;
import com.vpgh.dms.model.dto.response.AiUsageRes.UserRow;
import com.vpgh.dms.repository.ChatMessageRepository;
import com.vpgh.dms.service.AiUsageService;

@Service
public class AiUsageServiceImpl implements AiUsageService {

    private final ChatMessageRepository messageRepository;

    public AiUsageServiceImpl(ChatMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AiUsageRes getUsage(int days) {
        ZoneId zone = ZoneId.systemDefault();
        Instant since = LocalDate.now(zone).minusDays(days - 1L).atStartOfDay(zone).toInstant();

        List<DailyRow> daily = messageRepository.sumUsageByDayAndModel(zone.getId(), since).stream()
                .map(r -> new DailyRow(r.getDay(), r.getModel(), r.getAnswers(), r.getInputTokens(),
                        r.getCachedTokens(), r.getOutputTokens(), r.getLlmCalls(), r.getToolCalls()))
                .toList();

        List<QuestionRow> questions = messageRepository.countQuestionsByDay(zone.getId(), since).stream()
                .map(r -> new QuestionRow(r.getDay(), r.getQuestions()))
                .toList();

        List<UserRow> users = messageRepository.sumUsageByUserAndModel(since).stream()
                .map(r -> new UserRow(r.getUserId(), r.getEmail(), r.getName(), r.getModel(), r.getAnswers(),
                        r.getInputTokens(), r.getCachedTokens(), r.getOutputTokens()))
                .toList();

        return new AiUsageRes(days, daily, questions, users);
    }
}
