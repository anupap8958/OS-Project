
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;

public class Server {
    public static final int PORT = 8087;
    File[] fileName;
    public static void main(String[] args) throws IOException {
       new Server().Model();
    }

    private void Model() throws IOException {
        // Reader name file
        String path = "C:/Users/Pond/Desktop/ปี3เทอม1/Operating Systems/Server file";
        File file = new File(path);
        fileName = file.listFiles();
        String[][] arr = new String[fileName.length][2];
        for (int i = 0; i < fileName.length; i++) {
            arr[i][0] = fileName[i].getName();
            arr[i][1] = fileName[i].length()/1024+1 +" "+"KB";
        }
        String[] col = { "file", "size" };

        // frame Main Server
        JFrame frameMain = new JFrame();
        frameMain.setSize(500, 800);
        frameMain.setTitle("Welcome to Server");
        frameMain.setVisible(true);
        JPanel jpMain = new JPanel();
        jpMain.setLayout(new CardLayout());
        // ส่วนของหน้าmain ปุ่ม File
        JPanel jpBut = new JPanel();
        JButton butFile = new JButton("File");
        butFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Frame Server
                JFrame frameServer = new JFrame();
                frameServer.setSize(500, 800);
                frameServer.setTitle("Server");
                frameServer.setLayout(new BoxLayout(frameServer.getContentPane(), BoxLayout.Y_AXIS));
                frameServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frameServer.setVisible(true);
                frameMain.setVisible(false);
                // ตารางชื่อไฟล์และขนาด
                JPanel jpFile = new JPanel();
                JTable jtFile = new JTable(arr, col);
                JScrollPane jspFile = new JScrollPane(jtFile);
                jpFile.setBounds(0, 0, 500, 400);
                jpFile.add(jspFile);
                frameServer.getContentPane().add(jpFile);
                JPanel jpBack = new JPanel();
                JButton butBack = new JButton("Back");
                butBack.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        frameServer.setVisible(false);
                        frameMain.setVisible(true);
                    }

                });
                jpBack.add(butBack);
                frameServer.add(jpBack);
            }

        });
        jpBut.add(butFile);

        // ส่วนของหน้าmain ปุ่ม log
        JButton butLog = new JButton("Log");
        jpBut.add(butLog);
        butLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Jframe
                JFrame frameLog = new JFrame();
                frameLog.setLayout(new BoxLayout(frameLog.getContentPane(), BoxLayout.Y_AXIS));
                frameLog.setTitle("Log");
                frameLog.setSize(500, 800);
                // Panel and Lable
                JPanel jpLog = new JPanel();
                JLabel jlLog = new JLabel();
                jlLog.setText("Waiting to connecting from Client ");

                jlLog.setText("Connecting from Client ");
                jpLog.add(jlLog);
                System.out.println("connect");

                frameLog.add(jpLog);
                frameLog.setVisible(true);
                frameMain.setVisible(false);
                JPanel jpBack = new JPanel();
                JButton butBack = new JButton("Back");
                butBack.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        frameLog.setVisible(false);
                        frameMain.setVisible(true);
                    }

                });
                jpBack.add(butBack);
                frameLog.add(jpBack);

            }

        });

        jpMain.add(jpBut, "Panel 2");
        frameMain.getContentPane().add(jpMain, BorderLayout.CENTER);

        // ส่วนของ connecting

        ServerSocket serverSocket = new ServerSocket(8087);
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new HandleClient(clientSocket).start();

            } catch (Exception e) {
                System.out.println("0");
            }
        }
    }

    class HandleClient extends Thread {
        Socket clientSocket;

        HandleClient(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                DataInputStream din = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());

                dout.writeInt(fileName.length);
                for(File f : fileName){
                    dout.writeUTF(f.getName());
                }
                for (File f : fileName) {
                    dout.writeUTF("" + Files.probeContentType(f.toPath())); // ชนิดข้อมูลไฟล์
                }
                for (File f : fileName) {
                    dout.writeLong(f.length()); // ขนาดไฟล์
                }
                while (true) {
                    String reqFile = din.readUTF();
                    //System.out.println(reqFile);
                    for(int i=0; i<fileName.length; i++){
                        if(fileName[i].getName().equals(reqFile)){
                            FileInputStream fileIn = new FileInputStream(fileName[i].getAbsolutePath());
                            byte [] data = new byte[(int)fileName[i].length()];
                            fileIn.read(data);
                            dout.writeInt(data.length);
                            dout.write(data);
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }
}
