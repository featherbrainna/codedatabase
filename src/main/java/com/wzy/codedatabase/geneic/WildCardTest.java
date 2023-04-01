package com.wzy.codedatabase.geneic;

import java.math.BigDecimal;

/**
 * 通配符使用
 * @author 王忠义
 * @version 1.0
 * @date: 2023/3/31 14:08
 *
 */
public class WildCardTest {
    public static void main(String[] args) {
        Manager ceo = new Manager("wzy", new BigDecimal(66666));
        Manager cfo = new Manager("wzy6", new BigDecimal(66666));
        Pair<Manager> buddies = new Pair<>(ceo, cfo);
        printBuddies(buddies);

        ceo.setBonus(1000000);
        cfo.setBonus(500000);
        Manager[] managers = {ceo,cfo};

        Pair<Employee> result = new Pair<>();
        minmaxBonus(managers,result);
        System.out.println("first: "+result.getFirst().getName()+",second:"+result.getSecond().getName());

        maxminBonus(managers,result);
        System.out.println("first: "+result.getFirst().getName()+",second:"+result.getSecond().getName());
    }

    /**
     * 利用子类通配符读取特性，读取特定Pair类及子类，最终打印数据
     * @param p
     */
    public static void printBuddies(Pair<? extends Employee> p){
        Employee first = p.getFirst();
        Employee second = p.getSecond();
        System.out.println(first.getName()+" and "+second.getName()+" are buddies.");
    }

    /**
     * 利用超类通配符可写特性，写入特定Pair类及父类,将managers数组处理后写入Pair
     * @param managers
     * @param result
     */
    public static void minmaxBonus(Manager[] managers,Pair<? super Manager> result){
        if (managers.length == 0) return;
        Manager min = managers[0];
        Manager max = managers[0];
        for (int i = 1; i < managers.length; i++) {
            if (min.getBonus().intValue() > managers[i].getBonus().intValue()) min = managers[i];
            if (max.getBonus().intValue() < managers[i].getBonus().intValue()) max = managers[i];
        }
        result.setFirst(min);
        result.setSecond(max);
    }

    /**
     * 利用超类通配符可写特性，写入特定Pair类及父类,将managers数组处理后写入Pair
     * @param managers
     * @param result
     */
    public static void maxminBonus(Manager[] managers,Pair<? super Manager> result) {
        minmaxBonus(managers,result);
        PairAlgs.swap(result);
    }
}

class PairAlgs{
    public static boolean hasNulls(Pair<?> p){
        //p.setFirst(new Object());无限定通配符无法写入参数
        return p.getFirst() == null || p.getSecond() == null;
    }

    public static void swap(Pair<?> p){
        swapHelper(p);
    }

    /**
     * 泛型方法，交换字段参数，通配符捕获参数T捕获通配符
     * @param p
     * @param <T>
     */
    public static <T> void swapHelper(Pair<T> p){
        T t = p.getFirst();
        p.setFirst(p.getSecond());
        p.setSecond(t);
    }
}

class Pair<T> {

    private T first;
    private T second;

    public Pair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public Pair() {
        first =null;
        second = null;
    }

    public T getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(T second) {
        this.second = second;
    }
}





