package com.vpgh.dms.model.dto.response;

import com.vpgh.dms.model.entity.Document;

import java.util.List;

public record UploadResult(List<Document> uploaded, List<String> conflicts) {
}
