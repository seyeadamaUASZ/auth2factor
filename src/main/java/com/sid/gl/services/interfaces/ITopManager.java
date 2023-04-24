package com.sid.gl.services.interfaces;

import java.util.Date;

public interface ITopManager {
    String generateSecret();
    String getUriForImage(String secret);
    boolean verifyCode(String code,String secret);
    boolean codeVerify(String code, String secret, Date date);
}
