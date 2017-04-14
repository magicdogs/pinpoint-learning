package com.magic.instrumentation;

import com.magic.bootstrap.ExampleLoader;
import com.magic.interceptor.AroundInterceptor;
import com.magic.interceptor.InterceptorRegister;
import com.magic.util.TargetConstructor;
import com.magic.util.TargetMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by magicdog on 2017/4/13.
 */
public class AsmInstrumentClass implements InstrumentionClass{

    private List<Field> fieldList = new LinkedList<Field>();

    private List<Method> methodList = new LinkedList<Method>();

    private ExampleLoader pluginsClassLoader;
    private ClassLoader classLoader;
    private String className;
    private byte[] byteBuf;

    public AsmInstrumentClass(ExampleLoader pluginsClassLoader, ClassLoader loader, String className, byte[] classfileBuffer) {
        this.byteBuf = classfileBuffer;
        this.className = className;
        this.classLoader = loader;
        this.pluginsClassLoader = pluginsClassLoader;
    }

    public void addField(String fieldType, String fieldName) {
        System.out.println("add field fieldType: " + fieldType + ",fieldName: " + fieldName);
        //fieldList.add()
    }

    public void addInterceptor(String className) {
        System.out.println("add Interceptor name: " + className);
        try {
            Class clz = pluginsClassLoader.loadClass(className);
            try {
                Object target = clz.newInstance();
                if(target instanceof AroundInterceptor){
                    int index = InterceptorRegister.addInterceptor((AroundInterceptor)target);
                    TargetConstructor targetConstructor = (TargetConstructor) clz.getDeclaredAnnotation(TargetConstructor.class);
                    TargetMethod targetMethod = (TargetMethod) clz.getDeclaredAnnotation(TargetMethod.class);
                    if(targetConstructor != null){
                        processTargetConstructor(targetConstructor,index);
                    }
                    if(targetMethod != null){
                        processTargetMethods(targetConstructor,index);
                    }
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processTargetMethods(TargetConstructor targetConstructor, int index) {

    }

    private void processTargetConstructor(TargetConstructor targetConstructor, int index) {

    }

    public byte[] toByte() {
        return byteBuf;
    }
}
