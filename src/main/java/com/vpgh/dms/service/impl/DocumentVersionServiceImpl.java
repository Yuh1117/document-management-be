package com.vpgh.dms.service.impl;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.DocumentVersion;
import com.vpgh.dms.repository.DocumentVersionRepository;
import com.vpgh.dms.service.DocumentVersionService;
import com.vpgh.dms.service.specification.DocumentVersionSpecification;
import com.vpgh.dms.util.PageSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DocumentVersionServiceImpl implements DocumentVersionService {
    private final DocumentVersionRepository documentVersionRepository;

    public DocumentVersionServiceImpl(DocumentVersionRepository documentVersionRepository) {
        this.documentVersionRepository = documentVersionRepository;
    }

    @Override
    public Page<DocumentVersion> getVersionsByDocument(Map<String, String> params, Document doc) {
        Specification<DocumentVersion> combinedSpec = Specification.allOf();
        Pageable pageable = Pageable.unpaged();

        if (params != null) {
            int page = Integer.parseInt(params.get("page"));
            pageable = PageRequest.of(page - 1, PageSize.DOCUMENT_PAGE_SIZE.getSize(),
                    Sort.by(Sort.Order.desc("id")));
        }
        combinedSpec = combinedSpec.and(DocumentVersionSpecification.hasDocument(doc));
        return this.documentVersionRepository.findAll(combinedSpec, pageable);
    }

    @Override
    public DocumentVersion getVersionById(Integer id) {
        return this.documentVersionRepository.findById(id).orElse(null);
    }
}
