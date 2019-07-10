package com.calvin.android.module.compiler;

import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-5
 */
final class BindingSet {

    static final ClassName UTILS = ClassName.get("butterknife.internal", "Utils", new String[0]);


    private static final ClassName VIEW = ClassName.get("android.view", "View", new String[0]);
    private static final ClassName CONTEXT = ClassName.get("android.content", "Context", new String[0]);

    private static final ClassName UNBINDER = ClassName.get("butterknife", "Unbinder", new String[0]);

    private final TypeName targetTypeName;
    private final ClassName bindingClassName;
    private final boolean isFinal;
    private final boolean isView;
    private final boolean isActivity;
    private final boolean isDialog;
    private final ImmutableList<ViewBinding> viewBindings;
    private final ImmutableList<FieldCollectionViewBinding> collectionBindings;
    //private final ImmutableList<ResourceBinding> resourceBindings;

    private final BindingSet parentBinding;

    private BindingSet(TypeName targetTypeName, ClassName bindingClassName, boolean isFinal, boolean isView, boolean isActivity, boolean isDialog, ImmutableList<ViewBinding> viewBindings, ImmutableList<FieldCollectionViewBinding> collectionBindings,
            /*ImmutableList<ResourceBinding> resourceBindings, @Nullable*/ BindingSet parentBinding) {
        this.isFinal = isFinal;
        this.targetTypeName = targetTypeName;
        this.bindingClassName = bindingClassName;
        this.isView = isView;
        this.isActivity = isActivity;
        this.isDialog = isDialog;
        this.viewBindings = viewBindings;
        this.collectionBindings = collectionBindings;
        //this.resourceBindings = resourceBindings;
        this.parentBinding = parentBinding;
    }



    JavaFile brewJava(int sdk, boolean debuggable) {
        TypeSpec bindingConfiguration = this.createType(sdk, debuggable);
        return JavaFile.builder(this.bindingClassName.packageName(), bindingConfiguration).addFileComment("Generated code from Butter Knife. Do not modify!", new Object[0]).build();
    }

    private TypeSpec createType(int sdk, boolean debuggable) {
        TypeSpec.Builder result = TypeSpec.classBuilder(this.bindingClassName.simpleName()).addModifiers(new Modifier[]{Modifier.PUBLIC});
        if (this.isFinal) {
            result.addModifiers(new Modifier[]{Modifier.FINAL});
        }

        if (this.parentBinding != null) {
            result.superclass(this.parentBinding.bindingClassName);
        } else {
            result.addSuperinterface(UNBINDER);
        }

        if (this.hasTargetField()) {
            result.addField(this.targetTypeName, "target", new Modifier[]{Modifier.PRIVATE});
        }

        if (this.isView) {
            result.addMethod(this.createBindingConstructorForView());
        } else if (this.isActivity) {
            result.addMethod(this.createBindingConstructorForActivity());
        } else if (this.isDialog) {
            result.addMethod(this.createBindingConstructorForDialog());
        }

        if (!this.constructorNeedsView()) {
            result.addMethod(this.createBindingViewDelegateConstructor());
        }

        result.addMethod(this.createBindingConstructor(sdk, debuggable));
        if (this.hasViewBindings() || this.parentBinding == null) {
            result.addMethod(this.createBindingUnbindMethod(result));
        }

        return result.build();
    }

    private MethodSpec createBindingViewDelegateConstructor() {
        return MethodSpec.constructorBuilder().addJavadoc("@deprecated Use {@link #$T($T, $T)} for direct creation.\n    Only present for runtime invocation through {@code ButterKnife.bind()}.\n", new Object[]{this.bindingClassName, this.targetTypeName, CONTEXT}).addAnnotation(Deprecated.class)/*.addAnnotation(UI_THREAD)*/.addModifiers(new Modifier[]{Modifier.PUBLIC}).addParameter(this.targetTypeName, "target", new Modifier[0]).addParameter(VIEW, "source", new Modifier[0]).addStatement("this(target, source.getContext())", new Object[0]).build();
    }



