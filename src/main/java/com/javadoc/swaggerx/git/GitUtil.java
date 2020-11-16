package com.javadoc.swaggerx.git;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

/**
 * 拉取远程仓库代码
 *
 * @author shuzhuang.su
 * @date 2020-11-13 18:23
 */
public class GitUtil {

    /**
     * 远程库路径
     */
    @Value("${swaggerx.project.git.address}")
    public static String remotePath ;

    @Value("${swaggerx.project.git.userName}")
    private static String gitUser;
    @Value("${swaggerx.project.git.password}")
    private static String gitPassword;
    @Value("${swaggerx.project.git.branch}")
    private static String projectBranch;

    /**
     * //下载已有仓库到本地路径
     */
    @Value("${swaggerx.project.beta.path}")
    public static String localPath;


    /**
     * 克隆远程库
     */
    public static String cloneProject(){
        //设置远程服务器上的用户名和密码
        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider =new
                UsernamePasswordCredentialsProvider(gitUser,gitPassword);
        //克隆代码库命令
        CloneCommand cloneCommand = Git.cloneRepository();
        try {
            Git git= cloneCommand
                    //设置远程URI
                    .setURI(remotePath)
                    //设置clone下来的分支
                    .setBranch(projectBranch)
                    //设置下载存放路径
                    .setDirectory(new File(localPath))
                    //设置权限验证
                    .setCredentialsProvider(usernamePasswordCredentialsProvider)
                    .call();
            return localPath;
        } catch (GitAPIException ignored) {
        }
        return "";
    }


    public static void main(String[] args) throws GitAPIException {
        //设置远程服务器上的用户名和密码
        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider =new
                UsernamePasswordCredentialsProvider("shuzhuang.su","ssj980910!");

        //克隆代码库命令
        CloneCommand cloneCommand = Git.cloneRepository();

        Git git= cloneCommand.setURI("http://gitlab.corp.11bee.com/backend/honeycomb_supervise.git") //设置远程URI
                .setBranch("master") //设置clone下来的分支
                .setDirectory(new File("/Users/shuzhusngsu/honeycomb_supervise")) //设置下载存放路径
                .setCredentialsProvider(usernamePasswordCredentialsProvider) //设置权限验证
                .call();
    }
}
