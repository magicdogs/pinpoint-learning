package com.magic.instrumentation;

/**
 * Created by magicdog on 2017/4/13.
 */
public interface InstrumentionClass {
    void addField(String fieldType,String fieldName);
    void addInterceptor(String className);
    byte[] toByte();
}