    private MethodSpec createBindingConstructorForActivity() {
        com.squareup.javapoet.MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(new Modifier[]{Modifier.PUBLIC}).addParameter(this.targetTypeName, "target", new Modifier[0]);
        if (this.constructorNeedsView()) {
            builder.addStatement("this(target, target.getWindow().getDecorView())", new Object[0]);
        } else {
            builder.addStatement("this(target, target)", new Object[0]);
        }

        return builder.build();
    }

    private MethodSpec createBindingConstructorForView() {
        com.squareup.javapoet.MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(new Modifier[]{Modifier.PUBLIC}).addParameter(this.targetTypeName, "target", new Modifier[0]);
        if (this.constructorNeedsView()) {
            builder.addStatement("this(target, target)", new Object[0]);
        } else {
            builder.addStatement("this(target, target.getContext())", new Object[0]);
        }

        return builder.build();
    }



    private MethodSpec createBindingConstructor(int sdk, boolean debuggable) {
        com.squareup.javapoet.MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(new Modifier[]{Modifier.PUBLIC});
        /*if (this.hasMethodBindings()) {
            constructor.addParameter(this.targetTypeName, "target", new Modifier[]{Modifier.FINAL});
        } else {*/
            constructor.addParameter(this.targetTypeName, "target", new Modifier[0]);
        //}

        if (this.constructorNeedsView()) {
            constructor.addParameter(VIEW, "source", new Modifier[0]);
        } else {
            constructor.addParameter(CONTEXT, "context", new Modifier[0]);
        }

       /* if (this.hasUnqualifiedResourceBindings()) {
            constructor.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", new Object[]{"ResourceType"}).build());
        }

        if (this.hasOnTouchMethodBindings()) {
            constructor.addAnnotation(AnnotationSpec.builder(SUPPRESS_LINT).addMember("value", "$S", new Object[]{"ClickableViewAccessibility"}).build());
        }*/

        if (this.parentBinding != null) {
            if (this.parentBinding.constructorNeedsView()) {
                constructor.addStatement("super(target, source)", new Object[0]);
            } else if (this.constructorNeedsView()) {
                constructor.addStatement("super(target, source.getContext())", new Object[0]);
            } else {
                constructor.addStatement("super(target, context)", new Object[0]);
            }

            constructor.addCode("\n", new Object[0]);
        }

        if (this.hasTargetField()) {
            constructor.addStatement("this.target = target", new Object[0]);
            constructor.addCode("\n", new Object[0]);
        }

        UnmodifiableIterator var4;
        if (this.hasViewBindings()) {
            if (this.hasViewLocal()) {
                constructor.addStatement("$T view", new Object[]{VIEW});
            }

            var4 = this.viewBindings.iterator();

            while(var4.hasNext()) {
                ViewBinding binding = (ViewBinding)var4.next();
                this.addViewBinding(constructor, binding, debuggable);
            }

            var4 = this.collectionBindings.iterator();

            while(var4.hasNext()) {
                FieldCollectionViewBinding binding = (FieldCollectionViewBinding)var4.next();
                constructor.addStatement("$L", new Object[]{binding.render(debuggable)});
            }

            /*if (!this.resourceBindings.isEmpty()) {
                constructor.addCode("\n", new Object[0]);
            }*/
        }

        /*if (!this.resourceBindings.isEmpty()) {
            if (this.constructorNeedsView()) {
                constructor.addStatement("$T context = source.getContext()", new Object[]{CONTEXT});
            }

            if (this.hasResourceBindingsNeedingResource(sdk)) {
                constructor.addStatement("$T res = context.getResources()", new Object[]{RESOURCES});
            }

            var4 = this.resourceBindings.iterator();

            while(var4.hasNext()) {
                ResourceBinding binding = (ResourceBinding)var4.next();
                constructor.addStatement("$L", new Object[]{binding.render(sdk)});
            }
        }*/

        return constructor.build();
    }

