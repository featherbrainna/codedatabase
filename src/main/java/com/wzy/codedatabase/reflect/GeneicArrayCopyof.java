package com.wzy.codedatabase.reflect;

import java.lang.reflect.Array;

import static java.lang.Math.min;

/**
 * 利用反射机制实现“泛型”数组的拷贝，并没有使用泛型语法，但原理与泛型语法相同
 * @author 王忠义
 * @version 1.0
 * @date: 2023/4/7 10:40
 */
public class GeneicArrayCopyof {
    /**
     * 复制一个与a数组相同的类型大小为newLength的数组
     * @param a 原始数组的变量
     * @param newLength 新数组长度
     * @return
     */
    public static Object goodCopyOf(Object a,int newLength){
        Class cl = a.getClass();
        //传入变量不是数组，拒绝复制返回null，有安全风险
        if (!cl.isArray()) return null;
        Class componentType = cl.getComponentType();
        int length = Array.getLength(a);
        Object newArray = Array.newInstance(componentType, newLength);
        System.arraycopy(a,0,newArray,0,min(length,newLength));
        return newArray;
    }
}
