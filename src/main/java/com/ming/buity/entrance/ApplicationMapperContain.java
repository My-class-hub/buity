package com.ming.buity.entrance;

import com.ming.buity.annotation.*;
import com.ming.buity.handlerenum.TypeHandler;
import com.ming.buity.proxy.MapperMethod;
import com.ming.buity.proxy.MapperProxy;
import com.ming.buity.utils.QueryUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationMapperContain {
    private static Map<Class<?>, List<MapperMethod>> typeMap = new ConcurrentHashMap<>();
    private static Map<Class<?>, Class<?>> EntityMap = new ConcurrentHashMap<>();

    //通过这个内部类对其进行封装
    protected static class ApplicationContainConfig {
        public static Map<Class<?>, List<MapperMethod>> typeMap = ApplicationMapperContain.typeMap;
        public static Map<Class<?>, Class<?>> EntityMap = ApplicationMapperContain.EntityMap;
    }

    public static <T> void run(Class<T> clazz) {
        MapperSan mapperSan = clazz.getAnnotation(MapperSan.class);
        if (mapperSan != null) {
            String packageURL = mapperSan.value();
            //获取接口类和其代理对象
            Set<Class<?>> cacheSet = QueryUtils.findPackage(packageURL);
            //实体关系映射
            EntityMap = QueryUtils.BindClazzEntity(cacheSet);
            for (Class<?> clazzSet : cacheSet) {
                judgeType(clazzSet);
            }
            //定义接口开始
        }
    }

    //对数据判断并且将方法和类型放入数据结构中
    private static void judgeType(Class<?> clazzKey) {
        List<MapperMethod> list = new ArrayList<>();
        Method[] methods = clazzKey.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(Select.class) != null) {
                list.add(new MapperMethod(method.getName(), TypeHandler.SELECT));
            }
            if (method.getAnnotation(Insert.class) != null) {
                list.add(new MapperMethod(method.getName(), TypeHandler.INSERT));
            }
            if (method.getAnnotation(Update.class) != null) {
                list.add(new MapperMethod(method.getName(), TypeHandler.UPDATE));
            }
            if (method.getAnnotation(Delete.class) != null) {
                list.add(new MapperMethod(method.getName(), TypeHandler.DELETE));
            }
        }
        typeMap.put(clazzKey, list);
    }

}
