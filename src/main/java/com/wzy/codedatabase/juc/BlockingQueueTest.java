package com.wzy.codedatabase.juc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于阻塞队列的多线程的查找指定文件夹下文件内容的指定关键字
 * 技术难点：
 * 1.基于阻塞队列实现的同步机制。
 * 2.基于多线程（消费者线程）完成搜索文件关键字并打印任务
 * 3.同步控制了写队列线程与读队列线程不能同时进行（同步了枚举线程和搜索线程对队列的操作）。
 *       同步控制了读队列进程不能同时进行。队列资源同一时间只有一个线程可以操作。（同步了搜索线程之间对队列的操作）
 *       （注：枚举是在一个线程进行的所以也是同步的）
 *       套路：同步XXX线程对XX资源的操作。
 * 4.而具体搜索任务是并发的，每个文件可同时进行搜索扫描。
 * @version 1.03 2018-03-17
 * @author Cay Horstmann
 */
public class BlockingQueueTest
{
    private static final int FILE_QUEUE_SIZE = 10;//10个文件队列大小
    private static final int SEARCH_THREADS = 100;//100个搜索线程
    private static final Path DUMMY = Paths.get("");//Path.of("")改为Java 8 中的Paths.get()
    private static BlockingQueue<Path> queue = new ArrayBlockingQueue<>(FILE_QUEUE_SIZE);//阻塞队列，容量10

    public static void main(String[] args)
    {//多线程完成一个查找文件任务的例子
        try (Scanner in = new Scanner(System.in))
        {
            System.out.print("Enter base directory (e.g. /opt/jdk-11-src): ");
            String directory = in.nextLine();//输入目录
            System.out.print("Enter keyword (e.g. volatile): ");
            String keyword = in.nextLine();//输入关键字

            Runnable enumerator = () -> {
                try
                {
                    enumerate(Paths.get(directory));//将目录中的文件路径递归的放入队列
                    queue.put(DUMMY);//结束标志
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                }
            };

            new Thread(enumerator).start();//一个文件路径罗列生产线程，生产者线程
            for (int i = 1; i <= SEARCH_THREADS; i++) {//文件路径搜索线程，消费者线程
                Runnable searcher = () -> {
                    try
                    {
                        boolean done = false;
                        while (!done)
                        {
                            Path file = queue.take();//从队列中获取路径
                            if (file == DUMMY)//结束文件路径就结束循环
                            {
                                queue.put(file);
                                done = true;
                            }
                            else search(file, keyword);//其他文件路径则，多个线程一起并发处理各个文件的搜索任务
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    catch (InterruptedException e)
                    {
                    }
                };
                new Thread(searcher).start();//启动多个消费者线程
            }
        }
    }

    /**
     * Recursively enumerates all files in a given directory and its subdirectories.
     * See Chapters 1 and 2 of Volume II for the stream and file operations.
     * @param directory the directory in which to start
     */
    public static void enumerate(Path directory) throws IOException, InterruptedException
    {
        try (Stream<Path> children = Files.list(directory))
        {
            for (Path child : children.collect(Collectors.toList()))
            {
                if (Files.isDirectory(child))
                    enumerate(child);
                else
                    queue.put(child);
            }
        }
    }

    /**
     * Searches a file for a given keyword and prints all matching lines.
     * @param file the file to search 要搜索的文件
     * @param keyword the keyword to search for 要搜索的文件中的关键字
     */
    public static void search(Path file, String keyword) throws IOException
    {
        try (Scanner in = new Scanner(file, String.valueOf(StandardCharsets.UTF_8)))//文件处理流，扫描器来扫描文件内容
        {
            int lineNumber = 0;
            while (in.hasNextLine())
            {
                lineNumber++;
                String line = in.nextLine();//逐行扫描文件内容
                if (line.contains(keyword))
                    System.out.printf("%s:%d:%s%n", file, lineNumber, line);
            }
        }
    }
}
