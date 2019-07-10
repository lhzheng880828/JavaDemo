package com.calvin.android.module.compiler;

import com.calvin.android.module.annotation.InjectView;
import com.calvin.android.module.compiler.BindingSet.Builder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Author:linhu
 * Email:lhzheng@grandstream.cn
 * Date:19-7-5
 */
//可以用这个注解指定编译选项，替代getSupportedOptions()
//@SupportedOptions({"viewinject.debuggable","viewinject.minSdk"})
public class ViewInjectProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Filer filer;

    static final Id NO_ID = new Id(-1);


    private int sdk = 1;
    private boolean debuggable = true;

    private Trees trees;
    private final RScanner rScanner = new RScanner();

    public ViewInjectProcessor() {
        super();
    }

    @Override
    public Set<String> getSupportedOptions() {
        //return super.getSupportedOptions();
        Set<String> supportedOptions = new HashSet<>();
        supportedOptions.add("viewinject.debuggable");
        supportedOptions.add("viewinject.minSdk");
        return supportedOptions;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        //return super.getSupportedAnnotationTypes();

        Set<String> types = new LinkedHashSet();
        Iterator var2 = this.getSupportedAnnotations().iterator();
        while(var2.hasNext()) {
            Class<? extends Annotation> annotation = (Class)var2.next();
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet();
        annotations.add(InjectView.class);
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        String sdk = (String)processingEnv.getOptions().get("viewinject.minSdk");
        if (sdk != null) {
            try {
                this.sdk = Integer.parseInt(sdk);
            } catch (NumberFormatException var5) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Unable to parse supplied minSdk option '" + sdk + "'. Falling back to API 1 support.");
            }
        }
        this.debuggable = !"false".equals(processingEnv.getOptions().get("viewinject.debuggable"));
        this.typeUtils = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();

        try {
            this.trees = Trees.instance(processingEnv);
        } catch (IllegalArgumentException var4) {
        }
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return super.getCompletions(element, annotation, member, userText);
    }

    @Override
    protected synchronized boolean isInitialized() {
        return super.isInitialized();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<TypeElement, BindingSet> bindingMap = this.findAndParseTargets(roundEnv);
        Iterator var4 = bindingMap.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<TypeElement, BindingSet> entry = (Map.Entry)var4.next();
            TypeElement typeElement = (TypeElement)entry.getKey();
            BindingSet binding = (BindingSet)entry.getValue();
            JavaFile javaFile = binding.brewJava(this.sdk, this.debuggable);

            try {
                javaFile.writeTo(this.filer);
            } catch (IOException var10) {
                this.error(typeElement, "Unable to write binding for type %s: %s", typeElement, var10.getMessage());
            }
        }
        return false;
    }


    private Map<TypeElement, BindingSet> findAndParseTargets(RoundEnvironment env) {
        Map<TypeElement, BindingSet.Builder> builderMap = new LinkedHashMap();
        Set<TypeElement> erasedTargetNames = new LinkedHashSet();
        Iterator var4 = env.getElementsAnnotatedWith(InjectView.class).iterator();
        Element element;
        while(var4.hasNext()) {
            element = (Element)var4.next();
            try {
                this.parseBindView(element, builderMap, erasedTargetNames);
            } catch (Exception var12) {
                this.logParsingError(element, InjectView.class, var12);
            }
        }
        Deque<Map.Entry<TypeElement, BindingSet.Builder>> entries = new ArrayDeque(builderMap.entrySet());
        LinkedHashMap bindingMap = new LinkedHashMap();

        while(!entries.isEmpty()) {
            Map.Entry<TypeElement, BindingSet.Builder> entry = (Map.Entry)entries.removeFirst();
            TypeElement type = (TypeElement)entry.getKey();
            Builder builder = (Builder)entry.getValue();
            TypeElement parentType = this.findParentType(type, erasedTargetNames);
            if (parentType == null) {
                bindingMap.put(type, builder.build());
            } else {
                BindingSet parentBinding = (BindingSet)bindingMap.get(parentType);
                if (parentBinding != null) {
                    builder.setParent(parentBinding);
                    bindingMap.put(type, builder.build());
                } else {
                    entries.addLast(entry);
                }
            }
        }

        return bindingMap;
    }

    private TypeElement findParentType(TypeElement typeElement, Set<TypeElement> parents) {
        do {
            TypeMirror type = typeElement.getSuperclass();
            if (type.getKind() == TypeKind.NONE) {
                return null;
            }

            typeElement = (TypeElement)((DeclaredType)type).asElement();
        } while(!parents.contains(typeElement));

        return typeElement;
    }

    private void parseBindView(Element element, Map<TypeElement, BindingSet.Builder> builderMap, Set<TypeElement> erasedTargetNames) {
        TypeElement enclosingElement = (TypeElement)element.getEnclosingElement();
        boolean hasError = this.isInaccessibleViaGeneratedCode(InjectView.class, "fields", element) ||
                this.isBindingInWrongPackage(InjectView.class, element);
        TypeMirror elementType = element.asType();
        if (elementType.getKind() == TypeKind.TYPEVAR) {
            TypeVariable typeVariable = (TypeVariable)elementType;
            elementType = typeVariable.getUpperBound();
        }

        Name qualifiedName = enclosingElement.getQualifiedName();
        Name simpleName = element.getSimpleName();
        if (!isSubtypeOfType(elementType, "android.view.View") && !this.isInterface(elementType)) {
            if (elementType.getKind() == TypeKind.ERROR) {
                this.note(element, "@%s field with unresolved type (%s) must elsewhere be generated as a View or interface. (%s.%s)", InjectView.class.getSimpleName(), elementType, qualifiedName, simpleName);
            } else {
                this.error(element, "@%s fields must extend from View or be an interface. (%s.%s)", InjectView.class.getSimpleName(), qualifiedName, simpleName);
                hasError = true;
            }
        }

        if (!hasError) {
            int id = ((InjectView)element.getAnnotation(InjectView.class)).value();
            BindingSet.Builder builder = (BindingSet.Builder)builderMap.get(enclosingElement);
            Id resourceId = this.elementToId(element, InjectView.class, id);
            String existingBindingName;
            if (builder != null) {
                existingBindingName = builder.findExistingBindingName(resourceId);
                if (existingBindingName != null) {
                    this.error(element, "Attempt to use @%s for an already bound ID %d on '%s'. (%s.%s)", InjectView.class.getSimpleName(), id, existingBindingName, enclosingElement.getQualifiedName(), element.getSimpleName());
                    return;
                }
            } else {
                builder = this.getOrCreateBindingBuilder(builderMap, enclosingElement);
            }

            existingBindingName = simpleName.toString();
            TypeName type = TypeName.get(elementType);
            boolean required = isFieldRequired(element);
            builder.addField(resourceId, new FieldViewBinding(existingBindingName, type, required));
            erasedTargetNames.add(enclosingElement);
        }
    }

    private Builder getOrCreateBindingBuilder(Map<TypeElement, Builder> builderMap, TypeElement enclosingElement) {
        Builder builder = (Builder)builderMap.get(enclosingElement);
        if (builder == null) {
            builder = BindingSet.newBuilder(enclosingElement);
            builderMap.put(enclosingElement, builder);
        }

        return builder;
    }

    private boolean isInterface(TypeMirror typeMirror) {
        return typeMirror instanceof DeclaredType && ((DeclaredType)typeMirror).asElement().getKind() == ElementKind.INTERFACE;
    }

    static boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (isTypeEqual(typeMirror, otherType)) {
            return true;
        } else if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        } else {
            DeclaredType declaredType = (DeclaredType)typeMirror;
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (typeArguments.size() > 0) {
                StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
                typeString.append('<');

                for(int i = 0; i < typeArguments.size(); ++i) {
                    if (i > 0) {
                        typeString.append(',');
                    }

                    typeString.append('?');
                }

                typeString.append('>');
                if (typeString.toString().equals(otherType)) {
                    return true;
                }
            }

            Element element = declaredType.asElement();
            if (!(element instanceof TypeElement)) {
                return false;
            } else {
                TypeElement typeElement = (TypeElement)element;
                TypeMirror superType = typeElement.getSuperclass();
                if (isSubtypeOfType(superType, otherType)) {
                    return true;
                } else {
                    Iterator var7 = typeElement.getInterfaces().iterator();

                    TypeMirror interfaceType;
                    do {
                        if (!var7.hasNext()) {
                            return false;
                        }

                        interfaceType = (TypeMirror)var7.next();
                    } while(!isSubtypeOfType(interfaceType, otherType));

                    return true;
                }
            }
        }
    }

    private static boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
        return otherType.equals(typeMirror.toString());
    }

    private boolean isInaccessibleViaGeneratedCode(Class<? extends Annotation> annotationClass, String targetThing, Element element) {
        boolean hasError = false;
        TypeElement enclosingElement = (TypeElement)element.getEnclosingElement();
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            this.error(element, "@%s %s must not be private or static. (%s.%s)", annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        if (enclosingElement.getKind() != ElementKind.CLASS) {
            this.error(enclosingElement, "@%s %s may only be contained in classes. (%s.%s)", annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
            this.error(enclosingElement, "@%s %s may not be contained in private classes. (%s.%s)", annotationClass.getSimpleName(), targetThing, enclosingElement.getQualifiedName(), element.getSimpleName());
            hasError = true;
        }

        return hasError;
    }


    private boolean isBindingInWrongPackage(Class<? extends Annotation> annotationClass, Element element) {
        TypeElement enclosingElement = (TypeElement)element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();
        if (qualifiedName.startsWith("android.")) {
            this.error(element, "@%s-annotated class incorrectly in Android framework package. (%s)", annotationClass.getSimpleName(), qualifiedName);
            return true;
        } else if (qualifiedName.startsWith("java.")) {
            this.error(element, "@%s-annotated class incorrectly in Java framework package. (%s)", annotationClass.getSimpleName(), qualifiedName);
            return true;
        } else {
            return false;
        }
    }

    private Id elementToId(Element element, Class<? extends Annotation> annotation, int value) {
        JCTree tree = (JCTree)this.trees.getTree(element, getMirror(element, annotation));
        if (tree != null) {
            this.rScanner.reset();
            tree.accept(this.rScanner);
            if (!this.rScanner.resourceIds.isEmpty()) {
                return (Id)this.rScanner.resourceIds.values().iterator().next();
            }
        }

        return new Id(value);
    }

    private Map<Integer, Id> elementToIds(Element element, Class<? extends Annotation> annotation, int[] values) {
        Map<Integer, Id> resourceIds = new LinkedHashMap();
        JCTree tree = (JCTree)this.trees.getTree(element, getMirror(element, annotation));
        if (tree != null) {
            this.rScanner.reset();
            tree.accept(this.rScanner);
            resourceIds = this.rScanner.resourceIds;
        }

        int[] var6 = values;
        int var7 = values.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            int value = var6[var8];
            ((Map)resourceIds).putIfAbsent(value, new Id(value));
        }

        return (Map)resourceIds;
    }

    private static AnnotationMirror getMirror(Element element, Class<? extends Annotation> annotation) {
        Iterator var2 = element.getAnnotationMirrors().iterator();

        AnnotationMirror annotationMirror;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            annotationMirror = (AnnotationMirror)var2.next();
        } while(!annotationMirror.getAnnotationType().toString().equals(annotation.getCanonicalName()));

        return annotationMirror;
    }

    private static boolean isFieldRequired(Element element) {
        return !hasAnnotationWithName(element, "Nullable");
    }

    private static boolean hasAnnotationWithName(Element element, String simpleName) {
        Iterator var2 = element.getAnnotationMirrors().iterator();

        String annotationName;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            AnnotationMirror mirror = (AnnotationMirror)var2.next();
            annotationName = mirror.getAnnotationType().asElement().getSimpleName().toString();
        } while(!simpleName.equals(annotationName));

        return true;
    }

    private void error(Element element, String message, Object... args) {
        this.printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        this.printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        this.processingEnv.getMessager().printMessage(kind, message, element);
    }

    private void logParsingError(Element element, Class<? extends Annotation> annotation, Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        this.error(element, "Unable to parse @%s binding.\n\n%s", annotation.getSimpleName(), stackTrace);
    }


    private static class RScanner extends TreeScanner {
        Map<Integer, Id> resourceIds;

        private RScanner() {
            this.resourceIds = new LinkedHashMap();
        }

        public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
            Symbol symbol = jcFieldAccess.sym;
            if (symbol.getEnclosingElement() != null && symbol.getEnclosingElement().getEnclosingElement() != null && symbol.getEnclosingElement().getEnclosingElement().enclClass() != null) {
                try {
                    int value = (Integer) Objects.requireNonNull(((Symbol.VarSymbol)symbol).getConstantValue());
                    this.resourceIds.put(value, new Id(value, symbol));
                } catch (Exception var4) {
                }
            }

        }

        public void visitLiteral(JCTree.JCLiteral jcLiteral) {
            try {
                int value = (Integer)jcLiteral.value;
                this.resourceIds.put(value, new Id(value));
            } catch (Exception var3) {
            }

        }

        void reset() {
            this.resourceIds.clear();
        }
    }
}
