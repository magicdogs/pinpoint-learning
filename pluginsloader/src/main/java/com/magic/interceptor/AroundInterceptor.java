package com.magic.interceptor;

/**
 * Created by magicdog on 2017/4/13.
 */
public interface AroundInterceptor {

    void before(Object target,Object[] args);

    void after(Object target,Object[] args,Object result);

}
