package Game.Server;

import Util.GameConstants;
import Util.MyUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

/*
该服务器的线程与客户端一 一对应，实现独立通信
该线程在游戏大厅线程建立后启动。
该线程能响应游戏大厅的点击事件，建立与游戏窗口的通信
 */
class ServerInputThread extends Thread implements GameConstants {
    private Logger logger = Logger.getLogger("Game.Server.MainServer");
    private Socket socket;
    private GameInfo gameInfo;
    private MainServer server;

    ServerInputThread(Socket socket, GameInfo gameInfo) {
        this.socket = socket;
        this.gameInfo = gameInfo;
//        MyUtils.setLogger(logger);
    }

    void setServer(MainServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
            String message;
            try {
                //debug
                logger.info("wait for input");
                message = MyUtils.ReceiveMessage(socket);
                Scanner scanner = new Scanner(message);
                while (scanner.hasNext()) {
                    cope(scanner.nextLine());
                }
            } catch (IOException e) {
                logger.warning("与客户端连接断开：" + e.getMessage());
                //连接异常时去除该玩家信息
                server.removePlayer(socket);
                return;//线程结束
            }
        }
    }

    private void cope(String message) {
        logger.info("coping :"+message);
        String cm = MyUtils.DeMessage_cm(message);//获取指令
        String cont = MyUtils.DeMessage_cont(message);//获取具体内容
        //处理收到的指令
        // （废弃,不接受主动的请求）
//        if (cm.equals(client_req_renew_hall)) {
//            try {
//                MyUtils.SendMessage(socket, MyUtils.BuildComm(ser_send_playerCount, gameInfo.getPlayersCount() + ""));
//                MyUtils.SendMessage(socket, MyUtils.BuildComm(ser_send_gameState, gameInfo.getGame_state()));
//            } catch (IOException e) {
//                logger.severe("发送更新大厅指令时发生异常："+e.getMessage());
//            }
//
//        } else
        if (cm.equals(client_btn_create)) {
            server.newGameCreated(socket);

        } else if (cm.equals(client_btn_join)) {
            server.newPlayerJoinGame(socket);

        } else if (cm.equals(client_btn_ready)) {
            server.btnReady(socket);

        } else if (cm.equals(client_btn_exit)) {
            server.btnExit(socket);

        } else if (cm.equals(client_btn_send_chat)) {
            server.sendChatToAllGaming(socket, cont);

        } else if (cm.equals(client_req_inc_score)) {
            server.incPlayerScore(socket, cont);
        }else if(cm.equals(client_over)){
            server.removePlayer(socket);
            interrupt();//该线程结束
        }
    }
}
