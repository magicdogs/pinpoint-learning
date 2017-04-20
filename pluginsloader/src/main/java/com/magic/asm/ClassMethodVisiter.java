package com.magic.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by magicdog on 2017/4/17.
 */
public class ClassMethodVisiter extends LocalVariablesSorter implements Opcodes{

    private int argumentIndex ,
                resultIndex,
                interceptorIndex;

    private int interceptorId;

    private String INTERCEPTOR_AGENTNAME = "com/magic/interceptor/InterceptorRegister"; //getInterceptor
    private String INTERCEPTOR_BACKNAME = "(I)Lcom/magic/interceptor/AroundInterceptor;";
    private String INTERCEPTOR_TYPENAME = "Lcom/magic/interceptor/AroundInterceptor;";
    private String INTERCEPTOR_TYPEOUTLINENAME = "com/magic/interceptor/AroundInterceptor";
    private String INTERCEPTOR_GETNAME = "getInterceptor";
    //方法的访问标志
    private int access;
    //Class 的名称
    private String owner;
    //方法名称
    private String name;
    //方法描述信息
    private String desc;

    //作用域label
    private Label start;
    private Label end;

    // below label variables are for adding try/catch blocks in instrumented
    // code.
    private Label lTryBlockStart = new Label();
    private Label lTryBlockEnd  = new Label();
    private Label lCatchBlockStart  = new Label();
    private Label lCatchBlockEnd  = new Label();

    //基本类型与对象类型的映射关系
    private static Map<String,String> jvmSignName = new HashMap<String,String>();
    static {
        jvmSignName.put("int","java/lang/Integer");
        jvmSignName.put("boolean","java/lang/Boolean");
        jvmSignName.put("char","java/lang/Character");
        jvmSignName.put("byte","java/lang/Byte");
        jvmSignName.put("short","java/lang/Short");
        jvmSignName.put("float","java/lang/Float");
        jvmSignName.put("long","java/lang/Long");
        jvmSignName.put("double","java/lang/Double");
    }


    public ClassMethodVisiter(int interceptorIndex,int access, String owner, String name, String desc, MethodVisitor visitor) {
        super(ASM5,access,desc,visitor);
        this.access = access;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.interceptorId = interceptorIndex;
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        if(index == 0 ){
            this.start = start;
            this.end = end;
        }
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        //设置局部变量表
        super.visitLocalVariable("_args",Type.getType(Object[].class).toString(),null,start,end,argumentIndex);
        super.visitLocalVariable("_result",Type.getType(Object.class).toString(),null,start,end,resultIndex);
        super.visitLocalVariable("_interceptor",INTERCEPTOR_TYPENAME,null,start,end,interceptorIndex);

        super.visitLabel(lTryBlockEnd);
        // when here, no exception was thrown, so skip exception handler
        super.visitJumpInsn(GOTO, lCatchBlockEnd);
        // exception handler starts here, with RuntimeException stored
        // on stack
        super.visitLabel(lCatchBlockStart);

        addAfterInterceptor();

        super.visitInsn(ATHROW);
        // exception handler ends here:
        super.visitLabel(lCatchBlockEnd);
        super.visitMaxs(maxStack + 10, maxLocals + 10);

    }

    @Override
    public void visitCode() {
        super.visitCode();
        //新增方法本地变量
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        argumentIndex = newLocal(Type.getType(Object[].class));
        resultIndex = newLocal(Type.getType(Object.class));
        interceptorIndex = newLocal(Type.getType(INTERCEPTOR_TYPENAME));

        super.visitIntInsn(BIPUSH,argumentTypes.length);
        super.visitTypeInsn(ANEWARRAY,"java/lang/Object");

        //封装所有的参数 构造Object[] 数组
        for (int i = 0;i < argumentTypes.length; i++){
            String clzName = argumentTypes[i].getClassName();
            if(jvmSignName.containsKey(clzName)){
                super.visitInsn(DUP);
                super.visitIntInsn(BIPUSH,i);
                super.visitVarInsn(ILOAD, i + 1);
                super.visitTypeInsn(NEW, jvmSignName.get(clzName));
                super.visitInsn(DUP_X1);
                super.visitInsn(SWAP);
                super.visitMethodInsn(INVOKESPECIAL, jvmSignName.get(clzName), "<init>", "(I)V", false);
                super.visitInsn(AASTORE);
            }else{
                super.visitInsn(DUP);
                super.visitIntInsn(BIPUSH,i);
                super.visitVarInsn(ALOAD,i + 1);
                super.visitInsn(AASTORE);
            }
        }

        super.visitVarInsn(ASTORE,argumentIndex);
        //println 调用
        super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        super.visitVarInsn(ALOAD, argumentIndex);
        super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);

        //设置空值
        super.visitInsn(ACONST_NULL);
        super.visitVarInsn(ASTORE,resultIndex);

        super.visitIntInsn(SIPUSH, interceptorId);
        super.visitMethodInsn(INVOKESTATIC, INTERCEPTOR_AGENTNAME, INTERCEPTOR_GETNAME, INTERCEPTOR_BACKNAME, false);
        super.visitVarInsn(ASTORE,interceptorIndex);

        super.visitVarInsn(ALOAD, interceptorIndex);
        super.visitTypeInsn(CHECKCAST, INTERCEPTOR_TYPEOUTLINENAME);
        super.visitVarInsn(ALOAD, 0);
        super.visitVarInsn(ALOAD, argumentIndex);
        super.visitMethodInsn(INVOKEINTERFACE, INTERCEPTOR_TYPEOUTLINENAME, "before", "(Ljava/lang/Object;[Ljava/lang/Object;)V", true);

        // set up try-catch block for RuntimeException
        visitTryCatchBlock(lTryBlockStart, lTryBlockEnd,
                lCatchBlockStart, "java/lang/Throwable");

        // started the try block
        visitLabel(lTryBlockStart);

    }

    @Override
    public void visitInsn(int opcode) {
        if((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW){
           if(opcode == ARETURN){
               addAfterInterceptor();
           }
        }
        super.visitInsn(opcode);
    }

    private void addAfterInterceptor() {

        super.visitVarInsn(ASTORE, resultIndex);
        super.visitVarInsn(ALOAD, interceptorIndex);
        super.visitTypeInsn(CHECKCAST, INTERCEPTOR_TYPEOUTLINENAME);
        super.visitVarInsn(ALOAD, 0);
        super.visitVarInsn(ALOAD, argumentIndex);
        super.visitVarInsn(ALOAD, resultIndex);
        super.visitMethodInsn(INVOKEINTERFACE, INTERCEPTOR_TYPEOUTLINENAME, "after", "(Ljava/lang/Object;[Ljava/lang/Object;Ljava/lang/Object;)V", true);
        super.visitVarInsn(ALOAD, resultIndex);
    }
}