package com.wzy.codedatabase.filedeal;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * 基于Files.walkFileTree文件目录的递归删除
 * 通过FileVisitor的协议细粒度的控制遍历过程的工作处理
 * @author 王忠义
 * @version 1.0
 * @date: 2023/4/23 18:42
 */
public class FileCategoryDelete {
    public static void main(String[] args) throws IOException {
        //递归删除目录
        Path path = Paths.get("test1");//定义要删除的目录
        //基于walkFileTree遍历目录树，并代理SimpleFileVisitor来处理遍历过程（文件和目录的删除）
        Files.walkFileTree(path,new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);//删除文件
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc!=null) throw exc;
                Files.delete(dir);//删除目录
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
