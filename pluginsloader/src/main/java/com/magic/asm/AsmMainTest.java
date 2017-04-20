package com.magic.asm;

import com.magic.util.TargetMethod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.*;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;

/**
 * Created by magicdog on 2017/4/18.
 */
public class AsmMainTest {

    public static void main(String[] args) throws Exception{
        byte[] byteBuf = getTemplateByteCode();
        ClassReader cr = new ClassReader(byteBuf);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ExampleClassVisitor(Opcodes.ASM5,null,301,cw);
        CheckClassAdapter checkClassAdapter = new CheckClassAdapter(cv);
        cr.accept(checkClassAdapter,EXPAND_FRAMES);

        writeFile(cw.toByteArray());
    }

    private static void writeFile(byte[] bytes) throws Exception{
        File fout = new File("C:\\Users\\magicdog\\Desktop\\pinpoint-learning\\pluginsloader\\src\\main\\java\\com\\magic\\asm\\ccc.class");
        OutputStream out = new FileOutputStream(fout);
        out.write(bytes);
        out.flush();
        out.close();
    }

    public static byte[] getTemplateByteCode() throws Exception{
        File fso = new File("C:\\Users\\magicdog\\Desktop\\pinpoint-learning\\pluginsloader\\src\\main\\java\\com\\magic\\asm\\template.class");
        InputStream in = new FileInputStream(fso);
        byte[] buf = new byte[in.available()];
        in.read(buf);
        return buf;
    }
}
