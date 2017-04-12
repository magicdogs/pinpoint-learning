
# Pinpoint 学习笔记!


> Pinpoint github 官网对其描述
> Pinpoint is an open source APM (Application Performance Management) tool for large-scale distributed systems written in Java. (一款开源的 应用性能管理工具  为大规模的分布式系统定制 )

==========================================================================
#### 本文将从这 5 个方面理解 Pinpoint 的实现机制
 
 1. JVMTI 、 JVMTIAgent  与 JAVAAGENT 机制
 2. Pinpoint ClassLoader
 3. Pinpoint 's Plugins ServiceLoader
 4. CLASS 字节码操作工具
 5. TCP OR UDP

-----

**1.JVMTI 、 JVMTIAgent  与 JAVAAGENT 机制**

**JVMTI 接口**

		JVMTI 全称JVM Tool Interface，是jvm暴露出来的一些供用户扩展的接口集合，JVMTI是基于事件驱动的，JVM每执行到一定的逻辑就会调用一些事件的回调接口（如果有的话），这些接口可以供开发者去扩展自己的逻辑。

	比如说我们最常见的想在某个类的字节码文件读取之后类定义之前能修改相关的字节码，从而使创建的class对象是我们修改之后的字节码内容，那我们就可以实现一个回调函数赋给JvmtiEnv（JVMTI的运行时，通常一个JVMTIAgent对应一个jvmtiEnv，但是也可以对应多个）的回调方法集合里的ClassFileLoadHook，这样在接下来的类文件加载过程中都会调用到这个函数里来了，大致实现如下:

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

		说到javaagent必须要讲的是一个叫做instrument的JVMTIAgent（linux下对应的动态库是libinstrument.so），因为就是它来实现javaagent的功能的，另外instrument agent还有个别名叫JPLISAgent(Java Programming Language Instrumentation Services Agent)，从这名字里也完全体现了其最本质的功能：就是专门为java语言编写的插桩服务提供支持的。

instrument agent	

		instrument agent实现了Agent_OnLoad和Agent_OnAttach两方法，也就是说我们在用它的时候既支持启动的时候来加载agent，也支持在运行期来动态来加载这个agent，其中启动时加载agent还可以通过类似-javaagent:myagent.jar的方式来间接加载instrument agent，运行期动态加载agent依赖的是jvm的attach机制JVM Attach机制实现，通过发送load命令来加载agent。

		启动的时候加载instrument agent，具体过程都在InvocationAdapter.c的Agent_OnLoad方法里，简单描述下过程：
		

		 1. 创建并初始化JPLISAgent
		 
		 2. 监听VMInit事件，在vm初始化完成之后做下面的事情：	
				创建InstrumentationImpl对象
				监听ClassFileLoadHook事件
				调用InstrumentationImpl的loadClassAndCallPremain方法，在这个方法里会去调用javaagent里MANIFEST.MF里指定的Premain-Class类的premain方法
				
		3. 解析javaagent里MANIFEST.MF里的参数，并根据这些参数来设置JPLISAgent里的一些内容

--------

 **2. Pinpoint ClassLoader**

javaagent的其他小众功能
javaagent除了做字节码上面的修改之外，其实还有一些小功能，有时候还是挺有用的
获取所有已经被加载的类
Class[] getAllLoadedClasses();
获取所有已经被初始化过了的类
Class[] getInitiatedClasses(ClassLoader loader);
获取某个对象的大小
long getObjectSize(Object objectToSize);
将某个jar加入到bootstrapclasspath里优先其他jar被加载
void appendToBootstrapClassLoaderSearch(JarFile jarfile);
将某个jar加入到classpath里供appclassloard去加载
void appendToSystemClassLoaderSearch(JarFile jarfile);
设置某些native方法的前缀，主要在找native方法的时候做规则匹配
void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix);


t Markdown document in **StackEdit**[^stackedit]. Don't delete me, I'm very helpful! I can be recovered anyway in the **Utils** tab of the <i class="icon-cog"></i> **Settings** dialog.

----------


Documents
-------------

StackEdit stores your documents in your browser, which means all your documents are automatically saved locally and are accessible **offline!**

> **Note:**

> - StackEdit is accessible offline after the application has been loaded for the first time.
> - Your local documents are not shared between different browsers or computers.
> - Clearing your browser's data may **delete all your local documents!** Make sure your documents are synchronized with **Google Drive** or **Dropbox** (check out the [<i class="icon-refresh"></i> Synchronization](#synchronization) section).

#### <i class="icon-file"></i> Create a document

