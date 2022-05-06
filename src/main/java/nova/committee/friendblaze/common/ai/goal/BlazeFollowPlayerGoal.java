package nova.committee.friendblaze.common.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import nova.committee.friendblaze.common.ai.StatusMode;
import nova.committee.friendblaze.common.entity.FriendBlazeEntity;

import java.util.EnumSet;
import java.util.Objects;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/4 19:03
 * Version: 1.0
 */
public class BlazeFollowPlayerGoal extends Goal {

    private final FriendBlazeEntity entity;
    private final double speed;
    private final PathNavigation navigation;
    private final double maxDistanceSquared;
    private final double minDistanceSquared;
    private LivingEntity owner;
    private int updateCountdownTicks;

    public BlazeFollowPlayerGoal(FriendBlazeEntity entity, double speed, float minDistance, float maxDistance) {
        this.entity = entity;
        this.speed = speed;
        this.navigation = entity.getNavigation();
        this.minDistanceSquared = (double) (minDistance * minDistance);
        this.maxDistanceSquared = (double) (maxDistance * maxDistance);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.entity.getOwner();
        if (livingEntity == null || livingEntity.isSpectator() || this.entity.getMode() == StatusMode.PATROL || !this.isWithinDistanceToStart(livingEntity)) {
            return false;
        } else {
            this.owner = livingEntity;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else {
            double distance = this.entity.distanceToSqr(this.owner);
            return distance > 7 && distance < getMinStartDistanceSq();
        }
    }

    public double getMinStartDistanceSq() {
        if (this.entity.isMode(StatusMode.GUARD)) {
            return 16d;
        }
        return this.maxDistanceSquared;
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
    }

    @Override

    public void stop() {
        this.owner = null;
        this.navigation.stop();
    }

    @Override
    public void tick() {
        this.entity.getLookControl().setLookAt(this.owner, 10.0F, (float) this.entity.getMaxHeadXRot());
        if (--this.updateCountdownTicks <= 0) {
            this.updateCountdownTicks = 10;
            if (!this.entity.isLeashed() && !this.entity.isPassenger()) {
                double distance = this.entity.distanceToSqr(this.owner);
                if (distance >= this.maxDistanceSquared * 1.15) {
                    this.tryToTeleportNearEntity();
                } else {
                    this.navigation.moveTo(this.owner, distance > (this.maxDistanceSquared * 10 / 100) ? this.speed : this.speed * 0.75);
                    Objects.requireNonNull(this.navigation.getPath()).setNextNodeIndex(1);
                }
            }
        }
    }

    private boolean isWithinDistanceToStart(LivingEntity owner) {
        return this.entity.distanceToSqr(owner) > this.minDistanceSquared;
    }

    private void tryToTeleportNearEntity() {
        BlockPos blockpos = this.owner.blockPosition();

        for (int i = 0; i < 10; ++i) {
            int j = this.getRandomNumber(-3, 3);
            int k = this.getRandomNumber(-1, 1);
            int l = this.getRandomNumber(-3, 3);
            boolean flag = this.tryToTeleportToLocation(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
            if (flag) {
                return;
            }
        }

    }

    private boolean tryToTeleportToLocation(int x, int y, int z) {
        if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.isTeleportFriendlyBlock(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.entity.moveTo((double) x + 0.5D, (double) y, (double) z + 0.5D, this.entity.getYRot(), this.entity.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean isTeleportFriendlyBlock(BlockPos pos) {
        if (this.entity.level.getBlockState(pos).isAir()) {
            BlockPos blockpos = pos.subtract(this.entity.blockPosition());
            return this.entity.level.noCollision(this.entity, this.entity.getBoundingBox().move(blockpos));
        }
        BlockPathTypes pathnodetype = WalkNodeEvaluator.getBlockPathTypeStatic(this.entity.level, pos.mutable());
        if (pathnodetype != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = this.entity.level.getBlockState(pos.below());
            if (blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = pos.subtract(this.entity.blockPosition());
                return this.entity.level.noCollision(this.entity, this.entity.getBoundingBox().move(blockpos));
            }
        }
    }

    private int getRandomNumber(int min, int max) {
        return this.entity.getRandom().nextInt(max - min + 1) + min;
    }


}
