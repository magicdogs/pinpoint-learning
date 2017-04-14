package com.magic.transfrom;

import java.util.List;

/**
 * Created by magicdog on 2017/4/13.
 */
public interface TransfromOpearation extends TransfromTemplate{
    List<ClassTransfromer> getClassFileTransformerByName(String className);
    boolean hasTransformer(String className);
}
