package com.wzy.codedatabase.security;

import java.security.PrivilegedAction;

/**
 * 一个操作类。
 * 通过Subject.doAsPrivileged会执行其run方法
 * This action looks up a system property.
 * @version 1.01 2007-10-06
 * @author Cay Horstmann   
*/
public class SysPropAction implements PrivilegedAction<String>
{
   private String propertyName;

   /**
      Constructs an action for looking up a given property.
      @param propertyName the property name (such as "user.home")
   */
   public SysPropAction(String propertyName) 
   { 
      this.propertyName = propertyName; 
   }

   public String run()
   {
      return System.getProperty(propertyName);
   }
}
