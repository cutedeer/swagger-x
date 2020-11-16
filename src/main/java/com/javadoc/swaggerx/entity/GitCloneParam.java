package com.javadoc.swaggerx.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

/**
 * 拉取远程代码参数
 *
 * @author shuzhuang.su
 * @date 2020-11-16 12:19
 */
public class GitCloneParam {

    /**
     * 远程库路径
     */
    public final String remotePath;
    /**
     * 用户名
     */
    private final String gitUser;
    /**
     * 密码
     */
    private final String gitPassword;
    /**
     * 分支
     */
    private final String projectBranch;

    /**
     * 下载已有仓库到本地路径
     */
    public final String localPath;

    public GitCloneParam(Environment environment) {
        this.remotePath = environment.getProperty("swaggerx.project.git.address");
        this.gitUser = environment.getProperty("swaggerx.project.git.userName");
        this.gitPassword = environment.getProperty("swaggerx.project.git.password");
        this.projectBranch = environment.getProperty("swaggerx.project.git.branch");
        this.localPath = environment.getProperty("swaggerx.project.git.branch");
    }

    public String getRemotePath() {
        return remotePath;
    }

    public String getGitUser() {
        return gitUser;
    }

    public String getGitPassword() {
        return gitPassword;
    }

    public String getProjectBranch() {
        return projectBranch;
    }

    public String getLocalPath() {
        return localPath;
    }
}
