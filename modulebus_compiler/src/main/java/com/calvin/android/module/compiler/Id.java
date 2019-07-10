package com.calvin.android.module.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.sun.tools.javac.code.Symbol;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-5
 */
final class Id {
    private static final ClassName ANDROID_R = ClassName.get("android", "R", new String[0]);
    private static final String R = "R";
    final int value;
    final CodeBlock code;
    final boolean qualifed;

    Id(int value) {
        this(value, (Symbol)null);
    }

    Id(int value,  Symbol rSymbol) {
        this.value = value;
        if (rSymbol != null) {
            ClassName className = ClassName.get(rSymbol.packge().getQualifiedName().toString(), "R", new String[]{rSymbol.enclClass().name.toString()});
            String resourceName = rSymbol.name.toString();
            this.code = className.topLevelClassName().equals(ANDROID_R) ? CodeBlock.of("$L.$N", new Object[]{className, resourceName}) : CodeBlock.of("$T.$N", new Object[]{className, resourceName});
            this.qualifed = true;
        } else {
            this.code = CodeBlock.of("$L", new Object[]{value});
            this.qualifed = false;
        }

    }

    public boolean equals(Object o) {
        return o instanceof Id && this.value == ((Id)o).value;
    }

    public int hashCode() {
        return this.value;
    }

    public String toString() {
        throw new UnsupportedOperationException("Please use value or code explicitly");
    }
}