import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import javax.swing.*;
import java.awt.*;

public class Server {
    ServerSocket socketServer; //เป็นตัวกลางหรือเป็นช่องทางในการติดต่อ รับ-ส่ง ข้อมูล
    final int PORT = 8080; //port ที่เชื่อมต่อระหว่าง server กับ client

    JFrame frameLog = new JFrame(); 
    JTextArea textAreaLog = new JTextArea();

    //time
    LocalDateTime myDateObj = LocalDateTime.now();
    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    String date = myDateObj.format(myFormatObj);

    int clientNo = 1; //นับการเข้าเชื่อมต่อของ client

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
                Socket socket = socketServer.accept();//ทำการเชื่อมต่อ port เริ่มต้น
                textAreaLog.append("[ " + date + " ]" + " : Connecting from client [" + clientNo + "]\n");
                clientNo++;
                new HandleClient(socket, serverSocket).start(); //แตก server รองรับ client
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
        String path = "C:/Users/api_q/OneDrive/เดสก์ท็อป/OSProject/FileServer/";//ที่อยู่ของไฟล์ทั้งหมดของserver
        File file = new File(path);//เก็บไฟล์ทั้งหมดลง object
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
                din = new DataInputStream(socketClient.getInputStream()); // การอ่าน
                dout = new DataOutputStream(socketClient.getOutputStream());//การเขียน
                dout.writeInt(fileName.length);//ส่งจำนวนไฟล์ทั้งหมดที่โหลดได้ไปหา client
                for (File f : fileName) {
                    dout.writeUTF(f.getName());//ส่งชื่อไฟล์เป็นString
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
            String reqFile; //เก็บชื่อไฟล์ที่client req
            try {

                while (true) {
                    reqFile = din.readUTF();
                    textAreaLog.append("[ " + date + " ]" + " : Client requirement file ----> " + reqFile + "\n");
                    for (int i = 0; i < fileName.length; i++) {
                        if (reqFile.equals(fileName[i].getName())) {
                            File file = fileName[i]; //เก็บไฟล์ที่จะดาวน์โหลด
                            dout.writeInt((int) file.length()); //ส่งขนาดไฟล์ทั้งหมดไปหาClient
                            int sizeFile = (int) file.length()/10;
                            for (int start = 0; start < 10; start++) {

                                Socket socket = socketServer.accept();//เช็คการดาวน์โหลดของ client ว่า port ตรงกันไหม
                                int s = start; 
                                int fileLength = start == 9 ? (int) file.length() - (sizeFile * 9) : sizeFile;
                                int indexStart = s * sizeFile;//จุดเริ่ม thread แต่ละเ thread
                                new Thread(() -> {
                                    try {
                                        DataOutputStream doutClient = new DataOutputStream(socket.getOutputStream());
                                        DataInputStream dinClient = new DataInputStream(
                                                new FileInputStream(file.getAbsolutePath()));
                                        doutClient.writeInt(indexStart); //เขียนจุดเริ่มต้นของแต่ละ thread
                                        doutClient.writeInt(fileLength);//เขียนขนาดแต่ละ thread
                                        byte[] dataPatial = new byte[1024]; //สั่งให้เขียนข้อมูลทีละ 1024 byte
                                        //ที่ต้องเป็น 1024 เพราะว่ามันคือขนาดอาเรย์ที่ไปจองบนพื้นที่หน่วยความจำส่วน heap แต่ fileLength คือขนาดที่ถูกแบ่งเป็น 10 ส่วน แล้ว มีหน่วยเป็น byte 
                                        //ถ้าใช้กับไฟล์ขนาดใหญ่เช่น ไฟล์ 2GB ที่มีขนาดประมาณ 2 พันล้าน byte มันจะกินพื้นที่หน่วยความจำมากเกินไป และทำให้พื้นที่หน่วยความจำไม่พอ จะเกิดปัญหา java heap outOfMemory

                                        dinClient.skip(indexStart);//จุดนี้เป็นจุดบอกว่าแต่ละ thread เริ่มต้นที่ไหน

                                        int count = 0;
                                        int total = 0;

                                        while ((count = dinClient.read(dataPatial)) != -1) { //จะทำการอ่านค่า dataPatial ทีละ 1024 และเก็บลงค่า count เมื่อ ค่า count ถึงข้อมูลตัวสุดท้ายแล้วจะมีค่าเป็น -1 จึงหยุด while loop
                                            
                                            total += count; //เก็บค่า +count ทีละ 1024 ลง total เพื่อนำไปใช้เทียบกับขนาดแต่ละ thread ที่จะทำ

                                            doutClient.write(dataPatial, 0, count); //เขียนข้อมูลตั้งแต่จุดเริ่มต้น ยันค่า count ทีละ 1024
                                            if (total >= fileLength) {//ถ้า total มากกว่าหรือเท่ากับขนาดไฟล์ของแต่ละ thread จะปริ้น success
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