package com.vpgh.dms.model.dto.response;

import com.vpgh.dms.model.entity.Folder;

import java.util.List;

public record FolderUploadPlan(Folder rootFolder, List<Folder> targetFolders) {
}
