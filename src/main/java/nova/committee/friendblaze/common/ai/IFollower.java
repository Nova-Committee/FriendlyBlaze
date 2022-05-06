package nova.committee.friendblaze.common.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/4 19:13
 * Version: 1.0
 */
public interface IFollower {
    boolean shouldFollow();

    default void followEntity(TamableAnimal tameable, LivingEntity owner, double followSpeed) {
        tameable.getNavigation().moveTo(owner, followSpeed);
    }
}
