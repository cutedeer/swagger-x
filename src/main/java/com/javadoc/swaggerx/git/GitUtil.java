package com.javadoc.swaggerx.git;

import com.javadoc.swaggerx.entity.GitCloneParam;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;

/**
 * 拉取远程仓库代码
 *
 * @author shuzhuang.su
 * @date 2020-11-13 18:23
 */
public class GitUtil {


    /**
     * 克隆远程库
     */
    public static String cloneProject(GitCloneParam param){
        //设置远程服务器上的用户名和密码
        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider =new
                UsernamePasswordCredentialsProvider(param.getGitUser(),param.getGitPassword());
        //克隆代码库命令
        CloneCommand cloneCommand = Git.cloneRepository();
        try {
            Git git= cloneCommand
                    //设置远程URI
                    .setURI(param.getRemotePath())
                    //设置clone下来的分支
                    .setBranch(param.getProjectBranch())
                    //设置下载存放路径
                    .setDirectory(new File(param.getLocalPath()))
                    //设置权限验证
                    .setCredentialsProvider(usernamePasswordCredentialsProvider)
                    .call();
            return param.getLocalPath();
        } catch (GitAPIException ignored) {
        }
        return "";
    }

}
