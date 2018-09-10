package com.jascal.ophelia_compiler;

final class NameUtils {
    static String getAutoGeneratorTypeName(String typeName) {
        return typeName + "$Binding";
    }
}
