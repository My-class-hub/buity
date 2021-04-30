package com.ming.buity.utils;

import com.ming.buity.annotation.Entity;
import com.ming.buity.annotation.UseEntity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class QueryUtils {

    public static Set<Class<?>> findPackage(String packageURL) {
        //将带类的数据存缓冲区
        Set<Class<?>> cacheSet = new HashSet<>();

        String packageDirName = packageURL.replace(".", "/");
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                    findPackageToFile(packageURL, filePath, cacheSet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cacheSet;
    }

    private static void findPackageToFile(String packageName, String filePath, Set<Class<?>> cacheSet) {
        // 获取此包的目录 建立一个File
        File dir = new File(filePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        //同时对文件进行过滤
        File[] files = dir.listFiles(pathname -> {
            return (pathname.getName().endsWith(".class") || pathname.isDirectory());
        });
        // 循环所有文件
        for (File file : files) {
            if (file.isDirectory()) {
                //循环遍历查找文件
                findPackageToFile(packageName + "." + file.getName(), file.getAbsolutePath(), cacheSet);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazzDefinition = Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className);
                    //只有是一个接口才给放出来
                    if (clazzDefinition.isInterface()) {
                        cacheSet.add(clazzDefinition);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //记录@Entity和@UseEntity的绑定
    public static Map<Class<?>, Class<?>> BindClazzEntity(Set<Class<?>> cacheSet) {
        Map<Class<?>, Class<?>> map = new ConcurrentHashMap<>();
        for (Class<?> clazz : cacheSet) {
            UseEntity useEntity = clazz.getAnnotation(UseEntity.class);
            if (useEntity != null) {
                Class<?> clazzEntity = useEntity.value();
                Entity entity = clazzEntity.getAnnotation(Entity.class);
                if (entity != null) {
                    map.put(clazz, clazzEntity);
                }
            }
        }
        return map;
    }

    public static List<String> findProperties() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("");
        String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
        List<String> list = new ArrayList<>();
        findPropertiesToFile(filePath, list);
        return list;
    }

    private static void findPropertiesToFile(String filePath, List<String> list) {
        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }
        File[] files = file.listFiles(pathname -> {
            return pathname.getName().endsWith(".properties");//目前先约定好只能在resources根目录下使用
        });
        for (File fileChildren : files) {
            list.add(fileChildren.getName());
        }
    }
}
