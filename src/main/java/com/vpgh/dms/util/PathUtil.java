package com.vpgh.dms.util;

import com.vpgh.dms.model.entity.Document;
import com.vpgh.dms.model.entity.Folder;

import java.util.ArrayDeque;
import java.util.Deque;

public class PathUtil {

    public static String buildRelativePath(Document doc, Folder rootFolder) {
        Deque<String> pathParts = new ArrayDeque<>();
        pathParts.addFirst(doc.getName());

        Folder current = doc.getFolder();
        while (current != null && !current.getId().equals(rootFolder.getId())) {
            pathParts.addFirst(current.getName());
            current = current.getParent();
        }

        pathParts.addFirst(rootFolder.getName());
        return String.join("/", pathParts);
    }

    public static String buildRelativePath(Folder folder, Folder rootFolder) {
        Deque<String> pathParts = new ArrayDeque<>();

        Folder current = folder;
        while (current != null && !current.getId().equals(rootFolder.getId())) {
            pathParts.addFirst(current.getName());
            current = current.getParent();
        }

        pathParts.addFirst(rootFolder.getName());
        return String.join("/", pathParts);
    }

}

