package com.magic.bootstrap;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Created by magicdog on 2017/4/12.
 */
public class AgentMainBootstrap {

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {

        if(agentArgs!=null){
            System.out.println("AgentMainBootstrap arguments : " + agentArgs);
        }

        for (Class clz :instrumentation.getAllLoadedClasses()) {
            System.out.println("AgentMainBootstrap agentmain loaded class : " + clz);
        }

        instrumentation.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                System.out.println("AgentMainBootstrap className: " + className);
                return classfileBuffer;
            }
        });

    }
}
