package com.vpgh.dms.util;

public enum PageSize {
    USER_PAGE_SIZE(10),
    ROLE_PAGE_SIZE(5),
    PERMISSION_PAGE_SIZE(10),
    SETTING_PAGE_SIZE(5),
    FOLDER_PAGE_SIZE(10),
    DOCUMENT_PAGE_SIZE(10),
    GROUP_PAGE_SIZE(5);

    private final int size;

    PageSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
