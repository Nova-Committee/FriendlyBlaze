package nova.committee.friendblaze.common.ai;

import nova.committee.friendblaze.common.entity.FriendBlazeEntity;

import java.util.Arrays;
import java.util.Comparator;

public enum StatusMode {

    DOCILE(0, "docile"),
    WANDERING(1, "wandering"),
    AGGRESIVE(2, "aggressive"),
    BERSERKER(3, "berserker"),
    TACTICAL(4, "tactical"),
    PATROL(5, "patrol"),
    GUARD(6, "guard");


    public static final StatusMode[] VALUES = Arrays.stream(StatusMode.values()).sorted(Comparator.comparingInt(StatusMode::getIndex)).toArray(StatusMode[]::new);
    private int index;
    private String saveName;
    private String unlocalisedTip;
    private String unlocalisedName;
    private String unlocalisedInfo;

    private StatusMode(int index, String name) {
        this(index, name, "blaze.mode." + name, "blaze.mode." + name + ".indicator", "blaze.mode." + name + ".description");
    }

    private StatusMode(int index, String mode, String unlocalisedName, String tip, String info) {
        this.index = index;
        this.saveName = mode;
        this.unlocalisedName = unlocalisedName;
        this.unlocalisedTip = tip;
        this.unlocalisedInfo = info;
    }

    public static StatusMode byIndex(int i) {
        if (i < 0 || i >= VALUES.length) {
            i = StatusMode.DOCILE.getIndex();
        }
        return VALUES[i];
    }

    public static StatusMode bySaveName(String saveName) {
        for (StatusMode gender : StatusMode.values()) {
            if (gender.saveName.equals(saveName)) {
                return gender;
            }
        }

        return DOCILE;
    }

    public int getIndex() {
        return this.index;
    }

    public String getSaveName() {
        return this.saveName;
    }

    public String getTip() {
        return this.unlocalisedTip;
    }

    public String getUnlocalisedName() {
        return this.unlocalisedName;
    }

    public String getUnlocalisedInfo() {
        return this.unlocalisedInfo;
    }

    public void onModeSet(FriendBlazeEntity dog, StatusMode prev) {
        switch (prev) {
            default -> {
                dog.getNavigation().stop();
                dog.setTarget(null);
                dog.setLastHurtByMob(null);
            }
        }
    }

    public StatusMode previousMode() {
        int i = this.getIndex() - 1;
        if (i < 0) {
            i = VALUES.length - 1;
        }
        return VALUES[i];
    }

    public StatusMode nextMode() {
        int i = this.getIndex() + 1;
        if (i >= VALUES.length) {
            i = 0;
        }
        return VALUES[i];
    }
}
