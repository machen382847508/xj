package com.mike.page;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer extends JFrame {
    private static String path = "";
    final static Object locks =new Object();
    private JLabel jresult = null;
    private ExecutorService executorService;
    private boolean ThreadState;

    public static void main(String[] args) {
        TCPServer server = new TCPServer();
        server.setTitle("TCP_SERVER");
        server.setSize(350, 200);
        server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        server.add(panel);
        server.placeComponents(panel);
        server.setVisible(true);
    }

    public void placeComponents(JPanel panel) {
        panel.setLayout(null);
        JLabel jLabel = new JLabel("发送的文件");
        jLabel.setBounds(5, 5, 80, 25);
        panel.add(jLabel);

        JTextField userText = new JTextField();
        userText.setBounds(5, 30, 250, 25);
        panel.add(userText);

        JButton openFilebtn = new JButton("....");
        openFilebtn.setBounds(255, 30, 50, 25);
        panel.add(openFilebtn);

        JLabel jsize = new JLabel("文件大小");
        jsize.setBounds(5, 60, 60, 25);
        panel.add(jsize);

        JTextField jtextsize = new JTextField();
        jtextsize.setBounds(65, 60, 120, 25);
        panel.add(jtextsize);

        JLabel jtxtsize = new JLabel("bytes");
        jtxtsize.setBounds(190, 60, 60, 25);
        panel.add(jtxtsize);

        JLabel jip = new JLabel("本机IP");
        jip.setBounds(5, 85, 60, 25);
        panel.add(jip);

        JTextField jiptext = new JTextField();
        jiptext.setBounds(65, 85, 120, 25);

        try {
            InetAddress addr = InetAddress.getLocalHost();
            jiptext.setText(addr.getHostAddress());
            panel.add(jiptext);
        } catch (UnknownHostException e) {
            jiptext.setText("请自己输入ip");
            e.printStackTrace();
        }

        JButton zhenting = new JButton("开始侦听");
        zhenting.setBounds(5, 120, 90, 25);
        panel.add(zhenting);

        jresult = new JLabel();
        jresult.setBounds(100, 120, 90, 25);
        panel.add(jresult);

        JButton send = new JButton("开始发送");
        send.setBounds(200, 120, 90, 25);
        panel.add(send);

        /**
         * 点击打开文件
         * */
        openFilebtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("."));//设置默认显示为当前文件夹
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);//设置选择模式（只选文件、只选文件夹、文件和文件均可选）
            fc.setMultiSelectionEnabled(false);//是否允许多选
            int result = fc.showOpenDialog(TCPServer.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (file != null) {
                    userText.setText(String.valueOf(file.getAbsoluteFile()));
                    jtextsize.setText(String.valueOf(file.length()));
                }
            }
        });

        /***
         * 点击开始侦听按钮
         */
        zhenting.addActionListener(e -> {
            Thread thread = new Thread(new SocketServerHandler());
            thread.setDaemon(true);
            if(!thread.isAlive() && !ThreadState){
                thread.start();
                jresult.setText("等待建立连接");
            }
        });

        send.addActionListener(e -> {
            if (userText.getText() == null || userText.getText().equals("")) {
                JOptionPane.showMessageDialog(TCPServer.this, "请选择文件路径！", "消息提示", JOptionPane.ERROR_MESSAGE);
            }else {
                path = userText.getText();
                synchronized (locks) {
                    //唤醒线程
                    locks.notifyAll();
                }
            }
        });
    }

    class SocketServerHandler implements Runnable {
        @Override
        public void run() {
            try {
                Socket socket = null;
                executorService = Executors.newCachedThreadPool();
                ServerSocket serverSocket = new ServerSocket(20000);
                while((socket = serverSocket.accept()) != null){
                    executorService.execute(new SocketServerExecute(socket));
                }
                ThreadState = true;
            } catch (IOException e) {
                e.printStackTrace();
                ThreadState = false;
            }
        }
    }

    class SocketServerExecute implements Runnable {

        private Socket socket;
        public SocketServerExecute(Socket client) {
            this.socket = client;
        }

        @Override
        public void run() {
            jresult.setText("连接完成");
            // 将文件内容从硬盘读入内存中
            try {
                synchronized (locks){
                    locks.wait();
                }
                File file = new File(path);
                FileInputStream fs = new FileInputStream(file);
                byte[] b = file.getName().getBytes();
                byte[] info = Arrays.copyOf(b,256);
                ByteArrayInputStream bais = new ByteArrayInputStream(info);
                SequenceInputStream sis = new SequenceInputStream(bais, fs);
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                byte[] buffer = new byte[256];
                int len = 0;
                // 因为文件内容较大，不能一次发送完毕，因此需要通过循环来分次发送
                while ((len = sis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                bos.close();
                sis.close();
                fs.close();
                jresult.setText("文件发送完成");
                socket.close();
                socket = null;
            }catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}