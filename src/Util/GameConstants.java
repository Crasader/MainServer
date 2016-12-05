package Util;

public interface GameConstants {

    //分隔符
    String SEP="#";



    //服务器发送玩家人数
    String ser_send_playerCount ="服务器发送玩家人数";

    //服务器发送游戏状态
    String ser_send_gameState ="服务器发送游戏状态";
    /*
    当全部人都准备了，游戏开始--》发送字符---》倒计时开启
     */
    //服务器发送4个数字字符  ***#1 2  3 4
    String ser_send_4num ="ser4num";
    //服务器命令更新table数据
    String ser_table_remv_player="去除玩家";//**#username
    String ser_table_incr_player="增加玩家";//**#username score state
    String ser_table_inc_player_score ="修改玩家分数"; //**#username  score
    String ser_table_set_player_state="修改玩家游戏状态"; //**#username  state
    //服务器发送聊天数据
    String ser_send_chat="聊天数据";//**#username instence(end)

    //服务器（不断）发送 倒计时的数字文本
    String ser_send_time="倒计时数据";//**#60

    /*
    处理倒计时事件
    1.都提前解出答案---》显示一个可能的解法--》下一轮的开始
    2.倒计时结束----》显示一个可能的解法---》下一轮的开始
    可以用一个结束信号表示
     */
    String ser_send_game_end ="当前游戏结束";



    /*
    状态常量
     */
    //玩家的游戏状态

    String player_not_inGame ="未进入游戏";//玩家依旧在游戏大厅
    String player_not_ready="未准备";
    String player_ready="已准备";
    String player_gaming="游戏中";

    //当前游戏状态

    String game_not_created="游戏未创建";
    String game_created_and_can_join ="游戏已创建，可加入";
    String game_started="游戏已开始，不可加入";

    //逻辑的常量
    String TRUE="true",FALSE="false";


    /*************************
    * 客户端发送给服务器的指令
    * */
    //客户命令检查是否用户重复
    String client_req_checkNameRepeat ="检查用户名";

    //客户端请求更新游戏大厅
    String client_req_renew_hall="客户端请求更新游戏大厅";

    //客户端的按钮事件
    /*
    客户发送创建新游戏的请求--》服务器同意并更新游戏信息---》服务器反馈ok-->客户端更新table
     */
    String client_btn_create ="玩家创建新游戏";
    /*
    客户端发送加入游戏请求--->服务器命令更新table
     */
    String client_btn_join ="玩家加入新游戏";

    //c-->s 服务器命令更新table;
    String client_btn_ready ="玩家准备了";
    //更新table，用户回退到大厅界面
    String client_btn_exit ="玩家退出游戏大厅";
    //服务器接收聊天数据，并传达给所有客户
    String client_btn_send_chat ="玩家发送聊天";

    //成功解出答案，请求服务器增加分数
    String client_req_inc_score="增加分数";

    String client_over="玩家退出本游戏";


    //计算相关
    int div_error=999;
    int no_use_number_given=1000;
    int op_error=1001;
    int overuse=1002;
    int lackuse=1003;
    String add="+";
    String sub="-";
    String mul="*";
    String div="/";
    String left="(";
    String right=")";
    int total_time=90;


}
