
# Pinpoint 学习笔记!


> Pinpoint github 官网对其描述
> Pinpoint is an open source APM (Application Performance Management) tool for large-scale distributed systems written in Java. (一款开源的 应用性能管理工具  为大规模的分布式系统定制 )

==========================================================================
#### 本文将从这 4 个方面理解 Pinpoint 的实现机制
 
 1. JVMTI 、 JVMTIAgent  与 JAVAAGENT 机制
 2. Pinpoint ClassLoader
 3. CLASS 字节码操作工具
 4. TCP OR UDP

-----

**1.JVMTI 、 JVMTIAgent  与 JAVAAGENT 机制**

**JVMTI 接口**

    JVMTI 全称JVM Tool Interface，是jvm暴露出来的一些供用户扩展的接口集合，JVMTI是基于事件驱动的
    JVM每执行到一定的逻辑就会调用一些事件的回调接口（如果有的话），这些接口可以供开发者去扩展自己的逻辑。


比如说我们最常见的想在某个类的字节码文件读取之后类定义之前能修改相关的字节码，
从而使创建的class对象是我们修改之后的字节码内容,那我们就可以实现一个回调函数赋给JvmtiEnv（JVMTI的运行时，通常一个JVMTIAgent对应一个jvmtiEnv，
    但是也可以对应多个）的回调方法集合里的ClassFileLoadHook，
    这样在接下来的类文件加载过程中都会调用到这个函数里来了，大致实现如下:
	

	jvmtiEventCallbacks callbacks;
    jvmtiEnv *  jvmtienv = jvmti(agent);
    jvmtiError  jvmtierror;
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.ClassFileLoadHook = &eventHandlerClassFileLoadHook;
    jvmtierror = (*jvmtienv)->SetEventCallbacks( jvmtienv,
                                                 &callbacks,
                                                 sizeof(callbacks));

-----

**JVMTIAgent 库**

JVMTIAgent其实就是一个动态库，利用JVMTI暴露出来的一些接口来干一些我们想做但是正常情况下又做不到的事情，不过为了和普通的动态库进行区分，它一般会实现如下的一个或者多个函数：

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved);

JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM* vm, char* options, void* reserved);

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm); 

Agent_OnLoad函数，如果agent是在启动的时候加载的，也就是在vm参数里通过
**-agentlib** 来指定，那在启动过程中就会去执行这个agent里的Agent_OnLoad函数。
**Pinpoint 正是利用这种方式进行加载启动的**
**-javaagent:$AGENTPATH/pinpoint-bootstrap-${AGENT_VERSION}.jar**

Agent_OnAttach函数，如果agent不是在启动的时候加载的，是我们先attach到目标进程上，然后给对应的目标进程发送load命令来加载agent，在加载过程中就会调用Agent_OnAttach函数。
**VirtualMachine vm = VirtualMachine.attach(pid);**
**vm.loadAgent(agentPath, agentArgs);**

Agent_OnUnload函数，在agent做卸载的时候调用，不过貌似基本上很少实现它。

-----

**JAVAAGENT**

javaagent

	说到 javaagent 必须要讲的是一个叫做instrument 的 JVMTIAgent(linux下对应的动态库是libinstrument.so),
	因为就是它来实现 javaagent 的功能的，另外 instrument agent 还有个别名
	叫 JPLISAgent(Java Programming Language Instrumentation Services Agent)，
	从这名字里也完全体现了其最本质的功能：就是专门为java语言编写的插桩服务提供支持的。

instrument agent	

	instrument agent实现了Agent_OnLoad和Agent_OnAttach两方法，也就是说我们在用它的时候既支持启动的时候
	来加载agent，也支持在运行期来动态来加载这个agent，其中启动时加载agent还可以通过类似
	-javaagent:myagent.jar的方式来间接加载instrument agent，运行期动态加载agent依赖的是jvm的attach机制
	JVM Attach机制实现，通过发送load命令来加载agent。

		启动的时候加载instrument agent，具体过程都在InvocationAdapter.c的Agent_OnLoad方法里，简单描述下过程：
		

		 1. 创建并初始化JPLISAgent
		 
		 2. 监听VMInit事件，在vm初始化完成之后做下面的事情：	
			    创建InstrumentationImpl对象
			    监听ClassFileLoadHook事件
			    调用InstrumentationImpl的loadClassAndCallPremain方法，
                        在这个方法里会去调用javaagent里MANIFEST.MF里指定的Premain-Class类的premain方法
				
		3. 解析javaagent里MANIFEST.MF里的参数，并根据这些参数来设置JPLISAgent里的一些内容


    javaagent的小功能，javaagent除了做字节码上面的修改之外，其实还有一些小功能。
    
        Class[] getAllLoadedClasses(); --获取所有已经被加载的类
    
        Class[] getInitiatedClasses(ClassLoader loader); --获取所有已经被初始化过了的类
    
        long getObjectSize(Object objectToSize); --获取某个对象的大小
    
        void appendToBootstrapClassLoaderSearch(JarFile jarfile); --将某个jar加入到bootstrapclasspath里优先其他jar被加载
    
        void appendToSystemClassLoaderSearch(JarFile jarfile); --将某个jar加入到classpath里供appclassloard去加载
    
        void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix); --设置某些native方法的前缀，主要在找native方法的时候做规则匹配