    private boolean hasViewLocal() {
        UnmodifiableIterator var1 = this.viewBindings.iterator();

        ViewBinding bindings;
        do {
            if (!var1.hasNext()) {
                return false;
            }

            bindings = (ViewBinding)var1.next();
        } while(!bindings.requiresLocal());

        return true;
    }

    private MethodSpec createBindingUnbindMethod(com.squareup.javapoet.TypeSpec.Builder bindingClass) {
        com.squareup.javapoet.MethodSpec.Builder result = MethodSpec.methodBuilder("unbind").addAnnotation(Override.class).addModifiers(new Modifier[]{Modifier.PUBLIC});
        if (!this.isFinal && this.parentBinding == null) {
            //result.addAnnotation(CALL_SUPER);
        }

        UnmodifiableIterator var3;
        ViewBinding binding;
        if (this.hasTargetField()) {
            if (this.hasFieldBindings()) {
                result.addStatement("$T target = this.target", new Object[]{this.targetTypeName});
            }

            result.addStatement("if (target == null) throw new $T($S)", new Object[]{IllegalStateException.class, "Bindings already cleared."});
            result.addStatement("$N = null", new Object[]{this.hasFieldBindings() ? "this.target" : "target"});
            result.addCode("\n", new Object[0]);
            var3 = this.viewBindings.iterator();

            while(var3.hasNext()) {
                binding = (ViewBinding)var3.next();
                if (binding.getFieldBinding() != null) {
                    result.addStatement("target.$L = null", new Object[]{binding.getFieldBinding().getName()});
                }
            }

            var3 = this.collectionBindings.iterator();

            while(var3.hasNext()) {
                FieldCollectionViewBinding fbinding = (FieldCollectionViewBinding)var3.next();
                result.addStatement("target.$L = null", new Object[]{fbinding.name});
            }
        }

       /* if (this.hasMethodBindings()) {
            result.addCode("\n", new Object[0]);
            var3 = this.viewBindings.iterator();

            while(var3.hasNext()) {
                binding = (ViewBinding)var3.next();
                this.addFieldAndUnbindStatement(bindingClass, result, binding);
            }
        }*/

        if (this.parentBinding != null) {
            result.addCode("\n", new Object[0]);
            result.addStatement("super.unbind()", new Object[0]);
        }

        return result.build();
    }

    private MethodSpec createBindingConstructorForDialog() {
        com.squareup.javapoet.MethodSpec.Builder builder = MethodSpec.constructorBuilder()/*.addAnnotation(UI_THREAD)*/.addModifiers(new Modifier[]{Modifier.PUBLIC}).addParameter(this.targetTypeName, "target", new Modifier[0]);
        if (this.constructorNeedsView()) {
            builder.addStatement("this(target, target.getWindow().getDecorView())", new Object[0]);
        } else {
            builder.addStatement("this(target, target.getContext())", new Object[0]);
        }

        return builder.build();
    }

    private boolean constructorNeedsView() {
        return this.hasViewBindings() || this.parentBinding != null && this.parentBinding.constructorNeedsView();
    }

    private boolean hasViewBindings() {
        return !this.viewBindings.isEmpty() || !this.collectionBindings.isEmpty();
    }

    private boolean hasTargetField() {
        return this.hasFieldBindings() /*|| this.hasMethodBindings()*/;
    }

    private boolean hasFieldBindings() {
        UnmodifiableIterator var1 = this.viewBindings.iterator();

        ViewBinding bindings;
        do {
            if (!var1.hasNext()) {
                return !this.collectionBindings.isEmpty();
            }

            bindings = (ViewBinding)var1.next();
        } while(bindings.getFieldBinding() == null);

        return true;
    }

    static final class Builder {
        private final TypeName targetTypeName;
        private final ClassName bindingClassName;
        private final boolean isFinal;
        private final boolean isView;
        private final boolean isActivity;
        private final boolean isDialog;

