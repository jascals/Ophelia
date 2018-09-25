package com.jascal.ophelia_compiler;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

class Proxy {
    private static final String OPHELIA_INFO = "OpheliaProxy";
    private static final String CONCAT = "$";

    private final String packageName;
    private final TypeElement element;
    private final String info;
    private final String typeName;

    public String getTypeName() {
        return typeName;
    }

    public TypeElement getElement() {
        return element;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getInfo() {
        return info;
    }

    Map<String, Integer> bindViewMap = new HashMap<>();
    Map<String, Integer> onClickMap = new HashMap<>();

    Proxy(Elements elementUtils, TypeElement element) {
        this.element = element;
        packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
        typeName = element.getSimpleName().toString();// class name

        String className = getClassName(element, packageName);
        info = className + CONCAT + OPHELIA_INFO;
    }

    private String getClassName(TypeElement element, String packageName) {
        int packageLen = packageName.length() + 1;
        return element.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }
}
