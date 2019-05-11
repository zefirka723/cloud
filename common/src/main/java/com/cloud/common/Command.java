package com.cloud.common;

public class Command extends AbstractMessage {

//    AUTH_REQUEST
//    AUTH_OK;
//    AUTH_DENIED;
//    DISCONNECTED;

//    REFRESH_REQUEST;
//    REFRESH_FILES_LIST;

    private String msgType;
    private String msgText;

    public Command (String msgType) {
        this.msgType = msgType;
    }

    public Command (String msgType, String msgText) {
        this.msgType = msgType;
        this.msgText = msgText;
    }

    public String getMsgType() {
        return msgType;
    }
    public String getMsgText() {
        return msgText;
    }


}