--------

 **2. Pinpoint ClassLoader**
 
 JVM ClassLoader 机制，双亲委托模型
 
 AppClassLoader(3) -> ExtClassLoader(2) -> BootstrapClassLoader(1)
 
 class 的查找顺序, 从右往左，优先级依次降低
 
 Thread.currentThread (setClassLoader,getClassLoader) 作用 ?
 
 问题:
        
        classT,ClassLoaderA(classT的实例对象A),ClassLoaderB(classT的实例对象B) 
        
        都加载classT ，
        
        问 A.getClass().cast(B) Success or ClassCastException ,Why ?
        

Pinpoint ClassLoader 主要是使用了 4 大 ClassLoader


通过javaagent: 注入启动的jar 到AppClassLoader

通过instrucment 注入 boot目录 核心jar 到BootStrapClassLoader

自定义的 LibraryClassLoader 加载第三方jar 与自身需要用到的库

pluginClassLoader 加载所有外置的plugins 通过SPI实例化所有插件对象实例

--------

 **3. CLASS 字节码操作工具**
 
 JAVA 常用的字节码操作工具
  Javassist , Asm , Apache bcel

pinpoint 可以选择 javassist 或 Asm 框架作为其 字节码增强工具 

字节码汇编
(性能由左往右依次降低,可读性由左往右依次升高)
01 二进制 -> 汇编语言 -> 高级语言

汇编语言的操作一般格式是 : 操作码 + 操作数

在java中，每一个方法的调用都带有方法的局部变量参数表，堆，栈 等信息。

一个典型的java字节码汇编例子 


    ; --- Copyright Jonathan Meyer 1996. All rights reserved. -----------------
    ; File:      jasmin/examples/HelloWorld.j
    ; Author:    Jonathan Meyer, 10 July 1996
    ; Purpose:   Prints out "2" and "20"
    ; -------------------------------------------------------------------------


    .class public NoJad.j
    .super java/lang/Object

    ;
    ; standard initializer
    .method public <init>()V
        aload_0
        invokenonvirtual java/lang/Object/<init>()V
        return
    .end method

    .method public static main([Ljava/lang/String;)V
    .limit stack 3
    .limit locals 1

        getstatic      java/lang/System/out Ljava/io/PrintStream;
        dup
        bipush 2
        invokevirtual  java/io/PrintStream/println(I)V
  
        bipush 20
        invokevirtual  java/io/PrintStream/println(I)V
   
        return
    .end method


   从字节码层面解释 为什么 Integer i1 = new Integer(number); 不是原子性操作
   
    ANEWARRAY java/lang/Object
    DUP
    ICONST_0
    ALOAD 1
    AASTORE
    DUP
    ICONST_1
    ILOAD 2
    NEW java/lang/Integer
    DUP_X1
    SWAP
    INVOKESPECIAL java/lang/Integer.<init> (I)V
    AASTORE
    DUP
    ICONST_2
    ILOAD 3
    NEW java/lang/Integer
    DUP_X1
    SWAP
    INVOKESPECIAL java/lang/Integer.<init> (I)V
    AASTORE
    
   
--------

 **4. TCP OR UDP**
 
 Pinpoint 应用状态数据采用 tcp 协议与 collector 通讯, 程序plugin 探针采集的数据通过udp协议发送到 collector 端.
 
 Pinpoint 网络通讯框架采用的是netty , 自定义编码与解码器
 
 TCP 面向连接协议 3次握手，4次挥手
 
 UDP 无连接 无状态

----------

