package com.ming.buity.proxy;

import com.ming.buity.annotation.*;
import com.ming.buity.entrance.ApplicationMapperContain;
import com.ming.buity.handlerenum.TypeHandler;
import com.ming.buity.utils.AnalysisUtils;
import com.ming.buity.utils.JdbcUtils;


import java.io.Serializable;
import java.lang.reflect.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapperProxy<T> extends ApplicationMapperContain implements InvocationHandler, Serializable {
    private Class<T> classType;
    private Map<Class<?>, List<MapperMethod>> typeMap = ApplicationContainConfig.typeMap;
    private Map<Class<?>, Class<?>> EntityMap = ApplicationContainConfig.EntityMap;

    public MapperProxy(Class<T> classType) {
        this.classType = classType;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //如果这个是Object方法那就直接调用就好了
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        //判断是否使用该代理
        if (typeMap.containsKey(classType)) {
            List<MapperMethod> list = typeMap.get(classType);
            //遍历带MapperMethod的list集合
            for (MapperMethod mapperMethod : list) {
                //如果方法的名称存在集合中,继续进行
                if (method.getName().equals(mapperMethod.getMethodName())) {
                    //判断方法属于的类型
                    if (mapperMethod.getTypeHandler() == TypeHandler.SELECT) {
                        String sql = method.getAnnotation(Select.class).value();
                        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();//作为缓冲区
                        //根据名称,存入参数的值
                        foreachParam(args, method, map);
                        //排序数组
                        List<Object> listContain = new ArrayList<>();
                        //参数名称缓冲区
                        List<String> sqlParam = AnalysisUtils.getSQLParam(sql);
                        for (String string : sqlParam) {
                            Object paramValue = map.get(string);//给参数排序
                            listContain.add(paramValue);
                        }
                        //解析SQL,改成?的形式
                        sql = AnalysisUtils.newSQL(sql, sqlParam);
                        //获取链接
                        Connection connection = JdbcUtils.getConnection();
                        //获取返回值类型class
                        Class<?> returnType = method.getReturnType();//这个参数有问题,因为无法获取List的具体泛型类型
                        //组合对象,通过光标判断是否为集合
                        if (method.getReturnType().equals(List.class)) {
                            //System.out.println("this list object");
                            //拿不到T信息,或者加上另一个方案通过@注入容器,或者编写List和实体类绑定的对象
                            //这里采用了第二种方式,第一种实现不了,我妥协了...
                            Class<?> clazzEntity = EntityMap.get(classType);
                            return JdbcUtils.getBeans(connection, sql, clazzEntity, listContain);
                        } else {
                            //尝试使用
                            return JdbcUtils.getBean(connection, sql, returnType, listContain);
                        }
                    } else if (mapperMethod.getTypeHandler() == TypeHandler.INSERT) {
                        System.out.println("INSERT");
                        String sql = method.getAnnotation(Insert.class).value();
                        return enclosureMethod(method,sql,args);
                    } else if (mapperMethod.getTypeHandler() == TypeHandler.UPDATE) {
                        System.out.println("UPDATE");
                        String sql = method.getAnnotation(Update.class).value();
                        return enclosureMethod(method,sql,args);
                    } else if (mapperMethod.getTypeHandler() == TypeHandler.DELETE) {
                        System.out.println("DELETE");
                        String sql = method.getAnnotation(Delete.class).value();
                        return enclosureMethod(method,sql,args);
                    }
                }
            }
        }

        return null;
    }

    private void foreachParam(Object[] args,Method method, ConcurrentHashMap<String, Object> map) {
        if (args == null) {
            return;
        }
        Parameter[] parameters = method.getParameters();//拿到参数的对象
        for (int i = 0; i < args.length; i++) {
            Object paramValue = args[i];
            Param param = parameters[i].getAnnotation(Param.class);
            if (param != null) {
                String paramName = parameters[i].getAnnotation(Param.class).value();
                map.put(paramName, paramValue);
            }
        }
    }
    private int enclosureMethod(Method method,String sql,Object[]args){
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();//作为缓冲区
        //根据名称,存入参数的值
        foreachParam(args, method, map);
        //排序数组
        List<Object> listContain = new ArrayList<>();
        //参数名称缓冲区
        List<String> sqlParam = AnalysisUtils.getSQLParam(sql);
        //获取链接
        Connection connection = JdbcUtils.getConnection();

        for (String string : sqlParam) {
            Object paramValue = map.get(string);//给参数排序
            listContain.add(paramValue);
        }
        //解析SQL,改成?的形式
        sql = AnalysisUtils.newSQL(sql, sqlParam);
        //结果
        return JdbcUtils.methodAll(connection,sql,listContain);
    }
}
