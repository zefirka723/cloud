package com.cloud.common;

public class Response extends AbstractMessage {

    public enum ResponseType { FILES_LIST; }

    private ResponseType type;

    public Response (ResponseType type) {
        this.type = type;
    }
    public ResponseType getType() {
        return type;
    }


    public Response (ResponseType type, String message) {}



  //  public String message() {
  //      return message;
    //}

}
