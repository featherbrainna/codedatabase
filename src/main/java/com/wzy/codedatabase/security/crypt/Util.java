package com.wzy.codedatabase.security.crypt;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public class Util
{
   /**
    * Uses a cipher to transform the bytes in an input stream and sends the transformed bytes
    * to an output stream.
    * @param in the input stream
    * @param out the output stream
    * @param cipher the cipher that transforms the bytes
    */
   public static void crypt(InputStream in, OutputStream out, Cipher cipher) 
         throws IOException, GeneralSecurityException
   {
      int blockSize = cipher.getBlockSize();
      int outputSize = cipher.getOutputSize(blockSize);
      byte[] inBytes = new byte[blockSize];
      byte[] outBytes = new byte[outputSize];

      int inLength = 0;
      boolean done = false;
      while (!done)
      {
         inLength = in.read(inBytes);//输入流读取到字节数组inBytes
         if (inLength == blockSize)//依据读取情况进行加解密操作
         {
            int outLength = cipher.update(inBytes, 0, blockSize, outBytes);//加解密
            out.write(outBytes, 0, outLength);//持久化
         }
         else done = true;
      }
      if (inLength > 0) outBytes = cipher.doFinal(inBytes, 0, inLength);//最后一次加解密
      else outBytes = cipher.doFinal();//最后一次加解密
      out.write(outBytes);//持久化
   }
}
