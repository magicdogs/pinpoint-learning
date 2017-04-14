package com.magic.transfrom;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by magicdog on 2017/4/13.
 */
public class TransfromTemplateImpl implements TransfromOpearation{

    private Map<String,List<ClassTransfromer>> fileTransformerMap = new ConcurrentHashMap<String,List<ClassTransfromer>>();

    public void addClassTransfrom(String className, ClassTransfromer classFileTransformer) {
        if(fileTransformerMap.containsKey(className)){
            fileTransformerMap.get(className).add(classFileTransformer);
            return ;
        }
        List<ClassTransfromer> nodeItems = new LinkedList<ClassTransfromer>();
        nodeItems.add(classFileTransformer);
        fileTransformerMap.put(className,nodeItems);
    }

    public List<ClassTransfromer> getClassFileTransformerByName(String className) {
        if(fileTransformerMap.containsKey(className)){
            return fileTransformerMap.get(className);
        }
        return new LinkedList<ClassTransfromer>();
    }

    public boolean hasTransformer(String className) {
        if(fileTransformerMap.containsKey(className)){
            return true;
        }
        return false;
    }
}
