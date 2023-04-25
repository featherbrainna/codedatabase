package com.wzy.codedatabase.web;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 基于ServerSocket和多线程的多客户端服务
 * This program implements a multithreaded server that listens to port 8189 and echoes back
 * all client input.
 * @author Cay Horstmann
 * @version 1.23 2018-03-17
 */
public class ThreadedEchoServer
{
    public static void main(String[] args )
    {
        try (ServerSocket s = new ServerSocket(8189))//创建一个服务端socket并用它监听进程端口
        {
            int i = 1;

            while (true)
            {
                Socket incoming = s.accept();//依据端口接收到的请求创建socket进行处理连接客户端
                System.out.println("Spawning " + i);
                Runnable r = new ThreadedEchoHandler(incoming);//创建连接的线程任务
                Thread t = new Thread(r);//创建连接处理的线程
                t.start();
                i++;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

/**
 * This class handles the client input for one server socket connection.
 */
class ThreadedEchoHandler implements Runnable
{
    private Socket incoming;

    /**
     Constructs a handler.
     @param incomingSocket the incoming socket
     */
    public ThreadedEchoHandler(Socket incomingSocket)
    {
        incoming = incomingSocket;
    }

    public void run()
    {
        try (InputStream inStream = incoming.getInputStream();
             OutputStream outStream = incoming.getOutputStream();
             Scanner in = new Scanner(inStream, String.valueOf(StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(outStream, StandardCharsets.UTF_8),
                     true /* autoFlush */))
        {
            out.println( "Hello! Enter BYE to exit." );

            // echo client input
            boolean done = false;
            while (!done && in.hasNextLine())
            {
                String line = in.nextLine();
                out.println("Echo: " + line);
                if (line.trim().equals("BYE"))
                    done = true;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
