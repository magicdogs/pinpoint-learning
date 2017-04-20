package com.magic.asm;

import com.magic.util.TargetConstructor;
import com.magic.util.TargetMethod;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;

/**
 * Created by magicdog on 2017/4/14.
 */
public class ExampleClassVisitor extends ClassVisitor {

    private String owner;
    private String methodName;
    private int index;

    public ExampleClassVisitor(int api, String methodName, int index, ClassVisitor cv) {
        super(api,cv);
        this.methodName = methodName == null ? "<init>" : methodName;
        this.index = index;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        System.out.println("name: " + name + ", desc: " + desc + ", val: " + value);
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println("name:　"+ name + ", superName: "+ superName + ",interfaceSize: " + interfaces.length);
        //String[] faces = new String[interfaces.length + 1];
        //faces[faces.length - 1] = "java/lang/Runnable";
        this.owner = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("name: "+ name+",desc : "+desc);
        MethodVisitor visitor = super.visitMethod(access,name,desc,signature,exceptions);
        if(methodName.equals(name) && access == Opcodes.ACC_PUBLIC && visitor != null){
            ClassMethodVisiter at = new ClassMethodVisiter(index,access,owner,name,desc,visitor);
            return at;
        }
        return visitor;
    }

}
