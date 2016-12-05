package Util;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.*;

public final class MyUtils implements GameConstants {
    private static FileHandler fileHandler;

    //配置日志文件
    static {
        try {
            File logPath=new File("javalog");
            if(!logPath.exists())logPath.mkdir();
            fileHandler = new FileHandler(logPath.getPath()+"/game%u.log",true);
        } catch (IOException e) {
            String mes=e.getMessage();
            JOptionPane.showMessageDialog(null,"无法创建日志文件:"+mes,"error",JOptionPane.ERROR_MESSAGE);
        }
    }
    private static Formatter formatter=new SimpleFormatter();

    public static Dimension GetParentSize(){
        Toolkit toolkit=Toolkit.getDefaultToolkit();
        return toolkit.getScreenSize();
    }
    public static void  setLogger(Logger logger)  {
        try {
            fileHandler.setEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            JOptionPane.showMessageDialog(null,"日志不支持utf-8编码"+e.getMessage());
        }
        fileHandler.setFormatter(formatter);
        logger.addHandler(fileHandler);
    }
    public static void SendMessage(Socket socket, String messge) throws IOException {
//         OutputStream os=socket.getOutputStream();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
        pw.println(messge);
        pw.flush();
    }

    public static String ReceiveMessage(Socket socket) throws IOException {

        BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
        char[] chars=new char[512];
        int len=br.read(chars,0,512);
        return new String(chars,0,len);


    }

    public static String BuildComm(String cm, String content) {
        return cm + SEP + content;
    }

    public static String DeMessage_cm(String message) {
        int index = message.indexOf(SEP);
        //仅仅是指令或内容，没有分隔符
        if(index==-1)return message;
        return message.substring(0, index);
    }

    public static String DeMessage_cont(String message) {
        int index = message.indexOf(SEP);

        if(index==-1)return message;
        return message.substring(index + 1);
    }
    public static ArrayList<String> DeMessge_strings(String message){
        Scanner scanner=new Scanner(message);
        ArrayList<String> strings=new ArrayList();
        while (scanner.hasNext()){
            strings.add(scanner.next());
        }
        return strings;
    }

}
