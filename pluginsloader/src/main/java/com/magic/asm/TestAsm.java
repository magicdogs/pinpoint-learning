package com.magic.asm;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

/**
 * Created by magicdog on 2017/4/14.
 */
public class TestAsm implements Opcodes{
    public static void main(String[] args) {
        ClassNode classNode = new ClassNode(V1_5);
    }
}
