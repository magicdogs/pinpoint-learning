package com.magic;

import com.magic.bootstrap.Agent;
import com.magic.bootstrap.BootstrapInfo;
import com.magic.bootstrap.ExampleLoader;
import com.magic.instrumentation.AsmInstrumention;
import com.magic.instrumentation.Instrumentor;
import com.magic.transfrom.TransfromOpearation;
import com.magic.transfrom.TransfromTemplateImpl;
import com.magic.spi.Plugins;
import com.magic.transfrom.ClassTransfromer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Created by magicdog on 2017/4/13.
 */
public class AgentOperation implements Agent{

    private BootstrapInfo bootstrapInfo;
    private TransfromOpearation transfromOpearation;

    private ExampleLoader pluginsClassLoader;

    private Instrumentor instrumentor;

    public AgentOperation(BootstrapInfo bootstrapInfo){
        this.bootstrapInfo = bootstrapInfo;
        this.transfromOpearation = new TransfromTemplateImpl();
        pluginsClassLoader = new ExampleLoader(bootstrapInfo.getPluginsJarFile(),AgentOperation.class.getClassLoader());
        ServiceLoader<Plugins> pluginsServiceLoader = ServiceLoader.load(Plugins.class,pluginsClassLoader);
        Iterator<Plugins> pluginsIterator = pluginsServiceLoader.iterator();
        while(pluginsIterator.hasNext()){
            pluginsIterator.next().setPluginContext(transfromOpearation);
        }
        instrumentor = new AsmInstrumention(pluginsClassLoader);
    }

    public void start() {
        System.out.println("AgentOperation start...");
        bootstrapInfo.getInstrumentation().addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

                final String clzName = className.replaceAll("/",".");
                if(transfromOpearation.hasTransformer(clzName)){
                    List<ClassTransfromer> transformerList = transfromOpearation.getClassFileTransformerByName(clzName);
                    for (ClassTransfromer transformer : transformerList){
                        classfileBuffer = transformer.transform(instrumentor,loader,clzName,classBeingRedefined,protectionDomain,classfileBuffer);
                    }
                }
                return classfileBuffer;
            }
        });
    }

    public void stop() {
        System.out.println("stop...");
    }
}
