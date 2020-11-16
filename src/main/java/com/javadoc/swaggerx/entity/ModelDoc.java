package com.javadoc.swaggerx.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 实体文档信息
 *
 * @author shuzhusngsu
 */
public class ModelDoc {

    public String description;
    /**
     * 属性文档信息
     */
    public Map<String, String> fieldMap = new HashMap<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, String> fieldMap) {
        this.fieldMap = fieldMap;
    }
}
