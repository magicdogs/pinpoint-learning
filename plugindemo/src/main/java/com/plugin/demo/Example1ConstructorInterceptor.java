package com.plugin.demo;

import com.magic.interceptor.AroundInterceptor;
import com.magic.util.TargetConstructor;

/**
 * Created by magicdog on 2017/4/13.
 */

@TargetConstructor({"java.lang.String","int"})
public class Example1ConstructorInterceptor implements AroundInterceptor{

    public void before(Object target, Object[] args) {
        System.out.println("before ");
    }

    public void after(Object target, Object[] args, Object result) {
        System.out.println("after : " );
    }
}
