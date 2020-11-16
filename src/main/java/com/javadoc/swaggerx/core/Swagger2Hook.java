package com.javadoc.swaggerx.core;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.javadoc.swaggerx.common.util.JacksonUtil;
import com.javadoc.swaggerx.common.util.JavaDocReaderUtil;
import com.javadoc.swaggerx.entity.ApiDoc;
import com.javadoc.swaggerx.entity.DocInfo;
import com.javadoc.swaggerx.entity.GitCloneParam;
import com.javadoc.swaggerx.entity.ModelDoc;
import com.javadoc.swaggerx.git.GitUtil;
import com.sun.javadoc.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import springfox.documentation.schema.Model;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.Tag;
import springfox.documentation.service.*;
import springfox.documentation.spring.web.DocumentationCache;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * documentationCache 内容替换
 */
public class Swagger2Hook {

    private static final Logger logger = LoggerFactory.getLogger(Swagger2Hook.class);

    private final DocumentationCache documentationCache;
    private final WebApplicationContext applicationContext;
    private final Environment environment;


    private DocInfo docInfo;
    private String projectDir;
    /**
     * 是否有源码
     */
    private boolean hasSrc;


    public Swagger2Hook(DocumentationCache documentationCache, WebApplicationContext applicationContext, Environment environment) {
        this.documentationCache = documentationCache;
        this.applicationContext = applicationContext;
        this.environment = environment;
        new Thread(() -> {
            logger.info("=======init easy swagger======");
            try {
                while (true) {
                    if (documentationCache.all().size() > 0) {
                        Swagger2Hook.this.run();
                        break;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        logger.error("Swagger2Hook sleep InterruptedException", e);
                    }
                }
            } finally {
                logger.info("easy swagger end ");
            }
        }).start();
    }


    public void run() {
        //read from local
        // 检查是否有源码
        checkSrc();
        if (hasSrc) {
            docInfo = new DocInfo();
            // 得到所方法的注释
            scanAllUrl();
        }
        logger.info("hookSwaggerDoc apiDoc:{}", JacksonUtil.toSerialize(docInfo));
        Map<String, Documentation> map = documentationCache.all();
        logger.info("all Documentation:{},hasSrc:{}", JacksonUtil.toSerialize(map), hasSrc);
        for (Documentation doc : map.values()) {
            hookSwaggerDoc(doc);
        }
        // 删除源代码
        File parent = new File(projectDir);
        deleteDir(parent);
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
    }

    private void checkSrc() {
        projectDir = GitUtil.cloneProject(new GitCloneParam(environment));
        File parent = new File(projectDir);
        logger.info("parent :{}", JacksonUtil.toSerialize(parent));
        //遍历二级目录（多模块）
        for (File file : Objects.requireNonNull(parent.listFiles())) {
            if (file.isDirectory()) {
                if (file.getName().equals("src")) {
                    hasSrc = true;
                    return;
                }
                for (File child : Objects.requireNonNull(file.listFiles(fileFilter))) {
                    if (child.isDirectory() && child.getName().equals("src")) {
                        hasSrc = true;
                        return;
                    }
                }
            }
        }
    }

