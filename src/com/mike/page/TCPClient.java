package com.mike.page;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class TCPClient extends JFrame {
    private JTextField jiptext = null;
    private JTextField userText = null;
    private JLabel jresult = null;
    private JTextField jtextsize = null;

    public static void main(String[] args) {
        TCPClient client = new TCPClient();
        client.setTitle("TCP_Client");
        client.setSize(350, 200);
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        client.add(panel);
        client.placeComponents(panel);
        client.setVisible(true);
    }

    public void placeComponents(JPanel panel) {
        panel.setLayout(null);
        JLabel jLabel = new JLabel("接收的文件(缺省在D:/out/目录)");
        jLabel.setBounds(5, 5, 300, 25);
        panel.add(jLabel);

        userText = new JTextField();
        userText.setBounds(5, 30, 250, 25);
        panel.add(userText);

        JLabel jsize = new JLabel("已接收字节数");
        jsize.setBounds(5, 60, 80, 25);
        panel.add(jsize);

        jtextsize = new JTextField();
        jtextsize.setBounds(90, 60, 120, 25);
        panel.add(jtextsize);

        JLabel jtxtsize = new JLabel("bytes");
        jtxtsize.setBounds(215, 60, 60, 25);
        panel.add(jtxtsize);

        JLabel jip = new JLabel("文件服务器IP地址");
        jip.setBounds(5, 85, 110, 25);
        panel.add(jip);

        jiptext = new JTextField();
        jiptext.setBounds(120, 85, 120, 25);
        panel.add(jiptext);

        JButton zhenting = new JButton("建立TCP");
        zhenting.setBounds(5, 120, 90, 25);
        panel.add(zhenting);

        jresult = new JLabel();
        jresult.setBounds(105, 120, 90, 25);
        panel.add(jresult);

        JButton shutsend = new JButton("中断接收");
        shutsend.setBounds(200, 120, 90, 25);
        panel.add(shutsend);


        zhenting.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Thread thread = new Thread(new SocketClientHandler());
                thread.setDaemon(true);
                if(jiptext.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(TCPClient.this, "IP不能为空", "消息提示", JOptionPane.ERROR_MESSAGE);
                }else if(userText.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(TCPClient.this, "接收文件不能为空", "消息提示", JOptionPane.ERROR_MESSAGE);
                }else {
                    thread.start();
                }
            }
        });

        shutsend.addActionListener(e -> {
            Thread.currentThread().interrupt();
        });
    }

    class SocketClientHandler implements Runnable {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(jiptext.getText().trim(), 20000);
                jresult.setText("连接已经建立。。");
                BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                byte[] info = new byte[256];
                bis.read(info);
                String file_name = new String(info).trim();
//                FileOutputStream fileOutputStream = new FileOutputStream(userText.getText());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(userText.getText()+file_name));
                userText.setText(userText.getText().concat(file_name));
                byte[] bytes = new byte[1024];
                int len = 0;
                int countlen = 0;
                while ((len = bis.read(bytes)) != -1) {
                    if(Thread.currentThread().isInterrupted()){
                        break;
                    }
                    bos.write(bytes, 0, len);
                    countlen+=len;
                    jtextsize.setText(String.valueOf(countlen));
                }
                bis.close();
                bos.close();
                socket.close();
                jresult.setText("文件接收完成。。");
            } catch (UnknownHostException e) {
                JOptionPane.showMessageDialog(TCPClient.this, "IP填写错误", "消息提示", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(TCPClient.this, "传输错误", "消息提示", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