        private BindingSet parentBinding;
        private final Map<Id, ViewBinding.Builder> viewIdMap;
        private final com.google.common.collect.ImmutableList.Builder<FieldCollectionViewBinding> collectionBindings;
        //private final com.google.common.collect.ImmutableList.Builder<ResourceBinding> resourceBindings;

        private Builder(TypeName targetTypeName, ClassName bindingClassName, boolean isFinal, boolean isView, boolean isActivity, boolean isDialog) {
            this.viewIdMap = new LinkedHashMap();
            this.collectionBindings = ImmutableList.builder();
            //this.resourceBindings = ImmutableList.builder();
            this.targetTypeName = targetTypeName;
            this.bindingClassName = bindingClassName;
            this.isFinal = isFinal;
            this.isView = isView;
            this.isActivity = isActivity;
            this.isDialog = isDialog;
        }

        void addField(Id id, FieldViewBinding binding) {
            this.getOrCreateViewBindings(id).setFieldBinding(binding);
        }

        void addFieldCollection(FieldCollectionViewBinding binding) {
            this.collectionBindings.add(binding);
        }

        /*boolean addMethod(Id id, ListenerClass listener, ListenerMethod method, MethodViewBinding binding) {
            butterknife.compiler.ViewBinding.Builder viewBinding = this.getOrCreateViewBindings(id);
            if (viewBinding.hasMethodBinding(listener, method) && !"void".equals(method.returnType())) {
                return false;
            } else {
                viewBinding.addMethodBinding(listener, method, binding);
                return true;
            }
        }

        void addResource(ResourceBinding binding) {
            this.resourceBindings.add(binding);
        }*/

        void setParent(BindingSet parent) {
            this.parentBinding = parent;
        }


        String findExistingBindingName(Id id) {
            ViewBinding.Builder builder = (ViewBinding.Builder)this.viewIdMap.get(id);
            if (builder == null) {
                return null;
            } else {
                FieldViewBinding fieldBinding = builder.fieldBinding;
                return fieldBinding == null ? null : fieldBinding.getName();
            }
        }

        private ViewBinding.Builder getOrCreateViewBindings(Id id) {
            ViewBinding.Builder viewId = (ViewBinding.Builder)this.viewIdMap.get(id);
            if (viewId == null) {
                viewId = new ViewBinding.Builder(id);
                this.viewIdMap.put(id, viewId);
            }

            return viewId;
        }

        BindingSet build() {
            com.google.common.collect.ImmutableList.Builder<ViewBinding> viewBindings = ImmutableList.builder();
            Iterator var2 = this.viewIdMap.values().iterator();

            while(var2.hasNext()) {
                ViewBinding.Builder builder = (ViewBinding.Builder)var2.next();
                viewBindings.add(builder.build());
            }

            return new BindingSet(this.targetTypeName, this.bindingClassName, this.isFinal, this.isView, this.isActivity, this.isDialog, viewBindings.build(), this.collectionBindings.build(), /*this.resourceBindings.build(),*/ this.parentBinding);
        }
    }


    static boolean requiresCast(TypeName type) {
        return !"android.view.View".equals(type.toString());
    }