    /**
     * 获取所有url
     */
    private void scanAllUrl() {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        // 获取url与类和方法的对应信息
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        Multimap<String, String> multimap = HashMultimap.create();
        map.forEach((req, hn) -> {
            HandlerMethod hm = map.get(req);
            String url = req.getPatternsCondition().getPatterns().iterator().next();
            String className = hm.getBeanType().getName();
            ApiDoc apiDoc = new ApiDoc();
            apiDoc.controllerClass = className;
            apiDoc.methodName = hm.getMethod().getName();
            docInfo.apiMap.put(url, apiDoc);
            multimap.put(className, url);
        });

        if (hasSrc) {
            multimap.asMap().forEach((className, urls) -> {
                File sourceFile = getSourceFile(projectDir, className);
                if (sourceFile != null) {
                    ClassDoc classDoc;
                    if (sourceFile.getName().endsWith(".java") && (classDoc = JavaDocReaderUtil.queryDocLocal(sourceFile.getAbsolutePath())) != null) {
                        parseControllerDoc(classDoc, urls);
                    }
                }
            });
        }

    }


    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }


    private void parseControllerDoc(ClassDoc classDoc, Collection<String> urls) {
        try {

            Map<String, MethodDoc> collect = Arrays.stream(classDoc.methods()).
                    collect(Collectors.toMap(Doc::name, Function.identity(), (o, n) -> n));

            urls.forEach(url -> {
                ApiDoc apiDoc = docInfo.getApiMap().get(url);
                if (apiDoc != null) {
                    apiDoc.controllerDescription = classDoc.commentText();
                    MethodDoc methodDoc = collect.get(apiDoc.methodName);
                    apiDoc.description = methodDoc.commentText();
                    Map<String, String> paramTagMap = Arrays.stream(methodDoc.paramTags()).
                            collect(Collectors.toMap(ParamTag::parameterName, ParamTag::parameterComment, (o, n) -> n));
                    Arrays.stream(methodDoc.parameters()).forEach(parameter -> apiDoc.params.put(parameter.name(), paramTagMap.get(parameter.name())));
                }
            });


        } catch (Exception e) {
            logger.error("parseControllerDoc error", e);
        }
    }


    private File getSourceFile(String parent, String cls) {
        File src = new File(parent, "src");
        if (src.exists() && src.isDirectory()) {
            String javaPath = "main/java/" + getClassPath(cls);
            File sourceFile = new File(src, javaPath + ".kt");
            if (sourceFile.exists()) {
                return sourceFile;
            }
            sourceFile = new File(src, javaPath + ".java");
            if (sourceFile.exists()) {
                return sourceFile;
            }
        } else {//从子目录目录中寻找
            return getSourceFileInChild(parent, cls);
        }
        return null;
    }


    private File getSourceFileInChild(String parent, String cls) {
        File file = new File(parent);
        if (file.isDirectory()) {
            for (File dir : Objects.requireNonNull(file.listFiles(fileFilter))) {
                File sourceFile = getSourceFile(dir.getAbsolutePath(), cls);
                if (sourceFile != null) {
                    return sourceFile;
                }
            }
        }
        return null;
    }

    private static final FileFilter fileFilter = pathname -> pathname.isDirectory() && !pathname.getName().startsWith(".");


    private String getClassPath(String cls) {
        return cls.replace(".", "/");
    }

    private void hookSwaggerDoc(Documentation doc) {
        Multimap<String, ApiListing> apiList = doc.getApiListings();
        for (ApiListing apiListing : apiList.values()) {
            for (Model model : apiListing.getModels().values()) {
                scanModelAndReplace(model);
            }
            for (ApiDescription apiDescription : apiListing.getApis()) {
                String path = apiDescription.getPath();
                ApiDoc apiDoc = docInfo.apiMap.get(path);
                logger.info("hookSwaggerDoc apiDoc:{}", JacksonUtil.toSerialize(apiDoc));
                if (apiDoc != null) {
                    replaceParameter(apiDescription, apiDoc);
                    setOperationTags(apiDescription, apiDoc.controllerDescription);
                    setTags(apiListing.getTags(), apiDoc.controllerDescription, apiDoc.controllerClass);
                }
            }
        }
    }


    private void setOperationTags(ApiDescription apiDescription, String controllerDescription) {
        if (StringUtils.isEmpty(controllerDescription)) {
            return;
        }
        Set<String> tags = new HashSet<>();
        tags.add(controllerDescription);
        for (Operation opt : apiDescription.getOperations()) {
            setField(opt, "tags", tags);
        }

    }

    /**
     * 替换实体文档
     *
     * @param model
     */
    private void scanModelAndReplace(Model model) {
        String cls = model.getType().getErasedType().getName();
        ModelDoc modelDoc = docInfo.modelMap.get(cls);
        if (modelDoc != null) {
            hookModelSwaggerDoc(model, modelDoc);
            return;
        }
        File sourceFile = getSourceFile(projectDir, cls);
        if (sourceFile == null) {
            return;
        }
        ClassDoc classDoc = JavaDocReaderUtil.queryDocLocal(sourceFile.getAbsolutePath());
        if (classDoc == null) {
            return;
        }
        modelDoc = new ModelDoc();
        Map<String, String> fieldMap = new HashMap<>();

        FieldDoc[] fields = classDoc.fields(false);
        for (FieldDoc fieldDoc : fields) {
            fieldMap.put(fieldDoc.name(), fieldDoc.commentText());
        }
        modelDoc.description = classDoc.commentText();
        modelDoc.fieldMap = fieldMap;
        docInfo.modelMap.put(cls, modelDoc);

        hookModelSwaggerDoc(model, modelDoc);
    }

    private void hookModelSwaggerDoc(Model model, ModelDoc modelDoc) {
        setField(model, "description", modelDoc.description);
        Map<String, String> fieldMap = modelDoc.fieldMap;
        for (String key : fieldMap.keySet()) {
            ModelProperty property = model.getProperties().get(key);
            if (property != null) {
                setField(property, "description", fieldMap.get(key));
            }
        }
    }


    private void setTags(Set<Tag> tags, String description, String controllerCls) {
        if (StringUtils.isEmpty(description)) {
            return;
        }
        String simpleClassName = getSimpleClassName(controllerCls);
        for (Tag tag : tags) {
            setField(tag, "name", description);
            setField(tag, "description", simpleClassName);
        }
    }

    private String getSimpleClassName(String controllerCls) {
        try {
            Class cls = Class.forName(controllerCls);
            return cls.getSimpleName();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void replaceParameter(ApiDescription apiDescription, ApiDoc apiDoc) {
        if (apiDoc.params == null) {
            return;
        }
        for (Operation opt : apiDescription.getOperations()) {
            setField(opt, "summary", apiDoc.description);
            for (Parameter parameter : opt.getParameters()) {
                String pn = parameter.getName();
                String desc = apiDoc.params.get(pn);
                if (desc != null) {
                    setParameter(parameter, desc);
                }
            }
        }
    }

    private void setParameter(Parameter parameter, String desc) {
        String[] array = desc.split("\\|");
        if (array.length > 0) {
            setField(parameter, "description", array[0]);
        }
        if (array.length > 1) {
            if ("required".equals(array[1])) {
                setField(parameter, "required", true);
            } else {
                setField(parameter, "defaultValue", array[1]);
            }
        }
        if (array.length > 2) {
            setField(parameter, "defaultValue", array[2]);
        }
    }

    private void setField(Object obj, String name, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("NoSuchFieldException | IllegalAccessException", e);
        }
    }

}