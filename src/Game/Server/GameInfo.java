package Game.Server;

import Util.GameConstants;

import java.util.ArrayList;

/*
只运行一个游戏

游戏信息：总玩家数、游戏数量（1），游戏状态、游戏的答案、玩家列表、正在游戏的玩家列表、游戏准备的人数、游戏解决的人数
 */
class GameInfo implements GameConstants{
//    int Count_playes;/*废弃，通过players 的size可以得到玩家人数*/
private String game_state;
    String game_answer;
    private ArrayList<Player> players=new ArrayList();
    //正在游戏中的玩家列表
    private ArrayList<Player> players_gaming=new ArrayList();
//    int Count_ready=0;
    int Count_resolve=0;

    GameInfo(String game_state) {
        this.game_state=game_state;
    }

    void setGame_answer(String game_answer) {
        this.game_answer = game_answer;
    }

    ArrayList<Player> getPlayers_gaming() {
        return players_gaming;
    }


    String getGame_state() {
        return game_state;
    }

    void setGame_state(String game_state) {
        this.game_state = game_state;
    }

    ArrayList<Player> getPlayers() {
        return players;
    }

    int  getPlayersCount() {
        return players.size();
    }
    int getCount_ready(){
        int ready=0;
        for(Player player:players){
            if(player.game_state.equals(player_ready))ready++;
        }
        return ready;

    }


}
