package com.wzy.codedatabase.jni;

import java.util.Enumeration;

/**
*  基于JNI编程和C中注册表API的Java本地方法调用
*  C编译指令：cl -I C:\Java\jdk1.8.0_202\include -I C:\Java\jdk1.8.0_202\include\win32 -LD Win32RegKey.c advapi32.lib -FeWin32RegKey.dll
*  运行时要指定JVM参数共享库目录：-Djava.library.path=win32reg
*  @version 1.03 2018-05-01
*  @author Cay Horstmann
*/
public class Win32RegKeyTest
{  
   public static void main(String[] args)
   {
      Win32RegKey key = new Win32RegKey(
         Win32RegKey.HKEY_CURRENT_USER, "Software\\JavaSoft\\Java Runtime Environment");

      key.setValue("Default user", "Harry Hacker");
      key.setValue("Lucky number", new Integer(13));
      key.setValue("Small primes", new byte[] { 2, 3, 5, 7, 11 });

      Enumeration<String> e = key.names();

      while (e.hasMoreElements())
      {  
         String name = e.nextElement();
         System.out.print(name + "=");

         Object value = key.getValue(name);

         if (value instanceof byte[])
            for (byte b : (byte[]) value) System.out.print((b & 0xFF) + " ");
         else 
            System.out.print(value);

         System.out.println();
      }
   }
}
