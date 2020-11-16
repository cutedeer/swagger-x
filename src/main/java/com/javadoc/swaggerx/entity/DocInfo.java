package com.javadoc.swaggerx.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzhusngsu
 */
public class DocInfo {
    public Map<String, ApiDoc> apiMap = new HashMap<>();
    public Map<String, ModelDoc> modelMap = new HashMap<>();

    public Map<String, ApiDoc> getApiMap() {
        return apiMap;
    }

    public void setApiMap(Map<String, ApiDoc> apiMap) {
        this.apiMap = apiMap;
    }
}
