package com.wzy.codedatabase.security.crypt;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;

/**
 * 基于Cipher和KeyGenerator的AES加密算法的密钥生成和加解密操作
 * AES是一种对称加密算法
 * This program tests the AES cipher. Usage:<br>
 * java aes.AESTest -genkey keyfile<br>
 * java aes.AESTest -encrypt plaintext encrypted keyfile<br>
 * java aes.AESTest -decrypt encrypted decrypted keyfile<br>
 * @author Cay Horstmann
 * @version 1.02 2018-05-01
 */
public class AESTest
{
   public static void main(String[] args) 
         throws IOException, GeneralSecurityException, ClassNotFoundException
   {
      if (args[0].equals("-genkey"))
      {
         KeyGenerator keygen = KeyGenerator.getInstance("AES");//为AES加密算法获取KeyGenerator
         SecureRandom random = new SecureRandom();
         keygen.init(random);//用随机源来初始化密钥发生器。
         SecretKey key = keygen.generateKey();//调用generateKey方法。
         try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(args[1])))
         {
            out.writeObject(key);//序列化密钥对象
         }
      }
      else
      {
         int mode;
         if (args[0].equals("-encrypt")) mode = Cipher.ENCRYPT_MODE;
         else mode = Cipher.DECRYPT_MODE;

         try (ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(args[3]));//密钥输入流
              FileInputStream in = new FileInputStream(args[1]);//输入流文件
              FileOutputStream out = new FileOutputStream(args[2]))//输出流文件
         {
            Key key = (Key) keyIn.readObject();//读取密钥对象
            Cipher cipher = Cipher.getInstance("AES");//获取AES密码对象，采用AES加密算法
            cipher.init(mode, key);//设置密码对象的模式和密钥进行初始化
            Util.crypt(in, out, cipher);//封装加解密过程，加解密过程是一样的
         }
      }
   }
}

