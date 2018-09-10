package com.jascal.ophelia_compiler;

import com.google.auto.service.AutoService;
import com.jascal.ophelia_annotation.BindView;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
public class OpheliaProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
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

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        // 将获取到的bindview细分到每个class
        Map<Element, List<Element>> elementMap = new LinkedHashMap<>();

        for (Element element : elements) {
            // 返回activity
            Element enclosingElement = element.getEnclosingElement();

            List<Element> bindViewElements = elementMap.get(enclosingElement);
            if (bindViewElements == null) {
                bindViewElements = new ArrayList<>();
                elementMap.put(enclosingElement, bindViewElements);
            }
            bindViewElements.add(element);
        }

        // 生成代码
        for (Map.Entry<Element, List<Element>> entrySet : elementMap.entrySet()) {
            Element enclosingElement = entrySet.getKey();
            List<Element> bindViewElements = entrySet.getValue();

            // public final class xxxActivity_ViewBinding implements Unbinder
            // 获取activity的类名
            String activityClassNameStr = enclosingElement.getSimpleName().toString();
            System.out.println("------------->" + activityClassNameStr);
            ClassName activityClassName = ClassName.bestGuess(activityClassNameStr);
            ClassName unBinderClassName = ClassName.get("com.jascal.ophelia_api", "UnBinder");
            TypeSpec.Builder classBuilder =
                    TypeSpec.classBuilder(activityClassNameStr + "_ViewBinding")
                            .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                            .addSuperinterface(unBinderClassName)
                            // 添加属性 private MainActivity target;
                            .addField(activityClassName, "target", Modifier.PRIVATE);

            // unbind()
            ClassName callSuperClassName = ClassName.get("android.support.annotation", "CallSuper");
            MethodSpec.Builder unbindMethodBuilder = MethodSpec.methodBuilder("unbind")
                    .addAnnotation(Override.class)
                    .addAnnotation(callSuperClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            // 构造函数
            MethodSpec.Builder constructorMethodBuilder = MethodSpec.constructorBuilder()
                    .addParameter(activityClassName, "target")
                    .addModifiers(Modifier.PUBLIC)
                    // this.target = target
                    .addStatement("this.target = target");

            for (Element bindViewElement : bindViewElements) {
                // textview
                String fieldName = bindViewElement.getSimpleName().toString();
                // Utils
                ClassName utilsClassName = ClassName.get("com.jascal.ophelia_api", "Utils");
                // R.id.textview
                int resourceId = bindViewElement.getAnnotation(BindView.class).value();
                // target.textview = Utils.findViewById(target, R.id.textview)
                constructorMethodBuilder.addStatement("target.$L = $T.findViewById(target, $L)", fieldName, utilsClassName, resourceId);
                // target.textview = null
                unbindMethodBuilder.addStatement("target.$L = null", fieldName);
            }


            classBuilder.addMethod(unbindMethodBuilder.build())
                    .addMethod(constructorMethodBuilder.build());

            // 获取包名
            String packageName = elementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();

            try {
                JavaFile.builder(packageName, classBuilder.build())
                        .addFileComment("自己写的ButterKnife生成的代码，不要修改！！！")
                        .build().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class);
        return annotations;
    }
}
