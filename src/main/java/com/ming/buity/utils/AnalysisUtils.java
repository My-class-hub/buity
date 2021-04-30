package com.ming.buity.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalysisUtils {

    public static String newSQL(String sql,String[] param) {
        for (int i = 0; i < param.length; i++){
            String string = param[i];
            sql = sql.replace("#{"+string+"}","?");
        }
        return sql;
    }

    public static String newSQL(String sql,List<String> param) {
        for (String string : param) {
            sql = sql.replace("#{"+string+"}","?");
        }
        return sql;
    }
    //正则表达式版本
    public static List<String> getSQLParam(String sql) {
        List<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("#[^}]+}");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            pattern = Pattern.compile("[^#{}]+");
            Matcher matcher1 = pattern.matcher(matcher.group());
            while(matcher1.find()){
                list.add(matcher1.group());
            }
        }
        return list;
    }
}
