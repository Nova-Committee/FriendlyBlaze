package nova.committee.friendblaze.common.entity;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/5 12:16
 * Version: 1.0
 */
public class BlazeLevels {
    public static final BlazeLevel[] LEVELS;

    static {
        LEVELS = new BlazeLevel[20];
        LEVELS[0] = new BlazeLevel(0, 0F, 0F, 0F);
        LEVELS[1] = new BlazeLevel(50, 1F, 0F, 0F);            // Health
        LEVELS[2] = new BlazeLevel(150, 2F, 0F, 0F);        // Health
        LEVELS[3] = new BlazeLevel(300, 2F, 0F, 0.03F);        // Speed
        LEVELS[4] = new BlazeLevel(500, 2F, 1F, 0.03F);        // Attack
        LEVELS[5] = new BlazeLevel(750, 3F, 1F, 0.03F);        // Health
        LEVELS[6] = new BlazeLevel(1050, 4F, 1F, 0.03F);    // Health
        LEVELS[7] = new BlazeLevel(1400, 4F, 1F, 0.06F);    // Speed
        LEVELS[8] = new BlazeLevel(1800, 4F, 2F, 0.06F);    // Attack
        LEVELS[9] = new BlazeLevel(2250, 5F, 2F, 0.06F);        // Health
        LEVELS[10] = new BlazeLevel(2750, 6F, 2F, 0.06F);    // Health
        LEVELS[11] = new BlazeLevel(3300, 6F, 2F, 0.09F);    // Speed
        LEVELS[12] = new BlazeLevel(3900, 6F, 3F, 0.09F);    // Attack
        LEVELS[13] = new BlazeLevel(4550, 7F, 3F, 0.09F);    // Health
        LEVELS[14] = new BlazeLevel(5250, 8F, 3F, 0.09F);    // Health
        LEVELS[15] = new BlazeLevel(6000, 8F, 3F, 0.12F);    // Speed
        LEVELS[16] = new BlazeLevel(6800, 9F, 3F, 0.12F);    // Health
        LEVELS[17] = new BlazeLevel(7650, 10F, 3F, 0.12F);    // Health
        LEVELS[18] = new BlazeLevel(8550, 10F, 3F, 0.15F);    // Speed
        LEVELS[19] = new BlazeLevel(9550, 10F, 4F, 0.15F);    // Attack

    }

    public static float getLevelHealth(int level) {
        return LEVELS[level].healthBonus;
    }

    public static float getLevelAttack(int level) {
        return LEVELS[level].attackBonus;
    }

    public static float getLevelSpeed(int level) {
        return LEVELS[level].speedBonus;
    }

    public static int getLevelByExp(int exp) {
        for (int i = BlazeLevels.LEVELS.length - 1; i >= 0; i--) {
            if (BlazeLevels.LEVELS[i].totalExpNeeded <= exp) {
                return i;
            }
        }
        return BlazeLevels.LEVELS.length - 1;
    }

    public static class BlazeLevel {
        public int totalExpNeeded;
        public float healthBonus;
        public float attackBonus;
        public float speedBonus;

        BlazeLevel(int totalExpNeeded, float healthBonus, float attackBonus, float speedBonus) {
            this.totalExpNeeded = totalExpNeeded;
            this.healthBonus = healthBonus;
            this.attackBonus = attackBonus;
            this.speedBonus = speedBonus;
        }
    }
}
