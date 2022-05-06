package nova.committee.friendblaze.common.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import nova.committee.friendblaze.common.entity.FriendBlazeEntity;

import java.util.EnumSet;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/4 18:39
 * Version: 1.0
 */
public class BlazeAttackGoal extends Goal {
    private final FriendBlazeEntity blaze;
    private int attackStep;
    private int attackTime;
    private int lastSeen;

    public BlazeAttackGoal(FriendBlazeEntity p_32247_) {
        this.blaze = p_32247_;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.blaze.getTarget();
        return livingentity != null && livingentity.isAlive() && this.blaze.canAttack(livingentity);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.attackStep = 0;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    @Override
    public void stop() {
        this.blaze.setCharged(false);
        this.lastSeen = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    @Override
    public void tick() {
        --this.attackTime;
        LivingEntity livingentity = this.blaze.getTarget();
        if (livingentity != null) {
            boolean flag = this.blaze.getSensing().hasLineOfSight(livingentity);
            if (flag) {
                this.lastSeen = 0;
            } else {
                ++this.lastSeen;
            }

            double d0 = this.blaze.distanceToSqr(livingentity);
            if (d0 < 4.0D) {
                if (!flag) {
                    return;
                }

                if (this.attackTime <= 0) {
                    this.attackTime = 20;
                    this.blaze.doHurtTarget(livingentity);
                }

                this.blaze.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(), livingentity.getZ(), 1.0D);
            } else if (d0 < this.getFollowDistance() * this.getFollowDistance() && flag) {
                double d1 = livingentity.getX() - this.blaze.getX();
                double d2 = livingentity.getY(0.5D) - this.blaze.getY(0.5D);
                double d3 = livingentity.getZ() - this.blaze.getZ();
                float healthPre = livingentity.getHealth();
                if (this.attackTime <= 0) {
                    ++this.attackStep;
                    if (this.attackStep == 1) {
                        this.attackTime = 10;
                        this.blaze.setCharged(true);
                    } else if (this.attackStep <= 4) {
                        this.attackTime = 6;
                    } else {
                        this.attackTime = 50;
                        this.attackStep = 0;
                        this.blaze.setCharged(false);
                    }

                    if (this.attackStep > 1) {
                        if (!this.blaze.isSilent()) {
                            this.blaze.level.levelEvent(null, 1018, this.blaze.blockPosition(), 0);
                        }
                        if (this.blaze.getCurrentLevel() < 5) {
                            for (int i = 0; i < 1; ++i) {
                                SmallFireball smallfireball = new SmallFireball(this.blaze.level, this.blaze, d1, d2, d3);
                                smallfireball.setPos(smallfireball.getX(), this.blaze.getY(0.5D) + 0.5D, smallfireball.getZ());
                                this.blaze.level.addFreshEntity(smallfireball);
                            }
                        } else {
                            for (int i = 0; i < 1; ++i) {
                                LargeFireball largeFireball = new LargeFireball(this.blaze.level, this.blaze, d1, d2, d3, 2);
                                largeFireball.setPos(largeFireball.getX(), this.blaze.getY(0.5D) + 0.5D, largeFireball.getZ());
                                this.blaze.level.addFreshEntity(largeFireball);
                            }
                        }
                        this.blaze.onAttack(livingentity);

                    }
                }

                this.blaze.getLookControl().setLookAt(livingentity, 10.0F, 10.0F);
            } else if (this.lastSeen < 5) {
                this.blaze.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(), livingentity.getZ(), 1.0D);
            }

            super.tick();
        }
    }

    private double getFollowDistance() {
        return this.blaze.getAttributeValue(Attributes.FOLLOW_RANGE);
    }
}
