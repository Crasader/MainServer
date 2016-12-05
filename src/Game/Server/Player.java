package Game.Server;

import java.net.Socket;

public class Player {
    String name;
    private int score;
    String game_state;
    private Socket socket;

    Player(String name, int score, String game_state, Socket socket) {
        this.name = name;
        this.score = score;
        this.game_state = game_state;

        this.socket=socket;
    }

    String getName() {
        return name;
    }

    int getScore() {
        return score;
    }

    void setScore(int score) {
        this.score = score;
    }

    String getPlayer_state() {
        return game_state;
    }

    void setPlayer_state(String game_state) {
        this.game_state = game_state;
    }

    Socket getSocket() {
        return socket;
    }
}
