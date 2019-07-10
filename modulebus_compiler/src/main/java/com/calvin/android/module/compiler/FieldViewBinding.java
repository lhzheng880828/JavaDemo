package com.calvin.android.module.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-5
 */
final class FieldViewBinding implements MemberViewBinding {
    private final String name;
    private final TypeName type;
    private final boolean required;

    FieldViewBinding(String name, TypeName type, boolean required) {
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public String getName() {
        return this.name;
    }

    public TypeName getType() {
        return this.type;
    }

    public ClassName getRawType() {
        return this.type instanceof ParameterizedTypeName ? ((ParameterizedTypeName)this.type).rawType : (ClassName)this.type;
    }

    public String getDescription() {
        return "field '" + this.name + "'";
    }

    public boolean isRequired() {
        return this.required;
    }
}