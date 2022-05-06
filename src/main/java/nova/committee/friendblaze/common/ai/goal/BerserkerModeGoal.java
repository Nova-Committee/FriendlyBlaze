package nova.committee.friendblaze.common.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import nova.committee.friendblaze.common.ai.StatusMode;
import nova.committee.friendblaze.common.entity.FriendBlazeEntity;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/4 18:46
 * Version: 1.0
 */
public class BerserkerModeGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final FriendBlazeEntity friendBlaze;

    public BerserkerModeGoal(FriendBlazeEntity blaze, Class<T> targetClassIn, boolean checkSight) {
        super(blaze, targetClassIn, checkSight, false);
        this.friendBlaze = blaze;
    }

    @Override
    public boolean canUse() {
        return this.friendBlaze.isMode(StatusMode.BERSERKER) && super.canUse();
    }
}
