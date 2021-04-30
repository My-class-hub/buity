package com.ming.buity.mapper;

import com.ming.buity.configuration.Configuration;
import com.ming.buity.proxy.MapperProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class InterfaceStart {
    private Configuration configuration;

    public InterfaceStart(Configuration configuration) {
        this.configuration = configuration;
    }


    public static <T> T getMapper(Class<T> clazz) {
        //这个可以证明他一定是一个接口
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[]{clazz},
                new MapperProxy<>(clazz));
    }
}
