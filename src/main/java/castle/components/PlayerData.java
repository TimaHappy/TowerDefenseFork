package castle.components;

import arc.util.Interval;
import mindustry.gen.Player;

public class PlayerData {

    public Player player;
    public Interval interval; // У каждого игрока свой интервал

    public int money;
    public int income;

    public boolean showHud;

    public PlayerData(Player player) {
        this.player = player;
        this.interval = new Interval(2);
        this.money = 0;
        this.income = 15;
        this.showHud = true;
    }

    public void update() {
        if (interval.get(0, 60f)) {
            money += income;
        }

        if (interval.get(1, 180f)) {
            updateLabels();
        }


    }

    public  void updateLabels() {

    }

    public void reset() {
        for (int i = 0; i < interval.getTimes().length; i++) interval.reset(i, 0f);

        money = 0;
        income = 15;
    }
}
