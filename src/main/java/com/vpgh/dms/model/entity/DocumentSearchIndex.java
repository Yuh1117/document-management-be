package com.vpgh.dms.model.entity;

import com.vpgh.dms.model.TimestampedEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "document_search_index")
public class DocumentSearchIndex extends TimestampedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String keywords;

    @Column(columnDefinition = "vector(384)")
    private float[] contentVector;

    @Column(columnDefinition = "tsvector", insertable = false, updatable = false)
    private String keywordsTsv;

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

    public float[] getContentVector() {
        return contentVector;
    }

    public void setContentVector(float[] contentVector) {
        this.contentVector = contentVector;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getKeywordsTsv() {
        return keywordsTsv;
    }

    public void setKeywordsTsv(String keywordsTsv) {
        this.keywordsTsv = keywordsTsv;
    }
}
