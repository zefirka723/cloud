package com.cloud.common;

public class Request extends AbstractMessage {
    public enum type {
        REFRESH_FILES_LIST,
        DELETE_FILE;
    }
}
