package com.wzy.codedatabase.security.jaas;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.swing.*;
import java.awt.*;

/**
 * This frame has text fields for user name and password, a field for the name of the requested
 * system property, and a field to show the property value.
 */
public class JAASFrame extends JFrame
{
   private JTextField username;
   private JPasswordField password;
   private JTextField propertyName;
   private JTextField propertyValue;

   public JAASFrame()
   {
      username = new JTextField(20);
      password = new JPasswordField(20);
      propertyName = new JTextField("user.home");
      propertyValue = new JTextField(20);
      propertyValue.setEditable(false);

      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(0, 2));
      panel.add(new JLabel("username:"));
      panel.add(username);
      panel.add(new JLabel("password:"));
      panel.add(password);
      panel.add(propertyName);
      panel.add(propertyValue);
      add(panel, BorderLayout.CENTER);

      JButton getValueButton = new JButton("Get Value");
      getValueButton.addActionListener(event -> getValue());//点击JButton就会登录并认证，认证后执行权限的方法
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(getValueButton);
      add(buttonPanel, BorderLayout.SOUTH);
      pack();
   }

   public void getValue()
   {
      try
      {
         LoginContext context = new LoginContext("Login1", new SimpleCallbackHandler(
            username.getText(), password.getPassword()));//创建一个登录上下文
         System.out.println("Trying to log in with " + username.getText() 
            + " and " + new String(password.getPassword()));
         context.login();//登录并通过SimpleLoginModule进行登录检查
         Subject subject = context.getSubject();//获取登录主体，其主体内容在登录时通过SimpleCallbackHandler初始化
         propertyValue.setText("" + Subject.doAsPrivileged(subject,
            new SysPropAction(propertyName.getText()), null));//Subject.doAsPrivileged使用subject执行SysPropAction，验证用户权限
         context.logout();
      }
      catch (LoginException e)
      {
         e.printStackTrace();
         Throwable cause = e.getCause();
         if (cause != null) cause.printStackTrace();         
      }
   }
}
