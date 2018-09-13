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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class OpheliaProcessor extends AbstractProcessor {
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

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver()) {
            Set<TypeElement> elements = getTypeElementsByAnnotationType(set, roundEnvironment.getRootElements());

            for (TypeElement typeElement : elements) {
                //包名
                String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                //类名
                String typeName = typeElement.getSimpleName().toString();
                //全称类名
                ClassName className = ClassName.get(packageName, typeName);
                //自动生成类全称名
                ClassName autoGenerationClassName = ClassName.get(packageName,
                        NameUtils.getAutoGeneratorTypeName(typeName));

                //构建自动生成的类
                TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(autoGenerationClassName)
                        .addModifiers(Modifier.PUBLIC);

                //构造方法
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

                for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                    BindView bindView = variableElement.getAnnotation(BindView.class);
                    if (bindView != null) {
                        bindViewBuilder.addStatement("$N.$N=($T)$N.findViewById($L)",
                                "activity",
                                variableElement.getSimpleName(),
                                variableElement,
                                "activity",
                                bindView.value()
                        );
                    }
                }

                typeBuilder.addMethod(bindViewBuilder.build());

                // set OnClick
                MethodSpec.Builder setOnClickListenerBuilder = MethodSpec.methodBuilder("setOnClickListener")
                        .addModifiers(Modifier.PRIVATE)
                        .returns(TypeName.VOID)
                        .addParameter(className, "activity", Modifier.FINAL);

                ClassName viewClassName = ClassName.get("android.view", "View");
                ClassName onClickListenerClassNAme = ClassName.get("android.view", "View", "OnClickListener");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    OnClick onClick = executableElement.getAnnotation(OnClick.class);
                    if (onClick != null) {
                        //构建匿名class
                        TypeSpec typeSpec = TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(onClickListenerClassNAme)
                                .addMethod(MethodSpec.methodBuilder("onClick")
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(viewClassName, "View")
                                        .returns(TypeName.VOID)
                                        .addStatement("$N.$N($N)",
                                                "activity",
                                                executableElement.getSimpleName(),
                                                "View")
                                        .build())
                                .build();

                        setOnClickListenerBuilder.addStatement("$N.findViewById($L).setOnClickListener($L)",
                                "activity",
                                onClick.value(),
                                typeSpec);
                    }
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
            }
        }
        return true;
    }

    private Set<TypeElement> getTypeElementsByAnnotationType(Set<? extends TypeElement> annotations, Set<? extends Element> elements) {
        Set<TypeElement> typeElements = new HashSet<>();
        for (Element element : elements) {
            if (element instanceof TypeElement) {
                boolean found = false;
                for (Element subElement : element.getEnclosedElements()) {
                    for (AnnotationMirror annotationMirror : subElement.getAnnotationMirrors()) {
                        for (TypeElement annotation : annotations) {
                            if (annotationMirror.getAnnotationType().asElement().equals(annotation)) {
                                typeElements.add((TypeElement) element);
                                found = true;
                                break;
                            }
                        }
                        if (found) break;
                    }
                    if (found) break;
                }
            }

        }
        return typeElements;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class);
        annotations.add(OnClick.class);
        return annotations;
    }
}
