package com.magic.example;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by magicdog on 2017/4/14.
 */
public class TestAsm implements Opcodes {

    private static Map<String,String> jvmShortName = new HashMap<String,String>();

    static {
        jvmShortName.put("int","I");
        jvmShortName.put("boolean","Z");
        jvmShortName.put("char","C");
        jvmShortName.put("byte","B");
        jvmShortName.put("short","S");
        jvmShortName.put("float","F");
        jvmShortName.put("long","J");
    }

    public static void main(String[] args) throws Exception{

        //System.out.println(Class.forName("int[]"));
        //printTypeMethod();
        //System.exit(0);
        ClassNode cn = new ClassNode();
        InputStream in = ClassLoader.getSystemResourceAsStream("com/magic/example/Example1.class");
        ClassReader cr = new ClassReader(in);
        cr.accept(cn,0);

        InterceptorConstructorMethod(cn,new String[]{"java.lang.String","int"});
        //InterceptorMethod(cn);

        /*MethodNode mn = createTestMethod();
        cn.methods.add(mn);*/

        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        byte[] bt = cw.toByteArray();
        String outPut = "target/classes/com/magic/example/";
        File f = new File(outPut);
        f.mkdirs();
        OutputStream fos = new FileOutputStream(new File(outPut + "Example1x.class"));
        fos.write(bt);
        fos.flush();
        fos.close();
    }

    private static void InterceptorConstructorMethod(ClassNode cn,String[] args) throws Exception{
        String desc = "";
        if(args!=null && args.length > 0){
            StringBuilder builder = new StringBuilder("(");
            for (int i = 0;i<args.length;i++){
                if(jvmShortName.containsKey(args[i])){
                    builder.append(jvmShortName.get(args[i]));
                }else{
                    Class clz = Class.forName(args[i]);
                    builder.append(Type.getType(clz).toString());
                }
            }
            builder.append(")V");
            desc = builder.toString();
        }
        Iterator<MethodNode> methods = cn.methods.iterator();
        while(methods.hasNext()){
            MethodNode method = methods.next();
            if("<init>".equals(method.name) && desc.startsWith(method.desc)){
                System.out.println("methodName: " + method.name + " , desc: "+method.desc);
                InsnList instructions = method.instructions;
                if(instructions.size() == 0){
                    continue;
                }
                Iterator<AbstractInsnNode> items = instructions.iterator();
                while(items.hasNext()){
                    AbstractInsnNode next = items.next();
                    int op = next.getOpcode();
                    if(op >= IRETURN && op <= RETURN){
                        InsnList after = new InsnList();
                        instructions.insert(next.getPrevious(),after);
                    }
                }
                InsnList before = new InsnList();

                instructions.insert(before);
                method.maxStack += 3;
            }
        }
    }

    private static void InterceptorMethod(ClassNode cn) {
        Iterator<MethodNode> methods = cn.methods.iterator();
        while(methods.hasNext()){
            MethodNode method = methods.next();
            if(!"<init>".equals(method.name)){
                System.out.println("methodName: " + method.name + " , desc: "+method.desc);
            }
        }
    }

    private static void printTypeMethod() {
        System.out.println(Type.getType(Integer.class));
        System.out.println(Type.getType(String.class).getInternalName());
        System.out.println(Type.getType(String.class).getDescriptor());
        System.out.println(Type.getType(String.class));
    }

    private static MethodNode createTestMethod() {
        MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC,"test2","(Ljava/lang/Integer;)Ljava/lang/Integer;",null,null);
        InsnList insnList = mn.instructions;
        insnList.add(new VarInsnNode(ILOAD,1));
        insnList.add(new InsnNode(ARETURN));
        mn.maxLocals = 2;
        mn.maxStack = 2;
        return mn;
    }
}
