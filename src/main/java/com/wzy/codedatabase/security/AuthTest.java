package com.wzy.codedatabase.security;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * 基于JAAS框架的LoginContext的Windows NT登录的登录认证和授权实现
 * AuthTest是登录类
 * 运行命令：java -classpath D:/Desktop/java学习资料/java/corejava11/v2ch10/auth/login.jar;D:/Desktop/java学习资料/java/corejava11/v2ch10/auth/action.jar -Djava.security.policy=auth/AuthTest.policy -Djava.security.auth.login.config=auth/jaas.config auth.AuthTest
 * 1.注意打JAR包时要用class字节码文件打，不能用源码。
 * 2.System.setProperty("java.security.policy","auth/AuthTest.policy");设置要先于System.setSecurityManager(new SecurityManager());
 * This program authenticates a user via a custom login and then executes the SysPropAction
 * with the user's privileges.
 * @version 1.02 2018-05-01
 * @author Cay Horstmann
 */
public class AuthTest
{
   public static void main(final String[] args)
   {
//      System.setProperty("java.security.policy","auth/AuthTest.policy");
//      System.setProperty("java.security.auth.login.config","auth/jaas.config");
      System.setSecurityManager(new SecurityManager());
      try
      {
         LoginContext context = new LoginContext("Login1");//创建登录控制上下文
         context.login();//基于NT登录的认证检查
         System.out.println("Authentication successful.");
         Subject subject = context.getSubject();
         System.out.println("subject=" + subject);
         SysPropAction action = new SysPropAction("user.home");
         String result = Subject.doAsPrivileged(subject, action, null);//封装授权检查
         System.out.println(result);
         context.logout();
      }
      catch (LoginException e)
      {
         e.printStackTrace();
      }
   }
}
