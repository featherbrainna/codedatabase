package com.wzy.codedatabase.geneic;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Scanner;

/**
 * 反射获取泛型类信息
 * @author 王忠义
 * @version 1.0
 * @date: 2023/3/31 16:16
 */
public class GenericReflectionTest {
    public static void main(String[] args){
        //读取类名
        String name;
        if (args.length>0) name=args[0];
        else {
            try (Scanner in = new Scanner(System.in)){
                System.out.println("Enter class name (e.g.,java.util.Collections):");
                name = in.next();
            }
        }

        try {
            //输出泛型信息
            Class<?> cl = Class.forName(name);
            //使用反射输出泛型类的类
            printClass(cl);
            //使用反射输出泛型类的方法(输入反射方法对象cl.getDeclaredMethods())
            for (Method m : cl.getDeclaredMethods()) {
                printMethod(m);
            }
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    /**
     * 通过反射得到泛型类的信息（{@code Class<?> cl}|类名、类型变量、父类、接口）
     * @param cl 要打印的类对象
     */
    private static void printClass(Class<?> cl) {
        System.out.print(cl);
        //1.返回泛型类的类型变量cl.getTypeParameters()（泛型可能多个故printTypes）
        printTypes(cl.getTypeParameters(),"<",",",">",true);
        //2.返回泛型的父类
        Type sc = cl.getGenericSuperclass();
        if(sc!=null){
            System.out.print(" extends ");
            //2.返回泛型的父类（父类只有一个故printType）
            printType(sc,false);
        }
        //3.返回泛型实现的接口（接口多个故printTypes）
        printTypes(cl.getGenericInterfaces()," implements ",",","",false);
        System.out.println();
    }

    /**
     * 通过反射得到泛型方法的信息（Method m|修饰符、类型变量、返回类型、方法名、参数类型）
     * @param m 要打印的方法对象
     */
    private static void printMethod(Method m) {
        String name = m.getName();
        //1.返回方法的修饰符
        System.out.print(Modifier.toString(m.getModifiers()));
        System.out.print(" ");
        //2.返回泛型的类型变量m.getTypeParameters()
        printTypes(m.getTypeParameters(),"<",",",">",true);

        //3.返回泛型返回类型
        printType(m.getGenericReturnType(),false);
        System.out.print(" ");
        //4.返回方法名
        System.out.print(name);
        System.out.print("(");
        //返回方法参数类型
        printTypes(m.getGenericParameterTypes(),"",",","",false);
        System.out.println(")");
    }

    /**
     * 打印所有类型信息
     * @param types 类型对象
     * @param pre
     * @param sep
     * @param suf
     * @param isDefinition
     */
    private static void printTypes(Type[] types, String pre, String sep, String suf, boolean isDefinition) {
        if (pre.equals(" extends ") && Arrays.equals(types,new Type[]{Object.class})) return;
        if (types.length>0) System.out.print(pre);
        for (int i = 0; i < types.length; i++) {
            if (i>0) System.out.print(sep);
            //打印单个类型
            printType(types[i], isDefinition);
        }
        if (types.length>0) System.out.print(suf);
    }

    /**
     * 打印单个类型信息！！！核心底层关于泛型的类型分析
     * @param type
     * @param isDefinition
     */
    private static void printType(Type type, boolean isDefinition) {
        //1.如果类型是一个类Class
        if (type instanceof Class){
            //将接收的编译类型强转为真实类型，调用真实类型的方法
            Class<?> t = (Class<?>) type;
            System.out.print(t.getName());
        }else if(type instanceof TypeVariable){//2.如果接收的是一个类型变量
            TypeVariable<?> t = (TypeVariable<?>) type;
            System.out.print(t.getName());
            if (isDefinition){
                printTypes(t.getBounds()," extends "," & ","",false);
            }
        }else if (type instanceof WildcardType){//3.如果接收的是一个通配符
            WildcardType t = (WildcardType) type;
            System.out.print("?");
            printTypes(t.getUpperBounds()," extends "," & ","",false);
            printTypes(t.getLowerBounds()," super "," & ","",false);
        }else if (type instanceof ParameterizedType){//4.如果接收的是一个泛型类或者泛型接口
            ParameterizedType t = (ParameterizedType) type;
            Type owner = t.getOwnerType();
            if (owner!=null){
                printType(owner,false);
                System.out.print(".");
            }
            printType(t.getRawType(),false);
            printTypes(t.getActualTypeArguments(),"<",",",">",false);
        }else if (type instanceof GenericArrayType){//5.如果接收的是一个泛型数组
            GenericArrayType t = (GenericArrayType) type;
            System.out.print("");
            printType(t.getGenericComponentType(),isDefinition);
            System.out.print("[]");
        }
    }
}
