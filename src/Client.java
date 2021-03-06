import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.table.*;

public class Client {
    Socket socketClient;
    DataInputStream din;
    DataOutputStream dout;
    final int PORT = 8080;
    int FileLength;
    Object[][] fileList;
    String file;
    JButton downloadButton = new JButton();
    JPanel panelFileList;
    int total = 0;

    public static void main(String[] args) throws IOException {
        new Client().run();
    }
//ส่วนของการร้องขอเข้าเชื่อมต่อ
    public Client() {
        try {
            socketClient = new Socket("localhost", PORT);
            System.out.println("Connecing...");
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
//รับไฟล์ทั้งหมดของ server
    public void reciveAllFile() {
        JFrame frameReciveAllFile = new JFrame();
        frameReciveAllFile.setTitle("DOWNLOADER");
        frameReciveAllFile.setSize(780, 570);
        frameReciveAllFile.setResizable(false);
        frameReciveAllFile.setFont(new Font("TH-Sarabun-PSK", Font.BOLD, 13));
        frameReciveAllFile.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameReciveAllFile.setLocationRelativeTo(null); // ปรับให้ frame อยู่กลางจอ
        frameReciveAllFile.setVisible(true);

        try {
            din = new DataInputStream(socketClient.getInputStream());
            dout = new DataOutputStream(socketClient.getOutputStream());
            FileLength = din.readInt(); //อ่านค่าไฟล์ทั้งหมดที่ server ต้องทำการส่ง
            fileList = new Object[FileLength][4];

            String[] colHeaderFileList = { "All File", "File Type", "Size", "Action" };
            String[][] rowfileList = new String[FileLength][4];
            for (int i = 0; i < FileLength; i++) {
                fileList[i][0] = din.readUTF(); // อ่านชื่อไฟล์
            }
            for (int i = 0; i < FileLength; i++) {
                fileList[i][1] = din.readUTF(); // อ่านชนิดข้อมูลไฟล์
            }
            for (int i = 0; i < FileLength; i++) {
                fileList[i][2] = din.readUTF(); // อ่านขนาดไฟล์
            }
            for (int i = 0; i < FileLength; i++) {
                rowfileList[i][0] = "  "
                        + fileList[i][0].toString().substring(0, fileList[i][0].toString().lastIndexOf("."));
                rowfileList[i][1] = fileList[i][1].toString().substring(fileList[i][1].toString().indexOf("/") + 1,
                        fileList[i][1].toString().length());
                rowfileList[i][2] = fileList[i][2].toString() + " KB ";
                rowfileList[i][3] = fileList[i][0].toString();
            }

            TableCellRenderer tableRenderer;
            DefaultTableModel model = new DefaultTableModel();
            model.setDataVector(rowfileList, colHeaderFileList);
            JTable tableFileList = new JTable(model);
            tableFileList.getColumn("Action").setCellRenderer(new ButtonRenderer(rowfileList));
            tableFileList.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
            panelFileList = new JPanel();
            tableFileList.setFont(new Font("TH-Sarabun-PSK", Font.BOLD, 13));
            tableFileList.setRowHeight(40);
            tableFileList.setPreferredScrollableViewportSize(new Dimension(750, 500)); // ปรับขนาดตาราง
            tableFileList.getColumnModel().getColumn(0).setPreferredWidth(400); // ปรับขนาดคอลัม

            // ปรับข้อความชิดซ้ายชิดขวา
            DefaultTableCellRenderer d = new DefaultTableCellRenderer();
            DefaultTableCellRenderer d2 = new DefaultTableCellRenderer();
            d.setHorizontalAlignment(JLabel.CENTER);
            tableFileList.getColumnModel().getColumn(1).setCellRenderer(d);
            d2.setHorizontalAlignment(JLabel.RIGHT);
            tableFileList.getColumnModel().getColumn(2).setCellRenderer(d2);

            JScrollPane scrollPaneFileList = new JScrollPane(tableFileList);
            panelFileList.add(scrollPaneFileList);
            downloadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int confirm = JOptionPane.showConfirmDialog(
                            panelFileList,
                            "Do you want to download " + downloadButton.getName() + " ?"
                            , "Customized Dialog"
                            ,JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE
                            ,new ImageIcon("C:/Users/tubti/OneDrive - Silpakorn University/Documents/Thread/meaow2.png")
                            );
                    if (confirm == 0) { //หากกด confirm ที่ dialog จะไปทำงานที่ method reqFile();
                        reqFile();
                    }
                }

            });

            frameReciveAllFile.add(panelFileList);

        } catch (Exception e) {
            System.out.println(e);
        }

    }
    //ส่งไฟล์ที่ต้องการหา server
    public void reqFile() {

        try {
            dout.writeUTF(downloadButton.getName().toString());//เขียนชื่อไฟล์ที่ต้องการ
            reciveReqrFile();
        } catch (Exception e) {

        }

    }

    public void reciveReqrFile() throws IOException {
        int size = din.readInt();
        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setBounds(50 , 25, 200 ,150);

        JFrame downloadFrame = new JFrame("DOWNLOADING");
        downloadFrame.setSize(300, 100);
        downloadFrame.setResizable(false);
        downloadFrame.setFont(new Font("TH-Sarabun-PSK", Font.BOLD, 13));
        downloadFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        downloadFrame.setLocationRelativeTo(null); // ปรับให้ frame อยู่กลางจอ
        downloadFrame.add(progressBar);
        downloadFrame.setVisible(true);
        
        total = 0;

        //Thread progress bar
        new Thread(() -> {
            boolean success = false;
            while(!success) {
                try {
                    Thread.sleep(100);
                    if(total < size){
                        progressBar.setValue((int)((total*100.0) / size));//สูตรคิด %
                    } else {
                        success = true;
                        progressBar.setValue(100);
                        progressBar.setVisible(false);
                        downloadFrame.setVisible(false);
                        JOptionPane.showConfirmDialog(
                            downloadFrame, "Download complete", "Status",
                        JOptionPane.CLOSED_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon("C:/Users/tubti/OneDrive - Silpakorn University/Documents/Thread/meaow.png")
                        );
                    }
                } catch (Exception e) {
                    
                } 
            }
        }).start();

        //Thread การรับข้อมูล
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    System.out.println(Thread.currentThread().getName());
                    Socket socket = new Socket("localhost", 8087);
                    DataInputStream dinClient = new DataInputStream(socket.getInputStream());

                    String filePath = "C:/Users/api_q/OneDrive/เดสก์ท็อป/OSProject/FileClient/" + downloadButton.getName();

                    int startIndex = dinClient.readInt();
                    int fileLength = dinClient.readInt();
                    RandomAccessFile writer = new RandomAccessFile(filePath, "rw");//ตัวที่กำหนดให้สามารถเขียนและอ่านพร้อมกันได้
                    writer.seek(startIndex);//seek เหมือน skip
                    byte[] data = new byte[1024];
                    int receive = 0;
                    while (receive > -1) {//ถ้า receive อ่านข้อมูลชุดสุดท้ายของ thread นั้นๆแล้วจะไม่อ่านอีกต่อไปและมีค่าเป็น -1

                        receive = dinClient.read(data);
                        if (receive == -1) {
                            break;
                        }
                        writer.write(data, 0, receive);//อ่านข้อมูลตั้งแต่เริ่มต้น thread จนถึงชุดสุดท้ายของ thread ครั้งละ data ซึ่งค่าปกติที่อ่านคือ 1024 แต่อาจจะอ่านไม่ถึงก็ได้
                        updateDownload(receive);
                    }
                    System.out.println("finish");
                    // File fileDownload = new File(filePath);
                    // FileOutputStream fout = new FileOutputStream(fileDownload);
                    dinClient.close();
                    writer.close();
                    socket.close();
                    System.out.println("Recive");
                } catch (Exception e) {
                    // System.out.println("Can't Recive");
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private synchronized void updateDownload(int read) { //ทำหน้าที่ block ไม่ให้เกิด transection เพื่อไม่ให้ update ค่าผิดพลาด
        total += read;
    }

    public void run() throws IOException {
        reciveAllFile();
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer(String[][] dataValues) {

            setOpaque(true);

        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {

            setText("Download");
            setName(value.toString());

            return this;

        }

    }

    class ButtonEditor extends DefaultCellEditor {

        private String label;

        public ButtonEditor(JCheckBox checkBox) {

            super(checkBox);

        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {

            label = "Download";

            downloadButton.setText("Download");
            downloadButton.setName(value.toString());
            return downloadButton;

        }

        public Object getCellEditorValue() {

            return new String(label);

        }

    }
}