package com.magic.instrumentation;

/**
 * Created by magicdog on 2017/4/13.
 */
public interface Instrumentor {

    InstrumentionClass getInstrucmentClass(ClassLoader loader, String className, byte[] classfileBuffer);

}
