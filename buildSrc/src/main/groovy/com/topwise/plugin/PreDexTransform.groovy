package com.topwise.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project
import org.apache.commons.io.FileUtils
import org.apache.commons.codec.digest.DigestUtils

public class PreDexTransform extends Transform {

    Project project
    // 添加构造，为了方便从plugin中拿到project对象，待会有用
    public PreDexTransform(Project project) {
        this.project = project
    }

    // Transfrom在Task列表中的名字
    // TransfromClassesWithPreDexForXXXX
    @Override
    String getName() {
        return "PreDex"
    }

    // 指定input的类型
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    // 指定Transfrom的作用范围
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        // inputs就是输入文件的集合
        inputs.each {TransformInput input ->

            input.directoryInputs.each {DirectoryInput directoryInput->

                //TODO 这里可以对input的文件做处理，比如代码注入！
                project.logger.error "injectDir  start ---  " + directoryInput.file.absolutePath
                Inject.injectDir(directoryInput.file.absolutePath)

                // 获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                // 将input的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
            input.jarInputs.each {JarInput jarInput->

                //TODO 这里可以对input的文件做处理，比如代码注入！
                String jarPath = jarInput.file.absolutePath;
                String projectName = project.rootProject.name;
                project.logger.error "inject start ---  " + jarPath
                project.logger.error "inject start ---  " + projectName
                project.logger.error "inject start ---  " + "exploded-aar"+"/"+projectName
                if(jarPath.endsWith(".jar") && jarPath.contains("exploded-aar"+"/"+projectName)) {
                    project.logger.error "inject start ---"
                    Inject.injectJar(jarPath)
                }
                // 重命名输出文件（同目录copyFile会冲突）
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                project.logger.error "inject start --- 77 name  " + jarInput.name
                project.logger.error "inject start --- 77 " + jarInput.getFile()
                project.logger.error "inject start --- 77 " + jarInput.getName()
                project.logger.error "inject start --- 77 " + jarInput.file.getPath()
                project.logger.error "inject start --- 77 " + jarInput.file.getAbsolutePath()
                project.logger.error "inject start --- 77 " + jarInput.file.getAbsoluteFile()
                project.logger.error "inject start --- 77 " + md5Name
                if(jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0,jarName.length()-4)
                }
                def dest = outputProvider.getContentLocation(jarName+md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
        // outputProvider可以获取outputs的路径
    }
}