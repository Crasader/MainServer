package Game.Server;

import Util.GameConstants;
import Util.MyUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/*
不断地接受新客户端，每接受一个客户端新开一个线程处理与该客户端的通信。
 */

class ServerConnectThread extends Thread implements GameConstants {
    private Logger logger=Logger.getLogger("Game.Server.MainServer");
    private ServerSocket serverSocket;
    private MainServer server;
    private GameInfo gameInfo;

    void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }


    ServerConnectThread(String name, ServerSocket serverSocket, MainServer server) {
        super(name);
        this.server = server;
        this.serverSocket = serverSocket;
//        MyUtils.setLogger(logger);
    }

    @Override
    public void run() {
        while (true) {
            //等待连接
            logger.info("等待客户端连接");
            Socket socket = null;

            try {
                socket = serverSocket.accept();
                logger.info("client connected with socket:"+socket.getRemoteSocketAddress());
                socket.setTcpNoDelay(true);
            } catch (IOException e) {
                logger.severe("客户端连接时发送异常："+e.getMessage());
                logger.log(new LogRecord(Level.WARNING,"server exit"));
                System.exit(0);
            }


            String receiveMessage = null;
            try {
                logger.info("wait for name");
                socket.setSoTimeout(2000);//2s超时
                receiveMessage = MyUtils.ReceiveMessage(socket);
                socket.setSoTimeout(0);
                logger.info("rec:"+receiveMessage);
            } catch (IOException e) {
                logger.severe("接收名字信息异常："+e.getMessage());
                continue;
            }


            if(receiveMessage==null||receiveMessage.equals("")){
                logger.warning("接收名字信息为空！");
                continue;
            }
            //接收检查名字的消息
            //string 作为传递参数 是形参！！
            Scanner scanner;
            scanner = new Scanner(receiveMessage);
            receiveMessage = scanner.nextLine();//不接收换行@@


            String cm = MyUtils.DeMessage_cm(receiveMessage), userName = MyUtils.DeMessage_cont(receiveMessage);

            if (cm.equals(client_req_checkNameRepeat)) {
                boolean isRepeat=server.isNameRepeat(userName);
                logger.info("send check name result:"+isRepeat);
                if (isRepeat) {
                    //直接发送结果
                    try {
                        MyUtils.SendMessage(socket, TRUE);//名字重复
                        logger.warning(userName+"名字重复，启动失败");
                    } catch (IOException e) {
                        logger.severe("发送名字检测结果异常："+e.getMessage());
                    }
                } else {
                    try {
                        MyUtils.SendMessage(socket, FALSE);
                        logger.info(userName+"将正常启动");
                    } catch (IOException e) {
                        logger.severe("发送名字检测结果异常："+e.getMessage());
                        continue;
                    }
                    //要给 所有 client 更新游戏信息
                    server.addNewPlayer(new Player(userName, 0, player_not_inGame, socket));
                    // 客户端将启动，启动新线程  #接收# 与 该客户端的通信
                    ServerInputThread sit = new ServerInputThread(socket, gameInfo);
                    sit.setServer(server);
                    sit.start();
                    logger.info("server input thread start");

                }

            }else logger.warning("解析名字检查指令发生错误!");

        }
    }
}