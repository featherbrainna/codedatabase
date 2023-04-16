package com.wzy.codedatabase.juc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于高并发ConcurrentHashMap集合的多线程统计指定文件下多文件读取单词计数
 * This program demonstrates concurrent hash maps.
 * @version 1.0 2018-01-04
 * @author Cay Horstmann
 */
public class CHMDemo
{
    public static ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();//初始化线程安全哈希映射

    /**
     * 读取指定文件处理单词计数
     * Adds all words in the given file to the concurrent hash map.
     * @param file a file
     */
    public static void process(Path file)
    {
        try (Scanner in = new Scanner(file))//读取指定文件路径文件到输入流
        {
            while (in.hasNext())
            {
                String word = in.next();//循环读取一个单词
                map.merge(word, 1L, Long::sum);//原子更新映射条目
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 处理文件夹路径下所有的文件为文件路径集
     * Returns all descendants of a given directory--see Chapters 1 and 2 of Volume II
     * @param rootDir the root directory
     * @return a set of all descendants of the root directory
     */
    public static Set<Path> descendants(Path rootDir) throws IOException
    {
        try (Stream<Path> entries = Files.walk(rootDir))//流处理
        {
            return entries.collect(Collectors.toSet());
        }
    }

    public static void main(String[] args)
            throws InterruptedException, ExecutionException, IOException
    {
        int processors = Runtime.getRuntime().availableProcessors();//获取处理器核数
        ExecutorService executor = Executors.newFixedThreadPool(processors);//依据核数创建线程池
        Path pathToRoot = Paths.get(".");//读取当前目录路径
        for (Path p : descendants(pathToRoot))
        {
            if (p.getFileName().toString().endsWith(".java"))
                executor.execute(() -> process(p));//线程池循环获取线程处理文件单词计数（每个文件一个线程）
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
        map.forEach((k, v) ->//映射条目循环处理
        {
            if (v >= 10)
                System.out.println(k + " occurs " + v + " times");
        });
    }
}