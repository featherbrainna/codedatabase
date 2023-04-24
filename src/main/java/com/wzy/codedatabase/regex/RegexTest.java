package com.wzy.codedatabase.regex;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 基于Pattern和Matcher的JavaAPI正则表达式测试器
 * This program tests regular expression matching. Enter a pattern and strings to match,
 * or hit Cancel to exit. If the pattern contains groups, the group boundaries are displayed
 * in the match.
 * @author 王忠义
 * @version 1.0
 * @date: 2023/4/24 15:13
 */
public class RegexTest
{
    public static void main(String[] args) throws PatternSyntaxException
    {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter pattern: ");
        String patternString = in.nextLine();

        //(([1-9]|1[0-9]|2[0-4]):([0-5][0-9]))[ap]m  24小时正则表达式
        Pattern pattern = Pattern.compile(patternString);//根据正则字符串创建Pattern对象

        while (true)
        {
            System.out.println("Enter string to match: ");
            String input = in.nextLine();
            if (input == null || input.equals("")) return;
            Matcher matcher = pattern.matcher(input);//模式对象中获取一个matcher对象
            if (matcher.matches())//判断是否匹配
            {
                System.out.println("Match");
                int g = matcher.groupCount();
                if (g > 0)
                {
                    for (int i = 0; i < input.length(); i++)
                    {
                        // Print any empty groups
                        for (int j = 1; j <= g; j++)
                            if (i == matcher.start(j) && i == matcher.end(j))
                                System.out.print("()");
                        // Print ( for non-empty groups starting here
                        for (int j = 1; j <= g; j++)
                            if (i == matcher.start(j) && i != matcher.end(j))
                                System.out.print('(');
                        System.out.print(input.charAt(i));
                        // Print ) for non-empty groups ending here
                        for (int j = 1; j <= g; j++)
                            if (i + 1 != matcher.start(j) && i + 1 == matcher.end(j))
                                System.out.print(')');
                    }
                    System.out.println();
                }
            }
            else
                System.out.println("No match");
        }
    }
}
