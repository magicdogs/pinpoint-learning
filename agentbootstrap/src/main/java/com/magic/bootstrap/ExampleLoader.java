package com.magic.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by magicdog on 2017/4/12.
 */
public class ExampleLoader extends URLClassLoader{

    public ExampleLoader(URL[] urls) {
        super(urls);
    }

    public ExampleLoader(URL[] urls,ClassLoader parent){
        super(urls, parent);
    }
}
