package com.vpgh.dms.model.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Breadcrumb path of a folder.
 *
 * @param root  "my-files" when the chain reaches a top-level folder owned by the current user,
 *              otherwise "shared" (the user only has access starting from a shared ancestor)
 * @param items ancestors the user can view, ordered from the top-most down to the requested folder
 */
public record FolderPathRes(String root, List<Item> items) {
    public record Item(UUID id, String name) {
    }
}
