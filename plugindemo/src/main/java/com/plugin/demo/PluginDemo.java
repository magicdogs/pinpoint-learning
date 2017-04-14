package com.plugin.demo;

import com.magic.instrumentation.InstrumentionClass;
import com.magic.instrumentation.Instrumentor;
import com.magic.transfrom.TransfromTemplate;
import com.magic.spi.Plugins;
import com.magic.transfrom.ClassTransfromer;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by magicdog on 2017/4/12.
 */
public class PluginDemo implements Plugins{

    public void setPluginContext(TransfromTemplate transfromTemplate) {
        transfromTemplate.addClassTransfrom("com.magic.example.Example1", new ClassTransfromer() {

            public byte[] transform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                InstrumentionClass instrumentionClass = instrumentor.getInstrucmentClass(loader,className,classfileBuffer);
                System.out.println("PluginDemo Transfrom com.magic.example.Example1 operation");
                instrumentionClass.addInterceptor("com.plugin.demo.Example1ConstructorInterceptor");
                instrumentionClass.addInterceptor("com.plugin.demo.Example1MethodInterceptor");
                instrumentionClass.addField("java.lang.String","pname");
                return instrumentionClass.toByte();
            }
        });
    }


}
