package com.wzy.codedatabase.properties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 基于属性文件的窗口图片查看器（读取保存属性文件）
 * A program to test properties. The program remembers the frame position, size,
 * and last selected file.
 * @version 1.10 2018-03-15
 * @author Cay Horstmann
 */
public class ImageViewer
{
    public static void main(String[] args)
    {
        EventQueue.invokeLater(() -> {
            ImageViewerFrame frame = new ImageViewerFrame();
            frame.setTitle("ImageViewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}

/**
 * An image viewer that restores position and size from a properties file
 * and updates the properties upon exit.
 */
class ImageViewerFrame extends JFrame
{
    private static final int DEFAULT_WIDTH = 3000;//窗口默认宽度
    private static final int DEFAULT_HEIGHT = 2000;//窗口默认高度

    private File propertiesFile;//属性文件
    private Properties settings;//属性映射对象
    private String image;//图片地址
    private JLabel label = new JLabel();//窗口内容

    public ImageViewerFrame()//程序主方法方法运行时会调用构造
    {
        // get position, size, title from properties

        String userDir = System.getProperty("user.home");
        File propertiesDir = new File(userDir, ".corejava");
        if (!propertiesDir.exists()) propertiesDir.mkdir();
        propertiesFile = new File(propertiesDir, "ImageViewer.properties");//设置属性文件

        Properties defaultSettings = new Properties();
        defaultSettings.setProperty("left", "0");
        defaultSettings.setProperty("top", "0");
        defaultSettings.setProperty("width", "" + DEFAULT_WIDTH);
        defaultSettings.setProperty("height", "" + DEFAULT_HEIGHT);
        defaultSettings.setProperty("title", "");

        settings = new Properties(defaultSettings);//自动设置的默认属性，此属性会被读取的属性文件覆盖

        //读取属性文件
        if (propertiesFile.exists())
            try (FileInputStream in = new FileInputStream(propertiesFile))
            {
                settings.load(in);//读取本地属性文件（磁盘读取）
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }

        int left = Integer.parseInt(settings.getProperty("left"));
        int top = Integer.parseInt(settings.getProperty("top"));
        int width = Integer.parseInt(settings.getProperty("width"));
        int height = Integer.parseInt(settings.getProperty("height"));//读取属性（内存结构之间读取），此属性首先源于配置文件，再源于程序缺省值
        setBounds(left, top, width, height);//通过读取设置窗口
        image = settings.getProperty("image");//读取配置文件图片地址
        if (image != null) label.setIcon(new ImageIcon(image));//通过读取设置窗口内容

        //存储实时属性文件
        addWindowListener(new WindowAdapter()//匿名内部类实现窗口关闭时要执行的动作（设置属性对象并保存）
        {
            public void windowClosing(WindowEvent event)
            {
                settings.setProperty("left", "" + (int) getX());
                settings.setProperty("top", "" + (int) getY());
                settings.setProperty("width", "" + (int) getWidth());
                settings.setProperty("height", "" + (int) getHeight());//设置属性对象（内存中设置）
                if (image != null)
                    settings.setProperty("image", image);
                try (FileOutputStream out = new FileOutputStream(propertiesFile))
                {
                    settings.store(out, "Program Properties");//持久化属性文件（磁盘保存）
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });

        // use a label to display the images
        add(label);

        // set up the file chooser
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));

        // set up the menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        JMenuItem openItem = new JMenuItem("Open");
        menu.add(openItem);
        openItem.addActionListener(event -> {
            // show file chooser dialog
            int result = chooser.showOpenDialog(null);

            // if file selected, set it as icon of the label
            if (result == JFileChooser.APPROVE_OPTION)
            {
                image = chooser.getSelectedFile().getPath();
                label.setIcon(new ImageIcon(image));
            }
        });

        JMenuItem exitItem = new JMenuItem("Exit");
        menu.add(exitItem);
        exitItem.addActionListener(event -> System.exit(0));
    }
}
