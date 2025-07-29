package com.vpgh.dms.controller;

import com.vpgh.dms.model.dto.request.FileUploadReq;
import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.service.DocumentService;
import com.vpgh.dms.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @PostMapping(path = "/secure/documents/upload")
    @ApiMessage(message = "Upload tài liệu")
    public ResponseEntity<Document> upload(@Valid @ModelAttribute FileUploadReq fileUploadReq) throws IOException {
        Document doc = this.documentService.uploadFile(fileUploadReq.getFile());
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }


    @GetMapping(path = "/secure/documents/download/{fileName}")
    public ResponseEntity<byte[]> download(@PathVariable(value = "fileName") String fileName) {
        byte[] data = this.documentService.downloadFile(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename= " + fileName)
                .body(data);
    }
}
