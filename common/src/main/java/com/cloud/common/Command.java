package com.cloud.common;

public class Command extends AbstractMessage {

    public enum CommandType {
        AUTH_REQUEST,
        AUTH_OK,
        AUTH_FAILED,

        DISCONNECT,
        DISCONNECTED,

        CLOUD_FILES_REQUEST,
        CLOUD_FILES_LIST,

        DEL_FILE_REQUEST,
        DEL_FAILED
    }

    private CommandType commandType;
    private String msgText;

    public Command (CommandType commandType, String msgText) {
        this.commandType = commandType;
        this.msgText = msgText;
    }

    public Command (CommandType commandType) {
        this.commandType = commandType;
    }


    public String getMsgText() {
        return msgText;
    }

    public CommandType getCommandType() {
        return commandType;
    }

}
