package com.wzy.codedatabase.regex;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 基于正则匹配的读取网页所有字节输入到字符串并匹配多个内容结果输出
 * This program displays all URLs in a web page by matching a regular expression that
 * describes the <a href=...> HTML tag. Start the program as <br>
 * java match.HrefMatch URL
 * @version 1.04 2019-08-28
 * @author Cay Horstmann
 */
public class HrefMatch
{
    public static void main(String[] args)
    {
        try
        {
            // get URL string from command line or use default
            String urlString;
            if (args.length > 0) urlString = args[0];
            else urlString = "https://openjdk.org/";

            // read contents of URL
            InputStream in = new URL(urlString).openStream();
//         byte[] result = readAllBytes1(in);//低效率方法
            byte[] result = readAllBytes(in);//高效率方法
            String input = new String(result, StandardCharsets.UTF_8);

            // search for all occurrences of pattern
            String patternString = "<a\\s+href\\s*=\\s*(\"[^\"]*\"|[^\\s>]*)\\s*>";
            Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
            //Java9语法
//         pattern.matcher(input)
//            .results()
//            .map(MatchResult::group)
//            .forEach(System.out::println);

            //java8语法
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()){
                System.out.println(matcher.group());
            }
        }
        catch (IOException | PatternSyntaxException e)
        {
            e.printStackTrace();
        }
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        List<byte[]> bytes = new ArrayList<>();//收集流数据的表定义
        int cacheSize = 1024;//缓冲数组对象的大小
        byte[] cache = new byte[cacheSize];//缓存内存空间定义
        int n ;//记录读取的字节值，并判断读完退出循环
        do {
            int i = 0;
            while ((n = in.read())!=-1&&i<cacheSize){//输入流结束或缓存满退出当前循环
                cache[i] = (byte) n;
                i++;
            }
            bytes.add(cache);
        }while (n!=-1);//继续缓存并循环读取输入流内容
        byte[] result = new byte[bytes.size() * cacheSize];//最终结果
        int count=0;
        for (byte[] item : bytes) {
            System.arraycopy(item,0,result,count*cacheSize,cacheSize);
            count++;
        }
        return result;
    }

    private static byte[] readAllBytes1(InputStream in) throws IOException {
        List<Byte> bytes = new ArrayList<>();
        //注意此循环中不能使用in.available收集所有字节！！！
        int c;
        while ((c= in.read())!=-1){
            bytes.add((byte) c);//有一个问题ArrayList频繁扩容影响效率,因此第一个方法效率更低
        }
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }
}
