package com.javadoc.swaggerx.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzhusngsu
 */
public class ApiDoc {
    public String description;
    public String controllerDescription;
    public String controllerClass;
    public String methodName;
    public Map<String, String> params = new HashMap<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getControllerDescription() {
        return controllerDescription;
    }

    public void setControllerDescription(String controllerDescription) {
        this.controllerDescription = controllerDescription;
    }

    public String getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
