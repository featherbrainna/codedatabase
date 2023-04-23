package com.wzy.codedatabase.filedeal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 基于Files.walk文件目录的递归文件复制
 * 将一个目录复制到另一个目录，通过walk逐个读取处理文件路径，用source路径将每个路径相对化再合并target目录就完成了处理。
 * @author 王忠义
 * @version 1.0
 * @date: 2023/4/23 16:47
 */
public class FileCategoryCopy {
    public static void main(String[] args) throws IOException {
        Path source = Paths.get("test");//要复制的目录
        Path target = Paths.get("test1");//复制的目的地目录，目的目录可以不存在
        //基于walk方法递归遍历文件并处理每个文件
        Files.walk(source).forEach(p->{
            try {
                System.out.println(p);
                Path q = target.resolve(source.relativize(p));//此方法的本质是文件路径相对化和文件路径合并的运用
                if (Files.isDirectory(p)){
                    Files.createDirectory(q);
                }else {
                    Files.copy(p,q);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
