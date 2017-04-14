package com.magic.transfrom;

import com.magic.instrumentation.Instrumentor;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by magicdog on 2017/4/13.
 */
public interface ClassTransfromer {
    byte[]
    transform(  Instrumentor instrumentor,
                ClassLoader         loader,
                String              className,
                Class<?>            classBeingRedefined,
                ProtectionDomain protectionDomain,
                byte[]              classfileBuffer)
            throws IllegalClassFormatException;
}
