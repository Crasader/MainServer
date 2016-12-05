package Game.Server;

import Util.Cal24Points;
import Util.GameConstants;
import Util.MyUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;

/*
服务器总线程，处理与所有客户端的通信
 */
public class MainServer implements GameConstants {
    private Logger logger = Logger.getLogger("Game.Server.MainServer");
    private GameInfo gameInfo;
    private ArrayList<Player> players;
    private ArrayList<Player> players_gaming;
    private TimeThread tt;
    private ServerSocket serverSocket;
    private String ip;
    private int port;

    private MainServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
        MyUtils.setLogger(logger);
    }


    private void startServer() {

        try {
            //建立特定端口的服务器
//            ServerSocket serverSocket = new ServerSocket(port);

            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(ip), port));
            logger.info("端口开启成功");
        } catch (IOException e1) {
            logger.warning("绑定端口失败："+e1.getMessage());
            return;
        }
        //服务器启动成功
                    /*在这里初始化游戏信息*/
        gameInfo = new GameInfo(game_not_created);
        players = gameInfo.getPlayers();
        //设置正在游戏中的玩家对象
        players_gaming = gameInfo.getPlayers_gaming();
        //新线程监控客户端的连接
        ServerConnectThread sct = new ServerConnectThread("服务器连接线程", serverSocket, MainServer.this);
        sct.setGameInfo(gameInfo);
        sct.start();
    }

    boolean isNameRepeat(String username) {
        for (Player player : players) {
            if (player.name.equals(username)) {
                return true;
            }
        }
        return false;
    }

    void addNewPlayer(Player player) {
        players.add(player);
        logger.info("add player .player count："+players.size());
        //更新大厅
        sendPlayerCountToAll(players.size());
        sendGameStateToAll(gameInfo.getGame_state());
    }

    private Player getPlayerBySocket(Socket socket) {
        for (Player player : players) {
            if (player.getSocket() == socket) return player;
        }
        return null;
    }

    /*新的游戏窗口被建立，服务器获取用户，更新该用户信息（包括游戏窗口的table显示）
    * 更新游戏总信息（仅是游戏状态），发送给所有用户*/
    void newGameCreated(Socket playerSocket) {

        Player player = getPlayerBySocket(playerSocket);

        if (player != null) {
            player.setPlayer_state(player_not_ready);
            logger.info("new game is created");
        }else {
            logger.warning("玩家不存在");
            return;
        }

        //增加正在游戏中的玩家
        players_gaming.add(player);
//        gameInfo.setCount_games(++curGameCount);
        gameInfo.setGame_state(game_created_and_can_join);

        sendGameStateToAll(game_created_and_can_join);
        //更新一个socket对应#游戏中#用户的信息
        TableAddNewPlayer(playerSocket, player);
    }


    //新玩家加入游戏，先给该玩家table添加所有已存在的玩家信息，在给所有添加新用户
    void newPlayerJoinGame(Socket socket) {
        //添加已有玩家信息
        for (Player player : players_gaming) {
            //增加已存在的玩家
            TableAddNewPlayer(socket, player);//for循环更改player
        }
        //更新用户为游戏用户
        Player player = getPlayerBySocket(socket);
        if(player==null)return;
        player.setPlayer_state(player_not_ready);
        players_gaming.add(player);


        //增加新玩家player
        for (Player p : players_gaming) {//for循环更改socket
            Socket pSocket = p.getSocket();
            TableAddNewPlayer(pSocket, player);
        }

    }

    //发送游戏状态给所有客户端
    private void sendGameStateToAll(String gameState) {
        String message = MyUtils.BuildComm(ser_send_gameState, gameState);
        for (Player player : players) {
            Socket socket = player.getSocket();
            try {
                MyUtils.SendMessage(socket, message);
            } catch (IOException e) {
                logger.severe("发送游戏状态给客户端时发生异常："+e.getMessage());

            }
        }
    }


    void btnReady(Socket playerSocket) {
        //更新所有玩家table的状态
        Player player = getPlayerBySocket(playerSocket);
        if(player==null)return;
//        gameInfo.Count_ready++;
        player.game_state = player_ready;
        TableUpdatePlayerState(player);

        if (gameInfo.getCount_ready() == players_gaming.size()) {
            logger.info("all is ready");
            //所有游戏玩家都已准备，游戏开始，并且不准新玩家加入
            sendGameStateToAll(game_started);
            //生成四个数，（检测可解性），发送数据(给所有游戏中的玩家)，开始计时
            int num1, num2, num3, num4;
            //生成4个有解的数
            while (true) {
                num1 = genRandomNum();
                num2 = genRandomNum();
                num3 = genRandomNum();
                num4 = genRandomNum();
                String gen4Nums = Cal24Points.isCanWorkOut24(num1, num2, num3, num4);

                if (!gen4Nums.equals(FALSE)) {
                    gameInfo.setGame_answer(gen4Nums);
                    break;
                }
            }
            send4numToAllGaming(num1 + "", num2 + "", num3 + "", num4 + "");
            logger.info("game will start，the answer is:"+gameInfo.game_answer);
            tt = new TimeThread(MainServer.this);
            tt.start();
        }
    }

    //玩家退出游戏窗口，更新table，gameinfo
    //客户端会删除玩家的table信息
    void btnExit(Socket socket) {
        //先对playergaming 去除该玩家，然后在playgaming遍历玩家更新table
        Player removePlayer = getPlayerBySocket(socket);
        //更新游戏信息
        if(removePlayer==null)return;
        //更新玩家状态：没有在游戏中
        removePlayer.setPlayer_state(player_not_inGame);
        players_gaming.remove(removePlayer);
//        //更新游戏准备人数
//        if(removePlayer.game_state.equals(player_ready))
//        gameInfo.Count_ready--;

        //更新table
        String userName = removePlayer.getName();
        for (Player p : players_gaming) {
            Socket pSocket = p.getSocket();
            try {
                MyUtils.SendMessage(pSocket, MyUtils.BuildComm(ser_table_remv_player, userName));
            } catch (IOException e) {
                logger.severe("发送table信息时发生异常："+e.getMessage());

            }
        }


    }

    //一轮游戏结束后调用
    void gameOver() {
        /*发送表达式的解答结果
        更新游戏状态（存在但未开始），用户状态（未准备）
         */
        //通知游戏结束，发送答案
        String message = MyUtils.BuildComm(ser_send_game_end, gameInfo.game_answer);
        for (Player player : players_gaming) {
            Socket socket = player.getSocket();
            try {
                MyUtils.SendMessage(socket, message);
            } catch (IOException e) {
                logger.severe("通知游戏结束时发生异常："+e.getMessage());
            }
        }

        //设置游戏大厅的加入按钮可用
        sendGameStateToAll(game_created_and_can_join);

        //更新游戏信息
        resetGameInfo();

    }

    void incPlayerScore(Socket socket, String time) {
        /*
        * 检查是否所有玩家都解出了答案，是则更新游戏*/
        //玩家请求增加分数，更新所有玩家的table表
        gameInfo.Count_resolve++;

        Player incScoreplayer = getPlayerBySocket(socket);//增加分数的玩家
        String incScore = calculatePlayerScore(Integer.parseInt(time));//当轮分数
        int orginScore = 0;//历史分数
        if (incScoreplayer != null) {
            orginScore = incScoreplayer.getScore();
        }
        int newScore = Integer.parseInt(incScore) + orginScore;
        if (incScoreplayer != null) {
            incScoreplayer.setScore(newScore);
        }

        //更新所有玩家的该增加分数的玩家信息
        String userName = null;
        if (incScoreplayer != null) {
            userName = incScoreplayer.getName();
        }
        String userNameAndScore = userName + " " + newScore;
        String chatMessage = MyUtils.BuildComm(ser_send_chat, "系统消息：玩家" + userName + "在倒计时" + time + "s 时" + "解出了24！");
        for (Player player : players_gaming) {
            //对每一位玩家的table上的某一个玩家设置增加分数
            Socket socket1 = player.getSocket();
            setTablePlayerScore(socket1, userNameAndScore);

            //发送解出题目的消息
            try {
                MyUtils.SendMessage(socket1, chatMessage);
            } catch (IOException e) {
                logger.severe("发送系统消息时发生异常："+e.getMessage());

            }

        }
        if (gameInfo.Count_resolve == players_gaming.size()) {
            //当轮游戏结束，更新倒计时线程，更新游戏窗口面板
            renewTimeThread();
            //更新gameinfo，重置准备人数，解答人数，玩家的游戏状态
            gameOver();
        }
    }

    //一轮游戏结束后重置游戏信息
    private void resetGameInfo() {
//        gameInfo.Count_ready = 0;//准备人数
        gameInfo.Count_resolve = 0;//解决问题的人数
        gameInfo.setGame_answer(null);
        for (Player player : players_gaming) {
            player.game_state = player_not_ready;//更新每个玩家的状态
        }
    }

    private void renewTimeThread() {
        tt.interrupt();
    }

    private String calculatePlayerScore(int time) {
        //百分制
        if(time>=75)return 100+"";

        int score = time+25;
        return score + "";
    }


    //给特定流的玩家更新某玩家的分数
    private void setTablePlayerScore(Socket socket, String playerNameAndScore) {

        try {
            MyUtils.SendMessage(socket, MyUtils.BuildComm(ser_table_inc_player_score, playerNameAndScore));
        } catch (IOException e) {
            logger.severe("发送玩家分数时发生异常："+e.getMessage());

        }
    }

    //对特定socket，给table增加一个新player#行#
    //可以在有玩家加入到游戏时在for语句中调用
    private void TableAddNewPlayer(Socket socket, Player player) {
        try {
            MyUtils.SendMessage(socket, MyUtils.BuildComm(ser_table_incr_player, player.getName() + " " + player.getScore() + " " +
                    player.getPlayer_state()));
        } catch (IOException e) {
            logger.severe("发送table的玩家信息时发生异常："+e.getMessage());

        }
    }


    //更新所有玩家的table上某用户的状态
    private void TableUpdatePlayerState(Player player) {
        String nameAndState = player.getName() + " " + player.getPlayer_state();
        try {
            for (Player p : players_gaming) {
                Socket socket = p.getSocket();
                MyUtils.SendMessage(socket, MyUtils.BuildComm(ser_table_set_player_state, nameAndState));
            }
        } catch (IOException e) {
            logger.severe("发送玩家状态时发生异常："+e.getMessage());

        }

    }


    private void send4numToAllGaming(String num1, String num2, String num3, String num4) {
        for (Player player : players_gaming) {
            Socket socket = player.getSocket();
            try {
                MyUtils.SendMessage(socket, MyUtils.BuildComm(
                        GameConstants.ser_send_4num, num1 + " " + num2 + " " + num3 + " " + num4));
            } catch (IOException e) {
                logger.severe("发送4个数时发生异常："+e.getMessage());

            }
        }
    }

    void sendTimeToAllGaming(String time) {
        for (Player player : players_gaming) {
            Socket socket = player.getSocket();
            try {
                MyUtils.SendMessage(socket, MyUtils.BuildComm(ser_send_time, time));
            } catch (IOException e) {
                logger.severe("发送时间信息时发生异常："+e.getMessage());

            }
        }
    }

    private int genRandomNum() {
        while (true) {
            int num = (int) (Math.random() * 14);
            if (num != 0) return num;
        }
    }


    void sendChatToAllGaming(Socket socket, String content) {
        Player player = getPlayerBySocket(socket);
        //整合名字的对话消息
        String message = null;
        if (player != null) {
            message = player.getName() + ":  " + content;
        }
        for (Player p : players_gaming) {
            Socket pSocket = p.getSocket();
            try {
                MyUtils.SendMessage(pSocket, MyUtils.BuildComm(ser_send_chat, message));
            } catch (IOException e) {
                logger.severe("发送聊天信息时发生异常："+e.getMessage());

            }
        }
    }


    //玩家退出游戏后，客户端自行处理关闭，服务器更新信息
    //服务器的对该socket的监听自行中断
    void removePlayer(Socket socket) {
        Player player_remv = getPlayerBySocket(socket);
        if(player_remv==null)return;

        players.remove(player_remv);

        //确保从游戏中的玩家列表中移除该玩家
        if (players_gaming.remove(player_remv)) {

            logger.info("成功移除玩家:"+player_remv.getName());
        }
        logger.info("当前玩家数 :"+players.size());
        logger.info("当前游戏中玩家数:"+players_gaming.size());

//        gameInfo.Count_playes--;

        //没有玩家 ，更新游戏状态
        int playersCount = players.size();
        //发送游戏人数
        logger.info("set hall players count:"+playersCount);
        sendPlayerCountToAll(playersCount);
        if (playersCount == 0) {
            //重置操作
            if(players_gaming.size()!=0){
                logger.warning("用户数为0后，游戏人数仍不为0，将执行重置操作");
                gameInfo = new GameInfo(game_not_created);
                players = gameInfo.getPlayers();
                players_gaming=gameInfo.getPlayers_gaming();
            }else {
                logger.info("set gamestate not create");
                gameInfo.setGame_state(game_not_created);//游戏未开始
            }

        }




    }

    private void sendPlayerCountToAll(int count_playes) {
        String mes = MyUtils.BuildComm(ser_send_playerCount, count_playes + "");
        for (Player playe : players) {
            Socket socket = playe.getSocket();
            try {
                MyUtils.SendMessage(socket, mes);
            } catch (IOException e) {
                logger.severe("发送玩家人数时发生异常："+e.getMessage());

            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String ip;
        int port;
//        ip = "192.168.199.222";
//        ip = "10.104.68.136";
//        ip = "10.135.128.148";
//        port = 5000;
        System.out.println("input ip adress:");
        ip = scanner.next();
        System.out.println("input port");
        port = scanner.nextInt();
        MainServer mainServer = new MainServer(ip, port);
        mainServer.startServer();
//        10.135.43.177

    }

}
