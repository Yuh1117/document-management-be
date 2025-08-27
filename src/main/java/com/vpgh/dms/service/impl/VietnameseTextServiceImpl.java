package com.vpgh.dms.service.impl;

import com.vpgh.dms.service.VietnameseTextService;
import org.springframework.stereotype.Service;
import vn.pipeline.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VietnameseTextServiceImpl implements VietnameseTextService {
    private final VnCoreNLP pipeline;
    private final Set<String> stopWords;

    public VietnameseTextServiceImpl() throws IOException {
        String[] annotators = {"wseg"};
        this.pipeline = new VnCoreNLP(annotators);
        this.stopWords = loadStopWords("stopwords/vietnamese-stopwords.txt");
    }

    @Override
    public String tokenize(String text) {
        if (text == null || text.isBlank()) return "";

        Annotation annotation = new Annotation(text);
        try {
            pipeline.annotate(annotation);

            StringBuilder sb = new StringBuilder();
            for (Sentence sentence : annotation.getSentences()) {
                for (Word word : sentence.getWords()) {
                    sb.append(word.getForm()).append(" ");
                }
            }
            return sb.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return text;
        }
    }

    @Override
    public String tokenizeAndClean(String text) {
        String tokenized = tokenize(text);
        if (tokenized.isEmpty()) return "";

        return Arrays.stream(tokenized.split("\\s+"))
                .map(String::toLowerCase)
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.joining(" "));
    }

    private Set<String> loadStopWords(String resourcePath) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) throw new FileNotFoundException("Stop word file not found: " + resourcePath);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        }
    }
}
