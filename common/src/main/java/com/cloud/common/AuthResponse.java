package com.cloud.common;

public class AuthResponse extends AbstractMessage {
    boolean isLogged;
    String message;

   public AuthResponse(boolean isLogged, String message) {
       this.isLogged = isLogged;
       this.message = message;
   }


    public boolean isLogged() {
        return this.isLogged;
    }

    public String getMessage() {
        return this.message;
    }

}
