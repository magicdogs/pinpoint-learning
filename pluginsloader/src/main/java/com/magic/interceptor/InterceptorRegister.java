package com.magic.interceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by magicdog on 2017/4/13.
 */
public class InterceptorRegister {

    private static Map<Integer,AroundInterceptor> aroundInterceptorMap = new ConcurrentHashMap<>();
    private static int index = 300 ;

    public  static int addInterceptor(AroundInterceptor interceptor){
        synchronized(InterceptorRegister.class){
            aroundInterceptorMap.put(index++,interceptor);
        }
        return index;
    }


    public  static AroundInterceptor getInterceptor(int index){
       if(aroundInterceptorMap.containsKey(index)){
           return aroundInterceptorMap.get(index);
       }
        return null;
    }

}