The document panel is accessible using the <i class="icon-folder-open"></i> button in the navigation bar. You can create a new document by clicking <i class="icon-file"></i> **New document** in the document panel.

#### <i class="icon-folder-open"></i> Switch to another document

All your local documents are listed in the document panel. You can switch from one to another by clicking a document in the list or you can toggle documents using <kbd>Ctrl+[</kbd> and <kbd>Ctrl+]</kbd>.

#### <i class="icon-pencil"></i> Rename a document

You can rename the current document by clicking the document title in the navigation bar.

#### <i class="icon-trash"></i> Delete a document

You can delete the current document by clicking <i class="icon-trash"></i> **Delete document** in the document panel.

#### <i class="icon-hdd"></i> Export a document

You can save the current document to a file by clicking <i class="icon-hdd"></i> **Export to disk** from the <i class="icon-provider-stackedit"></i> menu panel.

> **Tip:** Check out the [<i class="icon-upload"></i> Publish a document](#publish-a-document) section for a description of the different output formats.


----------


Synchronization
-------------------

StackEdit can be combined with <i class="icon-provider-gdrive"></i> **Google Drive** and <i class="icon-provider-dropbox"></i> **Dropbox** to have your documents saved in the *Cloud*. The synchronization mechanism takes care of uploading your modifications or downloading the latest version of your documents.

> **Note:**

> - Full access to **Google Drive** or **Dropbox** is required to be able to import any document in StackEdit. Permission restrictions can be configured in the settings.
> - Imported documents are downloaded in your browser and are not transmitted to a server.
> - If you experience problems saving your documents on Google Drive, check and optionally disable browser extensions, such as Disconnect.

#### <i class="icon-refresh"></i> Open a document

You can open a document from <i class="icon-provider-gdrive"></i> **Google Drive** or the <i class="icon-provider-dropbox"></i> **Dropbox** by opening the <i class="icon-refresh"></i> **Synchronize** sub-menu and by clicking **Open from...**. Once opened, any modification in your document will be automatically synchronized with the file in your **Google Drive** / **Dropbox** account.

#### <i class="icon-refresh"></i> Save a document

You can save any document by opening the <i class="icon-refresh"></i> **Synchronize** sub-menu and by clicking **Save on...**. Even if your document is already synchronized with **Google Drive** or **Dropbox**, you can export it to a another location. StackEdit can synchronize one document with multiple locations and accounts.

#### <i class="icon-refresh"></i> Synchronize a document

Once your document is linked to a <i class="icon-provider-gdrive"></i> **Google Drive** or a <i class="icon-provider-dropbox"></i> **Dropbox** file, StackEdit will periodically (every 3 minutes) synchronize it by downloading/uploading any modification. A merge will be performed if necessary and conflicts will be detected.

If you just have modified your document and you want to force the synchronization, click the <i class="icon-refresh"></i> button in the navigation bar.

> **Note:** The <i class="icon-refresh"></i> button is disabled when you have no document to synchronize.

#### <i class="icon-refresh"></i> Manage document synchronization

Since one document can be synchronized with multiple locations, you can list and manage synchronized locations by clicking <i class="icon-refresh"></i> **Manage synchronization** in the <i class="icon-refresh"></i> **Synchronize** sub-menu. This will let you remove synchronization locations that are associated to your document.

> **Note:** If you delete the file from **Google Drive** or from **Dropbox**, the document will no longer be synchronized with that location.

----------


Publication
-------------

Once you are happy with your document, you can publish it on different websites directly from StackEdit. As for now, StackEdit can publish on **Blogger**, **Dropbox**, **Gist**, **GitHub**, **Google Drive**, **Tumblr**, **WordPress** and on any SSH server.

#### <i class="icon-upload"></i> Publish a document

You can publish your document by opening the <i class="icon-upload"></i> **Publish** sub-menu and by choosing a website. In the dialog box, you can choose the publication format:

- Markdown, to publish the Markdown text on a website that can interpret it (**GitHub** for instance),
- HTML, to publish the document converted into HTML (on a blog for example),
- Template, to have a full control of the output.

> **Note:** The default template is a simple webpage wrapping your document in HTML format. You can customize it in the **Advanced** tab of the <i class="icon-cog"></i> **Settings** dialog.

#### <i class="icon-upload"></i> Update a publication

After publishing, StackEdit will keep your document linked to that publication which makes it easy for you to update it. Once you have modified your document and you want to update your publication, click on the <i class="icon-upload"></i> button in the navigation bar.

> **Note:** The <i class="icon-upload"></i> button is disabled when your document has not been published yet.

#### <i class="icon-upload"></i> Manage document publication

Since one document can be published on multiple locations, you can list and manage publish locations by clicking <i class="icon-upload"></i> **Manage publication** in the <i class="icon-provider-stackedit"></i> menu panel. This will let you remove publication locations that are associated to your document.

> **Note:** If the file has been removed from the website or the blog, the document will no longer be published on that location.

----------


Markdown Extra
--------------------

StackEdit supports **Markdown Extra**, which extends **Markdown** syntax with some nice features.

> **Tip:** You can disable any **Markdown Extra** feature in the **Extensions** tab of the <i class="icon-cog"></i> **Settings** dialog.

> **Note:** You can find more information about **Markdown** syntax [here][2] and **Markdown Extra** extension [here][3].


### Tables

**Markdown Extra** has a special syntax for tables:

Item     | Value
-------- | ---
Computer | $1600
Phone    | $12
Pipe     | $1

You can specify column alignment with one or two colons:

| Item     | Value | Qty   |
| :------- | ----: | :---: |
| Computer | $1600 |  5    |
| Phone    | $12   |  12   |
| Pipe     | $1    |  234  |


### Definition Lists

**Markdown Extra** has a special syntax for definition lists too:

Term 1
Term 2
:   Definition A
:   Definition B

Term 3

:   Definition C

:   Definition D

	> part of definition D


### Fenced code blocks

GitHub's fenced code blocks are also supported with **Highlight.js** syntax highlighting:

```
// Foo
var bar = 0;
```

> **Tip:** To use **Prettify** instead of **Highlight.js**, just configure the **Markdown Extra** extension in the <i class="icon-cog"></i> **Settings** dialog.

> **Note:** You can find more information:

> - about **Prettify** syntax highlighting [here][5],
> - about **Highlight.js** syntax highlighting [here][6].


### Footnotes

You can create footnotes like this[^footnote].

  [^footnote]: Here is the *text* of the **footnote**.


### SmartyPants

SmartyPants converts ASCII punctuation characters into "smart" typographic punctuation HTML entities. For example:

|                  | ASCII                        | HTML              |
 ----------------- | ---------------------------- | ------------------
| Single backticks | `'Isn't this fun?'`            | 'Isn't this fun?' |
| Quotes           | `"Isn't this fun?"`            | "Isn't this fun?" |
| Dashes           | `-- is en-dash, --- is em-dash` | -- is en-dash, --- is em-dash |


### Table of contents

You can insert a table of contents using the marker `[TOC]`:

[TOC]


### MathJax

You can render *LaTeX* mathematical expressions using **MathJax**, as on [math.stackexchange.com][1]:

The *Gamma function* satisfying $\Gamma(n) = (n-1)!\quad\forall n\in\mathbb N$ is via the Euler integral

$$
\Gamma(z) = \int_0^\infty t^{z-1}e^{-t}dt\,.
$$

> **Tip:** To make sure mathematical expressions are rendered properly on your website, include **MathJax** into your template:

```
<script type="text/javascript" src="https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS_HTML"></script>
```

> **Note:** You can find more information about **LaTeX** mathematical expressions [here][4].


### UML diagrams

You can also render sequence diagrams like this:

```sequence
Alice->Bob: Hello Bob, how are you?
Note right of Bob: Bob thinks
Bob-->Alice: I am good thanks!
```

And flow charts like this:

```flow
st=>start: Start
e=>end
op=>operation: My Operation
cond=>condition: Yes or No?

st->op->cond
cond(yes)->e
cond(no)->op
```

> **Note:** You can find more information:

> - about **Sequence diagrams** syntax [here][7],
> - about **Flow charts** syntax [here][8].

### Support StackEdit

[![](https://cdn.monetizejs.com/resources/button-32.png)](https://monetizejs.com/authorize?client_id=ESTHdCYOi18iLhhO&summary=true)

  [^stackedit]: [StackEdit](https://stackedit.io/) is a full-featured, open-source Markdown editor based on PageDown, the Markdown library used by Stack Overflow and the other Stack Exchange sites.


  [1]: http://math.stackexchange.com/
  [2]: http://daringfireball.net/projects/markdown/syntax "Markdown"
  [3]: https://github.com/jmcmanus/pagedown-extra "Pagedown Extra"
  [4]: http://meta.math.stackexchange.com/questions/5020/mathjax-basic-tutorial-and-quick-reference
  [5]: https://code.google.com/p/google-code-prettify/
  [6]: http://highlightjs.org/
  [7]: http://bramp.github.io/js-sequence-diagrams/
  [8]: http://adrai.github.io/flowchart.js/
