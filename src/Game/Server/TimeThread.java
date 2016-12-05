package Game.Server;

import Util.GameConstants;

class TimeThread extends Thread implements GameConstants {
    private MainServer server;

    TimeThread(MainServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        int time=total_time;
        while(time>=0){
            server.sendTimeToAllGaming(time+"");
            try {
                sleep(998);
            } catch (InterruptedException e) {
                interrupt();
                return;
            }
            --time;
        }
        server.gameOver();
    }
}
