/*
package com.magic.example;

import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

*/
/**
 * Created by magicdog on 2017/4/14.
 *//*

public class ClassVisitDemo {
    public static void main(String[] args) throws Exception{

        System.out.println(Type.getType(Object[][].class).getInternalName());

        InputStream in = ClassLoader.getSystemResourceAsStream("com/magic/example/Example1.class");
        ClassReader cr = new ClassReader(in);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ExampleClassVisitor(Opcodes.ASM5,cw);
        CheckClassAdapter checkClassAdapter = new CheckClassAdapter(cv);
        cr.accept(checkClassAdapter,EXPAND_FRAMES);

        byte[] bt = cw.toByteArray();
        String outPut = "target/classes/com/magic/example/";
        File f = new File(outPut);
        f.mkdirs();
        OutputStream fos = new FileOutputStream(new File(outPut + "Example12.class"));
        fos.write(bt);
        fos.flush();
        fos.close();
    }
}
*/
