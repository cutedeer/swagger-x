package com.javadoc.swaggerx.common.util;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;

import java.util.Arrays;

/**
 * @author shuzhuang.su
 * @date 2020-11-12 19:51
 */
public class JavaDocReaderUtil {

    private static RootDoc root;

    /**
     * 一个简单Doclet,收到 RootDoc对象保存起来供后续使用
     * // 参见参考资料6
     */
    public static class Doclet {

        public Doclet() {
        }

        public static boolean start(RootDoc root) {
            JavaDocReaderUtil.root = root;
            return true;
        }
    }

    /**
     * 显示DocRoot中的基本信息
     */
    public static ClassDoc show() {
        ClassDoc[] classes = root.classes();
        if (classes != null && classes.length > 0) {
            return classes[0];
        }
        return null;
    }

    public JavaDocReaderUtil() {
    }

    /**
     * 读本地源代码
     *
     * @param filePath
     * @return
     */
    public static ClassDoc queryDocLocal(String filePath) {
        com.sun.tools.javadoc.Main.execute(new String[]{"-doclet",
                        Doclet.class.getName(),
                        "-encoding", "utf-8",
                        filePath
                }
        );
        return show();
    }


    /**
     * 读本地源代码
     *
     * @param filePath
     * @return
     */
    public static ClassDoc queryDocFromJar(String filePath) {
        com.sun.tools.javadoc.Main.execute(new String[]{"-doclet",
                        Doclet.class.getName(),
                        "-encoding", "utf-8", "-docletpath",
                        Doclet.class.getResource("/").getPath(),
                        filePath
                }
        );
        return show();
    }


    public static void main(final String... args) throws Exception {
        // 调用com.sun.tools.javadoc.Main执行javadoc,参见 参考资料3
        // javadoc的调用参数，参见 参考资料1
        // -doclet 指定自己的docLet类名
        // -classpath 参数指定 源码文件及依赖库的class位置，不提供也可以执行，但无法获取到完整的注释信息(比如annotation)
        // -encoding 指定源码文件的编码格式
        com.sun.tools.javadoc.Main.execute(new String[]{"-doclet",
                        Doclet.class.getName(),
// 因为自定义的Doclet类并不在外部jar中，就在当前类中，所以这里不需要指定-docletpath 参数，
//				"-docletpath",
//				Doclet.class.getResource("/").getPath(),
                        "-encoding", "utf-8",
//                "-classpath",
//                "D:/j/facelog/facelog-main/target/classes;D:/j/facelog/db/target/classes;D:/j/facelog/db/sql2java/lib/swift-annotations-0.14.2.jar",
// 获取单个代码文件FaceLogDefinition.java的javadoc
                        "/Users/shuzhusngsu/IdeaProjects/honeycomb_supervise/src/main/java/com/bee/honeycomb/web/controller/admin/LessonCallTempController.java"
                }
        );
        ClassDoc show = show();

        MethodDoc[] methods = show.methods();
        for (MethodDoc doc:methods){
            System.out.println(doc.name());
            System.out.println(doc.commentText());
            Parameter[] parameters = doc.parameters();

            Arrays.stream(parameters).forEach(x->{
                System.out.print(x.toString());
            });
            System.out.println();
        }

        System.out.println(show.qualifiedName());
    }
}
