
import java.io.*;
import java.net.*;
import java.nio.file.*;
import javax.swing.*;

import java.util.*;
import java.awt.*;

public class Server {

    ServerSocket socketServer;
    Socket socketClient;
    final int PORT = 8080;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {
        // เพิ่ม gui
        // Frame
        JFrame frameServer = new JFrame();
        frameServer.setTitle("Server");
        frameServer.setSize(600, 600);
        frameServer.setFont(new Font("TH-Sarabun-PSK", Font.BOLD, 13));
        frameServer.setVisible(true);
        //panel welcome
        JPanel jPanel = new JPanel();
        //jPanel.setBounds(156, 424, 156, 424);
        jPanel.setBounds(220, 250, 185, 92);
        //panel buttonlog
        JPanel jPanel2 = new JPanel();
        jPanel2.setBounds(280, 67, 133, 92);
        //button
        JButton jButton1 = new JButton();
        jButton1.setText("Log");
        //lable welcome
        JLabel jLabel1 = new JLabel();
        jLabel1.setText("Welcome To Server");
        jLabel1.setFont(new Font("TH-Sarabun-PSK", Font.BOLD,20 ));
        //
        frameServer.add(jPanel);
        frameServer.add(jPanel2);
        jPanel.add(jLabel1);
        jPanel.add(jButton1);
        try {
            socketServer = new ServerSocket(PORT);
            ServerSocket serverSocket = new ServerSocket(8087);
            while (true) {
                System.out.println("Waiting Connecting from client : " + PORT);
                socketClient = socketServer.accept();
                new HandleClient(socketClient, serverSocket).start();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private AbstractButton getContentPane() {
        return null;
    }
}

class HandleClient extends Thread {

    Server server;
    ServerSocket socketServer;
    Socket socketClient;
    DataInputStream din;
    DataOutputStream dout;
    String path = "C:/Users/Pond/Desktop/ปี3เทอม1/Operating Systems/OSproJ new/OSProject/FileServer";
    File file = new File(path);
    File[] fileName;

    public HandleClient(Socket socket, ServerSocket serverSocket) {
        this.socketClient = socket;
        this.socketServer = serverSocket;
    }

    public void sendNameAllFileToClient() {
        try {
            fileName = file.listFiles();
            din = new DataInputStream(socketClient.getInputStream());
            dout = new DataOutputStream(socketClient.getOutputStream());
            dout.writeInt(fileName.length);
            for (File f : fileName) {
                dout.writeUTF(f.getName());
            }
            for (File f : fileName) {
                dout.writeUTF("" + Files.probeContentType(f.toPath())); // ชนิดข้อมูลไฟล์
            }
            for (File f : fileName) {
                long tem = f.length() / 1024 + 1;
                dout.writeUTF("" + tem); // ขนาดไฟล์
            }
            sendFileReqToClient();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void sendFileReqToClient() throws IOException {

        String reqFile;
        try {
            din = new DataInputStream(socketClient.getInputStream());
            dout = new DataOutputStream(socketClient.getOutputStream());

            while (true) {
                reqFile = din.readUTF();
                System.out.println("req file from client : " + reqFile);
                for (int i = 0; i < fileName.length; i++) {
                    if (reqFile.equals(fileName[i].getName())) {
                        File file = fileName[i];
                        dout.writeInt((int) file.length());
                        int sizeFile = (int) file.length() / 10;
                        for (int start = 0; start < 10; start++) {
                            System.out.println("index : " + start);
                            Socket socket = socketServer.accept();
                            int s = start;
                            int fileLength = start == 9 ? (int) file.length() - (sizeFile * 9) : sizeFile;
                            int indexStart = s * sizeFile;
                            new Thread(() -> {
                                try {
                                    DataOutputStream doutClient = new DataOutputStream(socket.getOutputStream());
                                    DataInputStream dinClient = new DataInputStream(
                                            new FileInputStream(file.getAbsolutePath()));
                                    doutClient.writeInt(indexStart);
                                    doutClient.writeInt(fileLength);
                                    byte[] dataPatial = new byte[fileLength];

                                    System.out.println("Start : " + indexStart);
                                    System.out.println("File : " + fileLength);

                                    System.out.println(Thread.currentThread().getName() + " start :" + indexStart
                                            + "end : " + (indexStart + fileLength) + " flieLength :" + fileLength);

                                    dinClient.skip(indexStart);
                                    dinClient.read(dataPatial);
                                    System.out.println(Thread.currentThread().getName() + " skiped ");
                                    int send = 0;

                                    doutClient.write(dataPatial);

                                    System.out.println("finish");
                                    doutClient.close();
                                    dinClient.close();
                                    socket.close();
                                } catch (IOException ex) {

                                    System.out.println(ex);
                                }

                            }).start();
                        }
                    }
                }

            }

        } catch (Exception e) {
            System.out.println("Not Send");
        }

    }

    public void run() {
        sendNameAllFileToClient();
    }
}
