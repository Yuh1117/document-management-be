package com.vpgh.dms.service.specification;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentVersion;
import org.springframework.data.jpa.domain.Specification;

public class DocumentVersionSpecification {
    public static Specification<DocumentVersion> hasDocument(Document doc) {
        return (root, query, cb) -> cb.equal(root.get("document"), doc);
    }
}

