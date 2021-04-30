package com.ming.buity.proxy;

import com.ming.buity.handlerenum.TypeHandler;

public class MapperMethod {
    private String methodName;
    private TypeHandler typeHandler;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public TypeHandler getTypeHandler() {
        return typeHandler;
    }

    public void setTypeHandler(TypeHandler typeHandler) {
        this.typeHandler = typeHandler;
    }

    public MapperMethod(String methodName, TypeHandler typeHandler) {
        this.methodName = methodName;
        this.typeHandler = typeHandler;
    }

    @Override
    public String toString() {
        return "MapperMethod{" +
                "methodName='" + methodName + '\'' +
                ", typeHandler=" + typeHandler +
                '}';
    }
}
