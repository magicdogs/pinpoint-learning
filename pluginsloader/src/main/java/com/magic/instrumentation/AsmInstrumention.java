package com.magic.instrumentation;

import com.magic.bootstrap.ExampleLoader;

/**
 * Created by magicdog on 2017/4/13.
 */
public class AsmInstrumention implements Instrumentor {

    private ExampleLoader pluginsClassLoader;

    public AsmInstrumention(ExampleLoader pluginsClassLoader) {
        this.pluginsClassLoader = pluginsClassLoader;
    }

    public InstrumentionClass getInstrucmentClass(ClassLoader loader, String className, byte[] classfileBuffer) {
        return new AsmInstrumentClass(this.pluginsClassLoader,loader,className,classfileBuffer);
    }
}
