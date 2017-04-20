package com.magic.instrumentation;

import com.magic.asm.ExampleClassVisitor;
import com.magic.bootstrap.ExampleLoader;
import com.magic.interceptor.AroundInterceptor;
import com.magic.interceptor.InterceptorRegister;
import com.magic.util.TargetConstructor;
import com.magic.util.TargetMethod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

/**
 * Created by magicdog on 2017/4/13.
 */
public class AsmInstrumentClass implements InstrumentionClass{

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
                        processTargetMethods(targetMethod,index);
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

    private void processTargetMethods(TargetMethod targetMethod, int index) {
        System.out.println(targetMethod.name() + "," + index);
        ClassReader cr = new ClassReader(byteBuf);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ExampleClassVisitor(Opcodes.ASM5,targetMethod.name(),index,cw);
        CheckClassAdapter checkClassAdapter = new CheckClassAdapter(cv);
        cr.accept(checkClassAdapter,EXPAND_FRAMES);
        byteBuf = cw.toByteArray();
    }

    private void processTargetConstructor(TargetConstructor targetConstructor, int index) {
        /*System.out.println(targetConstructor.value()[0] + "," + index);
        ClassReader cr = new ClassReader(byteBuf);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ExampleClassVisitor(Opcodes.ASM5,null,index,cw);
        CheckClassAdapter checkClassAdapter = new CheckClassAdapter(cv);
        cr.accept(checkClassAdapter,EXPAND_FRAMES);
        byteBuf = cw.toByteArray();*/
    }

    public byte[] toByte() {
        /*try {
            OutputStream out = new FileOutputStream("C:\\Users\\magicdog\\Desktop\\pinpoint-learning\\pluginsloader\\src\\main\\Example1.class");
            out.write(byteBuf);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        System.out.println("tobyte =======================================");
        return byteBuf;
    }
}
