package com.calvin.android.module.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.List;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-8
 */
final class FieldCollectionViewBinding {
    final String name;
    private final TypeName type;
    private final FieldCollectionViewBinding.Kind kind;
    private final boolean required;
    private final List<Id> ids;

    FieldCollectionViewBinding(String name, TypeName type, FieldCollectionViewBinding.Kind kind, List<Id> ids, boolean required) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.ids = ids;
        this.required = required;
    }

    CodeBlock render(boolean debuggable) {
        CodeBlock.Builder builder = CodeBlock.builder().add("target.$L = $T.$L(", new Object[]{this.name, BindingSet.UTILS, this.kind.factoryName});

        for(int i = 0; i < this.ids.size(); ++i) {
            if (i > 0) {
                builder.add(", ", new Object[0]);
            }

            builder.add("\n", new Object[0]);
            Id id = (Id)this.ids.get(i);
            boolean requiresCast = BindingSet.requiresCast(this.type);
            if (!debuggable) {
                if (requiresCast) {
                    builder.add("($T) ", new Object[]{this.type});
                }

                builder.add("source.findViewById($L)", new Object[]{id.code});
            } else if (!requiresCast && !this.required) {
                builder.add("source.findViewById($L)", new Object[]{id.code});
            } else {
                builder.add("$T.find", new Object[]{BindingSet.UTILS});
                builder.add(this.required ? "RequiredView" : "OptionalView", new Object[0]);
                if (requiresCast) {
                    builder.add("AsType", new Object[0]);
                }

                builder.add("(source, $L, \"field '$L'\"", new Object[]{id.code, this.name});
                if (requiresCast) {
                    TypeName rawType = this.type;
                    if (rawType instanceof ParameterizedTypeName) {
                        rawType = ((ParameterizedTypeName)rawType).rawType;
                    }

                    builder.add(", $T.class", new Object[]{rawType});
                }

                builder.add(")", new Object[0]);
            }
        }

        return builder.add(")", new Object[0]).build();
    }

    static enum Kind {
        ARRAY("arrayFilteringNull"),
        LIST("listFilteringNull");

        final String factoryName;

        private Kind(String factoryName) {
            this.factoryName = factoryName;
        }
    }
}
