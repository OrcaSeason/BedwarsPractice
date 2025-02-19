package io.dogsbean.bedwarspractice.game;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameSettings {
    private double damageAmount = 2.0;
    private boolean autoBreakWool = false;
    private long woolBreakDelay = 3000;
    private long woolBreakInterval = 500;
    public GameSettings clone() {
        GameSettings settings = new GameSettings();
        settings.setDamageAmount(this.damageAmount);
        settings.setAutoBreakWool(this.autoBreakWool);
        settings.setWoolBreakDelay(this.woolBreakDelay);
        settings.setWoolBreakInterval(this.woolBreakInterval);
        return settings;
    }
}