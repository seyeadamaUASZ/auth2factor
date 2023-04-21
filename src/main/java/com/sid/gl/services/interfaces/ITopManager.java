package com.sid.gl.services.interfaces;

public interface ITopManager {
    String generateSecret();
    String getUriForImage(String secret);
    boolean verifyCode(String code,String secret);
}
