package com.ot.moto.util;

public class StringUtil {

    public static boolean isEmpty(String str){
        if(str == null){
            return false;
        }
        return str.isEmpty();
    }
}