package com.cloud.common;

public class AuthRequest extends AbstractMessage {
    String login, password;

   public AuthRequest (String login, String password) {
       this.login = login;
       this.password = password;
   }


    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }

}
