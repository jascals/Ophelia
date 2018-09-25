package com.jascal.ophelia_compiler;

import com.google.auto.service.AutoService;
import com.jascal.ophelia_annotation.BindView;
import com.jascal.ophelia_annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class OpheliaProcessor extends AbstractProcessor {
    private Map<String, Proxy> proxyMap = new HashMap<>();
    private Elements elementUtils;
    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        Set<Class<? extends Annotation>> supportedAnnotations = getSupportedAnnotations();
        for (Class<? extends Annotation> supportedAnnotation : supportedAnnotations) {
            types.add(supportedAnnotation.getCanonicalName());
        }
        return types;
    }

    private boolean isValid(Element element) {
        if (element.getModifiers().contains(Modifier.ABSTRACT) || element.getModifiers().contains(Modifier.PRIVATE)) {
            error(element, "%s must could not be abstract or private");
            return true;
        }
        return false;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private boolean isAnnotationWithMethod(RoundEnvironment environment, Class<? extends Annotation> clazz) {
        Set<? extends Element> elements = environment.getElementsAnnotatedWith(clazz);
        for (Element element : elements) {
            if (isValid(element)) {
                return false;
            }
            ExecutableElement method = (ExecutableElement) element;
            TypeElement typeElement = (TypeElement) method.getEnclosingElement();
            String typeName = typeElement.getQualifiedName().toString();

            Proxy proxy = proxyMap.get(typeName);
            if (proxy == null) {
                proxy = new Proxy(elementUtils, typeElement);
                proxyMap.put(typeName, proxy);
            }

            int size = method.getParameters().size();
            Annotation annotation = method.getAnnotation(clazz);

            String methodName = method.getSimpleName().toString();
            if (annotation instanceof BindView) {
                int value = ((BindView) annotation).value();
                proxy.bindViewMap.put(methodName, value);
            } else if (annotation instanceof OnClick) {
                int value = ((OnClick) annotation).value();
                proxy.onClickMap.put(methodName, value);
            } else {
                error(method, "%s not support.", method);
                return false;
            }

        }
        return true;
    }

    private boolean isAnnotationWithVariable(RoundEnvironment environment, Class<? extends Annotation> clazz){
        return true;
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        proxyMap.clear();
//        if (!isAnnotationWithMethod(roundEnvironment, BindView.class)) return false;
        if (!isAnnotationWithMethod(roundEnvironment, OnClick.class)) return false;

        for (Proxy proxy : proxyMap.values()) {
            writeToFile(proxy);
        }

        return true;
    }

    private void writeToFile(Proxy proxy) {
        TypeElement typeElement = proxy.getElement();
        String packageName = proxy.getPackageName();

        ClassName viewClassName = ClassName.get("android.view", "View");
        ClassName onClickListenerClassNAme = ClassName.get("android.view", "View", "OnClickListener");

        ClassName autoGenerationClassName = ClassName.get(packageName, proxy.getInfo());

        String typeName = proxy.getTypeName();
        ClassName className = ClassName.get(packageName, typeName);

        if (typeName.contains("Activity")) {
            // build class start
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(autoGenerationClassName)
                    .addModifiers(Modifier.PUBLIC);

            // constructor
            typeBuilder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(className, "activity")
                    .addStatement("$N($N)", "bindView", "activity")
                    .addStatement("$N($N)", "setOnClickListener", "activity")
                    .build());

            // bind View
            MethodSpec.Builder bindViewBuilder = MethodSpec.methodBuilder("bindView")
                    .addModifiers(Modifier.PRIVATE)
                    .returns(TypeName.VOID)
                    .addParameter(className, "activity");
            for (Map.Entry<String, Integer> entry : proxy.bindViewMap.entrySet()) {
                String methodName = entry.getKey();
                int value = entry.getValue();
                bindViewBuilder.addStatement("$N.$N=($T)$N.findViewById($L)",
                        "activity",
                        methodName,
                        methodName,
                        "activity",
                        value
                );
            }
            typeBuilder.addMethod(bindViewBuilder.build());

            // set OnClick
            MethodSpec.Builder setOnClickListenerBuilder = MethodSpec.methodBuilder("setOnClickListener")
                    .addModifiers(Modifier.PRIVATE)
                    .returns(TypeName.VOID)
                    .addParameter(className, "activity", Modifier.FINAL);
            for (Map.Entry<String, Integer> entry : proxy.onClickMap.entrySet()) {
                String methodName = entry.getKey();
                int value = entry.getValue();
                TypeSpec typeSpec = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(onClickListenerClassNAme)
                        .addMethod(MethodSpec.methodBuilder("onClick")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(viewClassName, "view")
                                .returns(TypeName.VOID)
                                .addStatement("$N.$N($N)",
                                        "activity",
                                        methodName,
                                        "view")
                                .build())
                        .build();

                setOnClickListenerBuilder.addStatement("$N.findViewById($L).setOnClickListener($L)",
                        "activity",
                        value,
                        typeSpec);
            }
            typeBuilder.addMethod(setOnClickListenerBuilder.build());

            //写入java文件
            try {
                JavaFile.builder(packageName, typeBuilder.build())
                        .addFileComment("自己写的ButterKnife生成的代码，不要修改！！！")
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), typeElement);
            }

        } else if (typeName.contains("Fragment")) {
            // build class start
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(autoGenerationClassName)
                    .addModifiers(Modifier.PUBLIC);

            // constructor
            typeBuilder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(className, "fragment")
                    .addParameter(viewClassName, "view")
                    .addStatement("$N($N,$N)", "bindView", "fragment", "view")
                    .addStatement("$N($N,$N)", "setOnClickListener", "fragment", "view")
                    .build());

            // bind View
            MethodSpec.Builder bindViewInFragmentBuilder = MethodSpec.methodBuilder("bindView")
                    .addModifiers(Modifier.PRIVATE)
                    .returns(TypeName.VOID)
                    .addParameter(className, "fragment")
                    .addParameter(viewClassName, "view");
            for (Map.Entry<String, Integer> entry : proxy.bindViewMap.entrySet()) {
                String methodName = entry.getKey();
                int value = entry.getValue();
                bindViewInFragmentBuilder.addStatement("$N.$N=($T)$N.findViewById($L)",
                        "fragment",
                        methodName,
                        methodName,
                        "view",
                        value
                );
            }
            typeBuilder.addMethod(bindViewInFragmentBuilder.build());

            MethodSpec.Builder setOnClickListenerInFragmentBuilder = MethodSpec.methodBuilder("setOnClickListener")
                    .addModifiers(Modifier.PRIVATE)
                    .returns(TypeName.VOID)
                    .addParameter(className, "fragment", Modifier.FINAL)
                    .addParameter(viewClassName, "view", Modifier.FINAL);

            for (Map.Entry<String, Integer> entry : proxy.onClickMap.entrySet()) {
                String methodName = entry.getKey();
                int value = entry.getValue();
                TypeSpec typeSpec = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(onClickListenerClassNAme)
                        .addMethod(MethodSpec.methodBuilder("onClick")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(viewClassName, "view")
                                .returns(TypeName.VOID)
                                .addStatement("$N.$N($N)",
                                        "fragment",
                                        methodName,
                                        "view")
                                .build())
                        .build();

                setOnClickListenerInFragmentBuilder.addStatement("$N.findViewById($L).setOnClickListener($L)",
                        "view",
                        value,
                        typeSpec);
            }
            typeBuilder.addMethod(setOnClickListenerInFragmentBuilder.build());

            try {
                JavaFile.builder(packageName, typeBuilder.build())
                        .addFileComment("自己写的ButterKnife生成的代码，不要修改！！！")
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), typeElement);
            }
        }
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class);
        annotations.add(OnClick.class);
        return annotations;
    }
}
