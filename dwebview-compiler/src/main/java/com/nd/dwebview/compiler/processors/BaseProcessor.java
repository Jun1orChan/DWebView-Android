package com.nd.dwebview.compiler.processors;

import com.nd.dwebview.compiler.utils.Consts;
import com.nd.dwebview.compiler.utils.Logger;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author cwj
 * @date 2021/10/14 14:41
 */
public abstract class BaseProcessor extends AbstractProcessor {

    Filer mFiler;
    Logger mLogger;
    Types mTypes;
    Elements mElementUtils;
    /**
     * Module name, maybe its 'app' or others
     */
    String mModuleName = null;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mFiler = processingEnv.getFiler();
        mTypes = processingEnv.getTypeUtils();
        mElementUtils = processingEnv.getElementUtils();
        mLogger = new Logger(processingEnv.getMessager());

        // Attempt to get user configuration [moduleName]
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            mModuleName = options.get(Consts.KEY_MODULE_NAME);
        }

        if (StringUtils.isNotEmpty(mModuleName)) {
            mModuleName = mModuleName.replaceAll("[^0-9a-zA-Z_]+", "");
            mLogger.info("The user has configuration the module name, it was [" + mModuleName + "]");
        } else {
            mLogger.error(Consts.NO_MODULE_NAME_TIPS);
            throw new RuntimeException("DWebView::Compiler >>> No module name, for more information, look at gradle log.");
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(Consts.KEY_MODULE_NAME);
        }};
    }
}