    private void addViewBinding(com.squareup.javapoet.MethodSpec.Builder result, ViewBinding binding, boolean debuggable) {
        if (binding.isSingleFieldBinding()) {
            FieldViewBinding fieldBinding = (FieldViewBinding) Objects.requireNonNull(binding.getFieldBinding());
            com.squareup.javapoet.CodeBlock.Builder builder = CodeBlock.builder().add("target.$L = ", new Object[]{fieldBinding.getName()});
            boolean requiresCast = requiresCast(fieldBinding.getType());
            if (debuggable && (requiresCast || fieldBinding.isRequired())) {
                builder.add("$T.find", new Object[]{UTILS});
                builder.add(fieldBinding.isRequired() ? "RequiredView" : "OptionalView", new Object[0]);
                if (requiresCast) {
                    builder.add("AsType", new Object[0]);
                }

                builder.add("(source, $L", new Object[]{binding.getId().code});
                if (fieldBinding.isRequired() || requiresCast) {
                    builder.add(", $S", new Object[]{asHumanDescription(Collections.singletonList(fieldBinding))});
                }

                if (requiresCast) {
                    builder.add(", $T.class", new Object[]{fieldBinding.getRawType()});
                }

                builder.add(")", new Object[0]);
            } else {
                if (requiresCast) {
                    builder.add("($T) ", new Object[]{fieldBinding.getType()});
                }

                builder.add("source.findViewById($L)", new Object[]{binding.getId().code});
            }

            result.addStatement("$L", new Object[]{builder.build()});
        } else {
            List<MemberViewBinding> requiredBindings = binding.getRequiredBindings();
            if (debuggable && !requiredBindings.isEmpty()) {
                if (!binding.isBoundToRoot()) {
                    result.addStatement("view = $T.findRequiredView(source, $L, $S)", new Object[]{UTILS, binding.getId().code, asHumanDescription(requiredBindings)});
                }
            } else {
                result.addStatement("view = source.findViewById($L)", new Object[]{binding.getId().code});
            }

            this.addFieldBinding(result, binding, debuggable);
            //this.addMethodBindings(result, binding, debuggable);
        }
    }

    private void addFieldBinding(com.squareup.javapoet.MethodSpec.Builder result, ViewBinding binding, boolean debuggable) {
        FieldViewBinding fieldBinding = binding.getFieldBinding();
        if (fieldBinding != null) {
            if (requiresCast(fieldBinding.getType())) {
                if (debuggable) {
                    result.addStatement("target.$L = $T.castView(view, $L, $S, $T.class)", new Object[]{fieldBinding.getName(), UTILS, binding.getId().code, asHumanDescription(Collections.singletonList(fieldBinding)), fieldBinding.getRawType()});
                } else {
                    result.addStatement("target.$L = ($T) view", new Object[]{fieldBinding.getName(), fieldBinding.getType()});
                }
            } else {
                result.addStatement("target.$L = view", new Object[]{fieldBinding.getName()});
            }
        }

    }

    static String asHumanDescription(Collection<? extends MemberViewBinding> bindings) {
        Iterator<? extends MemberViewBinding> iterator = bindings.iterator();
        switch(bindings.size()) {
            case 1:
                return ((MemberViewBinding)iterator.next()).getDescription();
            case 2:
                return ((MemberViewBinding)iterator.next()).getDescription() + " and " + ((MemberViewBinding)iterator.next()).getDescription();
            default:
                StringBuilder builder = new StringBuilder();
                int i = 0;

                for(int count = bindings.size(); i < count; ++i) {
                    if (i != 0) {
                        builder.append(", ");
                    }

                    if (i == count - 1) {
                        builder.append("and ");
                    }

                    builder.append(((MemberViewBinding)iterator.next()).getDescription());
                }

                return builder.toString();
        }
    }

    static BindingSet.Builder newBuilder(TypeElement enclosingElement) {
        TypeMirror typeMirror = enclosingElement.asType();
        boolean isView = ViewInjectProcessor.isSubtypeOfType(typeMirror, "android.view.View");
        boolean isActivity = ViewInjectProcessor.isSubtypeOfType(typeMirror, "android.app.Activity");
        boolean isDialog = ViewInjectProcessor.isSubtypeOfType(typeMirror, "android.app.Dialog");
        TypeName targetType = TypeName.get(typeMirror);
        if (targetType instanceof ParameterizedTypeName) {
            targetType = ((ParameterizedTypeName)targetType).rawType;
        }

        String packageName = MoreElements.getPackage(enclosingElement).getQualifiedName().toString();
        String className = enclosingElement.getQualifiedName().toString().substring(packageName.length() + 1).replace('.', '$');
        ClassName bindingClassName = ClassName.get(packageName, className + "_ViewBinding", new String[0]);
        boolean isFinal = enclosingElement.getModifiers().contains(Modifier.FINAL);
        return new BindingSet.Builder((TypeName)targetType, bindingClassName, isFinal, isView, isActivity, isDialog);
    }
}
