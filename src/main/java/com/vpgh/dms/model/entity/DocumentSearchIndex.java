package com.vpgh.dms.model.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "document_search_index")
public class DocumentSearchIndex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String keywords;
    @Column(columnDefinition = "JSON")
    private String contentVector;

    private Instant createdAt;
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getContentVector() {
        return contentVector;
    }

    public void setContentVector(String contentVector) {
        this.contentVector = contentVector;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
