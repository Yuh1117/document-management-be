package com.vpgh.dms.service;

import com.vpgh.dms.model.dto.response.FileItemDTO;
import com.vpgh.dms.model.dto.response.PaginationResDTO;
import com.vpgh.dms.model.entity.User;

import java.util.List;
import java.util.Map;

public interface FileService {
    PaginationResDTO<List<FileItemDTO>> getUserFiles(User user, Integer parentId, Map<String, String> params);
}
