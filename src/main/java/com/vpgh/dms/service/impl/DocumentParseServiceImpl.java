package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentSearchIndex;
import com.vpgh.dms.repository.DocumentRepository;
import com.vpgh.dms.repository.DocumentSearchIndexRepository;
import com.vpgh.dms.service.DocumentParseService;
import com.vpgh.dms.service.EmbeddingService;
import com.vpgh.dms.service.VietnameseTextService;
import net.sourceforge.tess4j.Tesseract;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;


@Service
public class DocumentParseServiceImpl implements DocumentParseService {
    private final DocumentRepository documentRepository;
    private final DocumentSearchIndexRepository indexRepository;
    private final VietnameseTextService vnProcessor;
    private final EmbeddingService embeddingService;

    private static final Logger log = LoggerFactory.getLogger(DocumentParseServiceImpl.class);
    private static final List<String> SUPPORTED_TIKA_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    @Value("${tesseract.path}")
    private String tesseractPath;

    public DocumentParseServiceImpl(DocumentRepository documentRepository, DocumentSearchIndexRepository indexRepository,
                                    VietnameseTextService vnProcessor, EmbeddingService embeddingService) {
        this.documentRepository = documentRepository;
        this.indexRepository = indexRepository;
        this.vnProcessor = vnProcessor;
        this.embeddingService = embeddingService;
    }

    @Async
    @Override
    public void parseAndIndexAsync(Document doc, File file) {
        try {
            String extracted = extractContent(file, doc.getMimeType());
            if (extracted == null || extracted.isBlank()) return;
            doc.setExtractedText(extracted);
            this.documentRepository.save(doc);

            DocumentSearchIndex idx = this.indexRepository.findByDocument(doc);
            if (idx == null) {
                idx = new DocumentSearchIndex();
                idx.setDocument(doc);
            }

            String vnTokens = this.vnProcessor.tokenizeAndClean(extracted);
//            String enTokens = cleanStopWords(extracted);
            idx.setKeywords(vnTokens);

            List<Double> embedding = embeddingService.getEmbedding(extracted);
            float[] embeddingArray = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                embeddingArray[i] = embedding.get(i).floatValue();
            }
            idx.setContentVector(embeddingArray);
            this.indexRepository.save(idx);

            log.info("Parsed and indexed document {}", doc.getId());
        } catch (Exception e) {
            log.error("Parse failed for document {}: {}", doc.getId(), e.getMessage(), e);
            this.documentRepository.save(doc);
        } finally {
            file.delete();
        }
    }

    private String extractContent(File file, String mimeType) throws Exception {
        if (mimeType != null && SUPPORTED_TIKA_TYPES.contains(mimeType)) {
            log.debug("Parsing text with Apache Tika for file {}", file.getName());
            Tika tika = new Tika();
            return tika.parseToString(file);
        } else if (mimeType != null && mimeType.startsWith("image")) {
            log.debug("Running OCR with Tesseract for file {}", file.getName());
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tesseractPath);
            tesseract.setLanguage("vie+eng");

            String rawOcrResult = tesseract.doOCR(file);

            String cleanedResult = rawOcrResult.trim().replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
            if (cleanedResult.isEmpty() || cleanedResult.matches("^\\W*$")) {
                cleanedResult = "";
            }
            return cleanedResult;
        }
        log.warn("Unsupported mime type {} for file {}", mimeType, file.getName());
        return "";
    }

    private String cleanStopWords(String text) {
        if (text == null || text.isBlank()) return "";

        try (Analyzer analyzer = new StandardAnalyzer()) {
            TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(text));
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            StringBuilder sb = new StringBuilder();
            while (tokenStream.incrementToken()) {
                sb.append(attr.toString()).append(" ");
            }
            tokenStream.end();
            tokenStream.close();

            return sb.toString().trim();
        } catch (IOException e) {
            log.error("Error while cleaning stop words: {}", e.getMessage(), e);
            return text;
        }
    }
}
