package com.plugin.demo;

import com.magic.interceptor.AroundInterceptor;
import com.magic.util.TargetMethod;

/**
 * Created by magicdog on 2017/4/13.
 */
@TargetMethod(name = "test")
public class Example1MethodInterceptor implements AroundInterceptor{

    public void before(Object target, Object[] args) {

    }

    public void after(Object target, Object[] args, Object result) {

    }
}
