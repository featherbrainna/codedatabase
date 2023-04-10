package com.wzy.codedatabase.geneic;

import java.util.function.IntFunction;

/**
 * 基于泛型数组实现的泛型环形队列
 * 可以存取任意对象数据到队列
 * @author 王忠义
 * @version 1.0
 * @date: 2023/4/10 21:52
 */
class GeneicCircleArray<T>{
    private int maxSize;//表示数组的最大容量
    private int front;//队列头,指向队列头索引
    private int rear;//队列尾，指向队尾后一个索引索引
    private T[] arr;//该数组用于存放数据，模拟队列

    public GeneicCircleArray(int maxSize, IntFunction<T[]> constr) {
        this.maxSize = maxSize;
        this.arr = constr.apply(maxSize);
    }

    /**
     * 判断队列是否满
     * @return
     */
    public boolean isFull(){
        return (rear+1)%maxSize == front;
    }

    /**
     * 判断队列是否为空
     * @return
     */
    public boolean isEmpty(){
        return front == rear;
    }

    /**
     * 给队列添加数据
     * @param value
     */
    public void addQueue(T value){
        if (isFull()) {
            throw new RuntimeException("队列已满，无法加入数据");
        }
        arr[rear] = value;
        rear = (rear+1)%maxSize;//让rear后移
    }

    /**
     * 获取队列数据
     * @return
     */
    public T getQueue(){
        if (isEmpty()){
            throw new RuntimeException("队列已空,无法获取数据");
        }
        int temp = front;
        front = (front+1)%maxSize;
        return arr[temp];
    }

    /**
     * 显示队列数据
     */
    public void showQueue(){
        if (isEmpty()){
            throw new RuntimeException("队列已空，无法显示数据");
        }
        for (int i = front; i < front+getQueueLength(); i++) {
            System.out.printf("arr[%d]=%s\n",i%maxSize,arr[i%maxSize]);//巧妙地转到数组前面的索引进行遍历！！！
        }
    }

    /**
     * 显示队列的头数据
     * @return
     */
    public T peekQueueHead(){
        if (isEmpty()) throw new RuntimeException("队列已空，无法显示对头数据");
        return arr[front];
    }

    /**
     * 获取队列长度
     * @return
     */
    public int getQueueLength(){
        return (rear + maxSize - front)%maxSize;
    }
}
