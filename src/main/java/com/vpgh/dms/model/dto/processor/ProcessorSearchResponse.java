package com.vpgh.dms.model.dto.processor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProcessorSearchResponse {

    private List<ProcessorSearchHit> hits;
    private int total;
    private int page;
    @JsonProperty("page_size")
    private int pageSize;

    public List<ProcessorSearchHit> getHits() {
        return hits;
    }

    public void setHits(List<ProcessorSearchHit> hits) {
        this.hits = hits;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public static class ProcessorSearchHit {
        @JsonProperty("document_id")
        private String documentId;
        private double score;

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }
}
