package com.nd.dwebview.compiler.processors;

import static javax.lang.model.element.Modifier.PUBLIC;

import com.google.auto.service.AutoService;
import com.nd.dwebview.compiler.utils.Consts;
import com.nd.dwebview.facade.annotations.JsApi;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * @author cwj
 * @date 2021/10/14 14:41
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({Consts.ANNOTATION_TYPE_JSAPI})
public class JsApiProcessor extends BaseProcessor {

    private Map<String, ClassName> mJsApiMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (CollectionUtils.isNotEmpty(annotations)) {
            Set<? extends Element> jsApiElements = roundEnv.getElementsAnnotatedWith(JsApi.class);
            try {
                mLogger.info(">>> Found JsApi, start... <<<");
                this.parseRoutes(jsApiElements);
            } catch (Exception e) {
                mLogger.error(e);
            }
            return true;
        }
        mLogger.info(">>> Not Found JsApi, end... <<<");
        return false;
    }

    private void parseRoutes(Set<? extends Element> jsApiElements) throws IOException {
        if (CollectionUtils.isEmpty(jsApiElements)) {
            return;
        }
        mLogger.info(">>> Found JsApi, size is " + jsApiElements.size() + " <<<");

        //将namespace和对应的class名称保存起来
        for (Element element : jsApiElements) {
            JsApi jsApi = element.getAnnotation(JsApi.class);
            TypeElement type_class = mElementUtils.getTypeElement(element.asType().toString());
            mJsApiMap.put(jsApi.nameSpace(), ClassName.get(type_class));
        }

        //Map<String, Class>
        ParameterizedTypeName inputMapType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(Class.class)
        );

        // loadInto
        ParameterSpec groupParamSpec = ParameterSpec.builder(inputMapType, "atlas").build();
        MethodSpec.Builder loadIntoMethodBuilder = MethodSpec.methodBuilder(Consts.METHOD_LOAD_INTO)
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(groupParamSpec);

        for (Map.Entry<String, ClassName> entry : mJsApiMap.entrySet()) {
            loadIntoMethodBuilder.addStatement(
                    "atlas.put($S, $T.class)", entry.getKey(),
                    entry.getValue());
        }
        TypeElement type_IRouteGroup = mElementUtils.getTypeElement(Consts.PACKAGE_OF_IJSAPI);
        // 生成文件
        String fileName = Consts.NAME_OF_JSAPI + mModuleName;
        JavaFile.builder(Consts.PACKAGE_OF_GENERATE_FILE,
                TypeSpec.classBuilder(fileName)
                        .addJavadoc(Consts.WARNING_TIPS)
                        .addSuperinterface(ClassName.get(type_IRouteGroup))
                        .addModifiers(PUBLIC)
                        .addMethod(loadIntoMethodBuilder.build())
                        .build()
        ).build().writeTo(mFiler);
    }
}
