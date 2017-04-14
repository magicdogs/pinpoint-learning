package com.magic.bootstrap;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * Created by magicdog on 2017/4/13.
 */
public class BootstrapInfo {

    private JarFile[] bootJarFile;
    private URL[] libraryJarFile;
    private URL[] pluginsJarFile;
    private Instrumentation instrumentation;
    private String arguments;


    public JarFile[] getBootJarFile() {
        return bootJarFile;
    }

    public void setBootJarFile(JarFile[] bootJarFile) {
        this.bootJarFile = bootJarFile;
    }


    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public URL[] getLibraryJarFile() {
        return libraryJarFile;
    }

    public void setLibraryJarFile(URL[] libraryJarFile) {
        this.libraryJarFile = libraryJarFile;
    }

    public URL[] getPluginsJarFile() {
        return pluginsJarFile;
    }

    public void setPluginsJarFile(URL[] pluginsJarFile) {
        this.pluginsJarFile = pluginsJarFile;
    }
}
