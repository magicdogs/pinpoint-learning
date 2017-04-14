package com.magic.bootstrap;

import java.io.File;
import java.io.FileFilter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * Created by magicdog on 2017/4/12.
 */
public class PreMainBootstrap {

    private static final String BOOT_PATH = "./out/boot/";

    private static final String LIBRARY_PATH = "./out/lib/";

    private static final String PLUGINS_PATH = "./out/plugins/";

    private static final String BOOT_CLASS = "com.magic.AgentOperation";

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if(agentArgs!=null){
            System.out.println("PreMainBootstrap arguments : " + agentArgs);
        }
        try {
            BootstrapInfo bootstrapInfo = getBootstrapInfo(agentArgs,instrumentation);
            appendBootstrapClassLoader(bootstrapInfo.getBootJarFile(),instrumentation);
            ExampleLoader exampleLoader = new ExampleLoader(bootstrapInfo.getLibraryJarFile(),PreMainBootstrap.class.getClassLoader());
            Thread current = Thread.currentThread();
            ClassLoader before = current.getContextClassLoader();
            current.setContextClassLoader(exampleLoader);
            try {
                Class agentClass = exampleLoader.loadClass(BOOT_CLASS);
                Constructor constructor = agentClass.getDeclaredConstructor(new Class[]{BootstrapInfo.class});
                Object agent = constructor.newInstance(bootstrapInfo);
                if(agent instanceof Agent){
                    ((Agent) agent).start();
                }
            }finally {
                current.setContextClassLoader(before);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static URL[] getBootJarFileURL(String libraryPath) throws Exception{
        File[] fs = getJarFiles(libraryPath);
        URL[] urls = new URL[fs.length];
        for (int i = 0 ; i < fs.length; i ++){
            urls[i] = fs[i].toURL();
        }
        return urls;
    }

    private static void appendBootstrapClassLoader(JarFile[] jfs ,Instrumentation instrumentation) throws Exception{
        for (JarFile f: jfs) {
            instrumentation.appendToBootstrapClassLoaderSearch(f);
        }
    }

    private static JarFile[] getBootJarFile(String path) throws Exception{
        File[] fs = getJarFiles(path);
        JarFile[] jfs = new JarFile[fs.length];
        for (int i = 0 ; i < fs.length; i ++){
            jfs[i] = new JarFile(fs[i]);
        }
        return jfs;
    }

    private static BootstrapInfo getBootstrapInfo(String agentArgs, Instrumentation instrumentation) throws Exception{
        JarFile[] jfsboot = getBootJarFile(BOOT_PATH);
        URL[] jfslibrary = getBootJarFileURL(LIBRARY_PATH);
        URL[] jfsplugins = getBootJarFileURL(PLUGINS_PATH);
        BootstrapInfo bootstrapInfo = new BootstrapInfo();
        bootstrapInfo.setBootJarFile(jfsboot);
        bootstrapInfo.setLibraryJarFile(jfslibrary);
        bootstrapInfo.setPluginsJarFile(jfsplugins);
        bootstrapInfo.setArguments(agentArgs);
        bootstrapInfo.setInstrumentation(instrumentation);
        return  bootstrapInfo;
    }

    private static File[] getJarFiles(String path) {
        File root = new File(path);
        System.out.println(root.getAbsolutePath());
        File[] fs = root.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                String pathName = pathname.getName();
                if(pathName.endsWith(".jar") || pathName.endsWith(".JAR")){
                    return true;
                }
                return false;
            }
        });
        return  fs;
    }
}
