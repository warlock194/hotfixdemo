package com.topwise.plugin

import com.android.tools.lint.detector.api.Project
import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.io.FileUtils
import sun.rmi.runtime.Log
import org.gradle.api.Project

/**
 * Created by AItsuki on 2016/4/7.
 * 注入代码分为两种情况，一种是目录，需要遍历里面的class进行注入
 * 另外一种是jar包，需要先解压jar包，注入代码之后重新打包成jar
 */
public class Inject {

    private static ClassPool pool = ClassPool.getDefault()

    /**
     * 添加classPath到ClassPool
     * @param libPath
     */
    public static void appendClassPath(String libPath) {
        pool.appendClassPath(libPath)
    }

    /**
     * 遍历该目录下的所有class，对所有class进行代码注入。
     * 其中以下class是不需要注入代码的：
     * --- 1. R文件相关
     * --- 2. 配置文件相关（BuildConfig）
     * --- 3. Application
     * @param path 目录的路径
     */
    public static void injectDir(String path,Project project) {
        pool.appendClassPath(path)
        File dir = new File(path)
        if(dir.isDirectory()) {
            dir.eachFileRecurse { File file ->

                String filePath = file.absolutePath
                if (filePath.endsWith(".class")
                        && !filePath.contains('R$')
                        && !filePath.contains('R.class')
                        && !filePath.contains("BuildConfig.class")
                        // 这里是application的名字，可以通过解析清单文件获得，先写死了
                        && !filePath.contains("MainApplication.class")) {
                    // 这里是应用包名，也能从清单文件中获取，先写死
                    project.logger.error "filePath --- " + filePath.indexOf("com\\example\\topwise\\hotfixcat")
                    int index = filePath.indexOf("com\\example\\topwise\\hotfixcat")
                    if (index != -1) {
                        int end = filePath.length() - 6 // .class = 6
                        String className = filePath.substring(index, end).replace('\\', '.').replace('/','.')
                        project.logger.error "injectclass start ---  "
                        injectClass(className, path,project)
                    }
                }
            }
        }
    }

    /**
     * 这里需要将jar包先解压，注入代码后再重新生成jar包
     * @path jar包的绝对路径
     */
    public static void injectJar(String path,Project project) {
        if (path.endsWith(".jar")) {
            File jarFile = new File(path)
            project.logger.error "inject start ---  " + jarFile.absolutePath

            // jar包解压后的保存路径
            String jarZipDir = jarFile.getParent() +"/"+jarFile.getName().replace('.jar','')

            // 解压jar包, 返回jar包中所有class的完整类名的集合（带.class后缀）
            List classNameList = JarZipUtil.unzipJar(path, jarZipDir)

            // 删除原来的jar包
            jarFile.delete()

            // 注入代码
            pool.appendClassPath(jarZipDir)
            for(String className : classNameList) {
                project.logger.error "11111111111 " + className
                if (className.endsWith(".class")
                        && !className.contains('R$')
                        && !className.contains('R.class')
                        && !className.contains("AssetsUtil.class")
                        &&  !className.contains("HotPatch.class")
                        &&  !className.contains("ReflectUtil.class")
                        &&  !className.contains("BuildConfig.class")
                        &&  !className.contains("AntilazyLoad.class")
                ) {
                    className = className.substring(0, className.length()-6)
                    injectClass(className, jarZipDir,project)
                }
            }

            // 从新打包jar
            JarZipUtil.zipJar(jarZipDir, path)

            // 删除目录
            FileUtils.deleteDirectory(new File(jarZipDir))
        }
    }

    private static void injectClass(String className, String path,Project project) {
        CtClass c = pool.getCtClass(className)
        if (c.isFrozen()) {
            c.defrost()
        }
        def constructor = c.getConstructors()[0];
        // 这里需要输入完整类名，否则javassist会报错
        project.logger.error "start write  ---  " + className
        constructor.insertAfter("System.out.println(com.example.hackdex.AntilazyLoad.class);")
//        constructor.insertAfter("System.out.println(com.aitsuki.hack.AntilazyLoad.class);")
        c.writeFile(path)
        project.logger.error "end write ---  "
    }

}