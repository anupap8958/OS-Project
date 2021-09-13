import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import javax.swing.*;
import java.awt.*;

public class Server {
    ServerSocket socketServer;
    final int PORT = 8080;

    JFrame frameLog = new JFrame();
    JTextArea textAreaLog = new JTextArea();

    LocalDateTime myDateObj = LocalDateTime.now();
    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    String date = myDateObj.format(myFormatObj);

    int clientNo = 1;

    public static void main(String[] args) {
        new Server();
    }

    public Server() {
        // frameLog
        frameLog.setSize(500, 500);
        frameLog.setTitle("SERVER [Log]");
        frameLog.setLocationRelativeTo(null);
        frameLog.setResizable(false);
        frameLog.setVisible(true);

        textAreaLog.setEditable(false);
        textAreaLog.setFont(new Font("TH-Sarabun-PSK", Font.BOLD, 13));
        textAreaLog.setLineWrap(true);
        textAreaLog.setWrapStyleWord(true);

        JScrollPane jScrollPaneLog = new JScrollPane(textAreaLog);
        jScrollPaneLog.setPreferredSize(new Dimension(450, 450));
        JPanel panelLog = new JPanel();
        panelLog.add(jScrollPaneLog);
        frameLog.add( panelLog);
        try {
            socketServer = new ServerSocket(PORT);
            ServerSocket serverSocket = new ServerSocket(8087);
            while (true) {
                Socket socket = socketServer.accept();
                textAreaLog.append("[ " + date + " ]" + " : Connecting from client [" + clientNo + "]\n");
                clientNo++;
                new HandleClient(socket, serverSocket).start();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    class HandleClient extends Thread {
        ServerSocket socketServer;
        Socket socketClient;
        DataInputStream din;
        DataOutputStream dout;
        String path = "C:/Users/tubti/OneDrive - Silpakorn University/Documents/Thread/server/";
        File file = new File(path);
        File[] fileName;

        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String date = myDateObj.format(myFormatObj);

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

                while (true) {
                    reqFile = din.readUTF();
                    textAreaLog.append("[ " + date + " ]" + " : Client requirement file ----> " + reqFile + "\n");
                    for (int i = 0; i < fileName.length; i++) {
                        if (reqFile.equals(fileName[i].getName())) {
                            File file = fileName[i];
                            dout.writeInt((int) file.length());
                            int sizeFile = (int) file.length() / 10;
                            for (int start = 0; start < 10; start++) {

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
                                        byte[] dataPatial = new byte[1024];
                                        // System.out.println(Thread.currentThread().getName() + " : start :" +
                                        // indexStart
                                        // + " , end : " + (indexStart + fileLength) + " , flieLength :" + fileLength);

                                        dinClient.skip(indexStart);

                                        int count = 0;
                                        int total = 0;

                                        while ((count = dinClient.read(dataPatial)) != -1) {
                                            total += count;

                                            doutClient.write(dataPatial, 0, count);
                                            if (total >= fileLength) {
                                                textAreaLog.append("[ " + date + " ]" + " : Server submit "
                                                        + Thread.currentThread().getName() + " successfully\n");
                                                break;
                                            }

                                        }
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
                // System.out.println("Not Send");
                e.printStackTrace();
            }

        }

        public void run() {
            sendNameAllFileToClient();
        }
    }
}