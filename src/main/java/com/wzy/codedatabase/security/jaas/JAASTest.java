package com.wzy.codedatabase.security.jaas;

import javax.swing.*;
import java.awt.*;

/**
 * 基于JAAS框架实现用户角色的自定义认证和授权。
 * 运行命令：java -classpath login.jar;.;action.jar -Djava.security.policy=jaas/JAASTest.policy -Djava.security.auth.login.config=jaas/jaas.config jaas.JAASTest
 * This program authenticates a user via a custom login and then looks up a system property
 * with the user's privileges.
 * @version 1.03 2018-05-01
 * @author Cay Horstmann
 */
public class JAASTest
{
   public static void main(final String[] args)
   {
      System.setSecurityManager(new SecurityManager());
      EventQueue.invokeLater(() ->
         {
            JAASFrame frame = new JAASFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("JAASTest");
            frame.setVisible(true);
         });
   }
}
