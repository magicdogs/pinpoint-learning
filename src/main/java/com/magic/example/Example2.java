package com.magic.example;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * Created by magicdog on 2017/4/12.
 */
public class Example2 {
    public static void main(String[] args) throws AgentLoadException, AgentInitializationException {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name);
        String pid = name.split("@")[0];
        System.out.println("Pid is:" + pid);
        try {
            VirtualMachine vm  = VirtualMachine.attach(pid);
            File agentFile = new File("agentbootstrap-1.0-SNAPSHOT.jar");
            vm.loadAgent(agentFile.getAbsolutePath(),pid);
        } catch (AttachNotSupportedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
