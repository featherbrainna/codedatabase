package com.wzy.codedatabase.geneic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * @author 王忠义
 * @version 1.0
 * @date: 2023/3/30 15:38
 * 泛型相当于普通类的工厂
 * 可以参照ArrayList<T>进行泛型类编写
 */
public class GeneicTemplate<T> {

    private T first;
    private T second;

    public GeneicTemplate(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public GeneicTemplate() {
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

    //方式一：泛型类中构建泛型变量对象，new T();
    public <T> GeneicTemplate<T> makePairlab(Supplier<T> constr){
        return new GeneicTemplate<>(constr.get(),constr.get());
    }

    //方式二：泛型类中构建泛型变量对象，new T();
    public <T> GeneicTemplate<T> makePair(Class<T> cls) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return new GeneicTemplate<>(cls.getConstructor().newInstance(),cls.getConstructor().newInstance());
    }

    //方式一：泛型类中构建泛型变量数组对象，new T[];
    public static <T> T[] makeTarr(IntFunction<T[]> constr){
        return constr.apply(2);
    }

    //方式二：泛型类中构建泛型变量数组对象，new T[];
    public static <T> T[] makeTarr1(T t){
        return (T[]) Array.newInstance(t.getClass().getComponentType(),2);
    }

    //泛型类重写Object中equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneicTemplate)) return false;
        GeneicTemplate<T> pair = (GeneicTemplate<T>) o;
        return Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second);
    }

    //泛型类重写Object中hashCode方法
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    //泛型类重写Object中toString方法
    @Override
    public String toString() {
        return "GeneicTemplate{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}

class ArrayAlg{
    //根据泛型类对象得到其值，调用泛型
    public void getPairValue(GeneicTemplate<? extends Employee> pair){
        pair.getFirst();
        //pair.setFirst(new Manager());无法更改
    }

    //根据泛型类对象更改其值，调用泛型
    public void setPairValue(GeneicTemplate<? super Manager> pair){
        pair.setFirst(new Manager());
        Object first = pair.getFirst();//访问受限，只能赋给Object
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Employee {
    private String name;
    private BigDecimal bonus;

    public void setBonus(int bonus) {
        this.bonus = new BigDecimal(bonus);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Manager extends Employee {
    private String name;
    private BigDecimal bonus;

    public void setBonus(int bonus) {
        this.bonus = new BigDecimal(bonus);
    }
}
