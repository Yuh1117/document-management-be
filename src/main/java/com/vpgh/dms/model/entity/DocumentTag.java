package com.vpgh.dms.model.entity;

import com.vpgh.dms.model.FullAuditableEntity;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "document_tags")
public class DocumentTag extends FullAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String color;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
    private Set<Document> documents;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Set<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Document> documents) {
        this.documents = documents;
    }

}
