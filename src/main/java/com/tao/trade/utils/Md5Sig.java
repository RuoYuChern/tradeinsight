package com.tao.trade.utils;

import java.security.MessageDigest;
import java.util.Base64;

public class Md5Sig {
    private MessageDigest md;
    public Md5Sig() {
        try{
            md = MessageDigest.getInstance("MD5");
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public String sign(String salt, String content, String time){
        StringBuilder sb = new StringBuilder();
        sb.append(salt).append(time).append(content);
        byte [] out = md.digest(sb.toString().getBytes());
        return Base64.getEncoder().encodeToString(out);
    }
}
