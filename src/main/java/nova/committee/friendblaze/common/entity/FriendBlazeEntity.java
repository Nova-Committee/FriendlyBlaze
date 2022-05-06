package nova.committee.friendblaze.common.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import nova.committee.friendblaze.common.ai.StatusMode;
import nova.committee.friendblaze.common.ai.goal.BerserkerModeGoal;
import nova.committee.friendblaze.common.ai.goal.BlazeAttackGoal;
import nova.committee.friendblaze.common.ai.goal.BlazeFollowPlayerGoal;
import nova.committee.friendblaze.init.registry.ModEntities;
import nova.committee.friendblaze.init.registry.ModItems;
import nova.committee.friendblaze.init.registry.ModSerializers;
import nova.committee.friendblaze.util.Cache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/4 18:30
 * Version: 1.0
 */
public class FriendBlazeEntity extends TamableAnimal {
    public static final float BASE_HEALTH = 30.0F;
    public static final float BASE_ATTACK = 8.0F;
    public static final float BASE_SPEED = 0.5F;
    public static final Predicate<ItemStack> IS_FOOD_ITEM;
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(FriendBlazeEntity.class, EntityDataSerializers.BYTE);
    private static final Cache<EntityDataAccessor<StatusMode>> MODE = Cache.make(() -> (EntityDataAccessor<StatusMode>) SynchedEntityData.defineId(FriendBlazeEntity.class, ModSerializers.MODE_SERIALIZER.get().getSerializer()));

    static {
        IS_FOOD_ITEM = (itemStack) -> itemStack.getItem() == Items.TNT;
    }

    private int exp = 0;
    private int currentLevel = -1;
    private float allowedHeightOffset = 0.5F;
    private int nextHeightOffsetChangeTick;

    public FriendBlazeEntity(EntityType<? extends TamableAnimal> p_21803_, Level p_21804_) {
        super(p_21803_, p_21804_);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 8.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    public static void initDataParameters() {
        MODE.get();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK)
                .add(Attributes.MOVEMENT_SPEED, BASE_SPEED)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.MAX_HEALTH, BASE_HEALTH)
                ;
    }

    public static float getItemHealAmount(ItemStack stack) {
        if (stack.getItem() == Items.TNT) {
            return 8.0f;
        }
        if (stack.getItem() == Items.GUNPOWDER) {
            return 2.0f;
        }
        return 0;
    }

    public static FriendBlazeEntity spawnFromItemStack(ServerLevel world, ItemStack itemStack, Player player) {
        Vec3 pos = player.position();
        EntityData entityData = new EntityData(itemStack);
        entityData.putOwner(player);
        return (FriendBlazeEntity) ModEntities.FRIEND_BLAZE.get().spawn(world, itemStack, player, new BlockPos(pos.x, Math.ceil(pos.y), pos.z), MobSpawnType.SPAWN_EGG, false, false);
    }

    public static float getMaxLevelHealth() {
        return BASE_HEALTH + BlazeLevels.getLevelHealth(BlazeLevels.LEVELS.length - 1);
    }

    public static float getLevelHealth(int level) {
        return BASE_HEALTH + BlazeLevels.getLevelHealth(level);
    }

    public static float getLevelAttack(int level) {
        return BASE_ATTACK + BlazeLevels.getLevelAttack(level);
    }

    public static float getLevelSpeed(int level) {
        return BASE_SPEED + BlazeLevels.getLevelSpeed(level);
    }

    public boolean isCharged() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setCharged(boolean pOnFire) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (pOnFire) {
            b0 = (byte) (b0 | 1);
        } else {
            b0 = (byte) (b0 & -2);
        }

        this.entityData.set(DATA_FLAGS_ID, b0);
    }

    public StatusMode getMode() {
        return this.entityData.get(MODE.get());
    }

    public void setMode(StatusMode collar) {
        this.entityData.set(MODE.get(), collar);
    }

    public boolean isMode(StatusMode... modes) {
        StatusMode mode = this.getMode();
        for (StatusMode test : modes) {
            if (mode == test) {
                return true;
            }
        }

        return false;
    }

    public int getExp() {
        return this.exp;
    }

    private void setExp(int exp) {
        if (exp > BlazeLevels.LEVELS[BlazeLevels.LEVELS.length - 1].totalExpNeeded) {
            exp = BlazeLevels.LEVELS[BlazeLevels.LEVELS.length - 1].totalExpNeeded;
        }
        this.exp = exp;
        this.tryLevelUp();
    }

    private void addExp(int expToAdd) {
        this.setExp(this.exp + expToAdd);
    }

    private void gainExp(int expToAdd) {
        if (this.exp != BlazeLevels.LEVELS[BlazeLevels.LEVELS.length - 1].totalExpNeeded) {
            this.addExp(expToAdd);
        }
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    private void setCurrentLevel(int exp) {
        this.exp = exp;
        this.currentLevel = BlazeLevels.getLevelByExp((int) this.exp);
    }

    private void tryLevelUp() {
        if (BlazeLevels.LEVELS.length > this.currentLevel + 1 && BlazeLevels.LEVELS[this.currentLevel + 1].totalExpNeeded <= this.exp) {
            this.currentLevel++;
            this.notifyLevelUp(this.currentLevel);
            this.setLevelAttributes(this.currentLevel);
            this.heal(this.getMaxHealth());
        }
    }

    private void writeExpToTag(EntityData entityData) {
        entityData.putExp(this.getExp());
    }

    public boolean isInjured() {
        return this.getHealth() < this.getMaxHealth();
    }

    public boolean canEat(ItemStack stack) {
        return this.isInjured() && IS_FOOD_ITEM.test(stack);
    }

    public boolean healWithItem(ItemStack stack) {
        if (!this.canEat(stack)) return false;
        float amount = getItemHealAmount(stack);
        if (amount > 0) {
            this.heal(amount);
            return true;
        }
        return false;
    }

    public boolean returnToPlayerInventory() {
        if (this.level.isClientSide) return false;
        ServerPlayer player = (ServerPlayer) this.getOwner();
        if (player != null) {
            Inventory inventory = player.getInventory();
            ImmutableList<NonNullList<ItemStack>> mainAndOffhand = ImmutableList.of(inventory.items, inventory.offhand);
            for (NonNullList<ItemStack> defaultedList : mainAndOffhand) {
                for (int i = 0; i < defaultedList.size(); ++i) {
                    if (defaultedList.get(i).getItem() == ModItems.blaze_ring && defaultedList.get(i).getOrCreateTag().getUUID("EntityUUID").equals(this.getUUID())) {
                        defaultedList.set(i, this.toItemStack());
                        this.remove(RemovalReason.KILLED);
                        level.playSound(null, player.blockPosition(), SoundEvents.SLIME_ATTACK, SoundSource.AMBIENT, 1F, 1F);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void onAttack(Entity target) {
        this.gainExp(2);
        if (target instanceof LivingEntity livingTarget) {
            if (getCurrentLevel() > 10) {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
            }
            if (getCurrentLevel() > 15) {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 3));
            }
            if (getCurrentLevel() >= 19) {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 3));
            }
        }
    }

    public ItemStack toItemStack() {
        ItemStack batItemStack = new ItemStack(ModItems.blaze_ring);
        if (this.hasCustomName()) {
            batItemStack.setHoverName(this.getCustomName());
        }
        float percentage = 1 - (this.getHealth() / this.getMaxHealth());
        batItemStack.setDamageValue(Math.round(percentage * batItemStack.getMaxDamage()));

        EntityData.fromFriendBlazeEntity(this).toStack(batItemStack);
        return batItemStack;
    }

    private ItemStack getRingItemStack() {
        ServerPlayer player = (ServerPlayer) this.getOwner();
        if (player != null) {
            Inventory inventory = player.getInventory();
            ImmutableList<NonNullList<ItemStack>> mainAndOffhand = ImmutableList.of(inventory.items, inventory.offhand);
            for (NonNullList<ItemStack> defaultedList : mainAndOffhand) {
                for (ItemStack stack : defaultedList) {
                    if (stack.getItem() == ModItems.blaze_ring && stack.getOrCreateTag().getUUID("EntityUUID").equals(this.getUUID())) {
                        return stack;
                    }
                }
            }
        }
        return null;
    }

    protected void notifyLevelUp(int level) {
        if (level > 0) {
            MutableComponent message = new TranslatableComponent("entity.friendblaze.blaze.level_up", this.hasCustomName() ? this.getCustomName() : new TranslatableComponent("entity.friendblaze.blaze.your_blaze"), level + 1).append("\n");
            if (BlazeLevels.LEVELS[level].healthBonus > BlazeLevels.LEVELS[level - 1].healthBonus) {
                message.append(new TextComponent("+").withStyle(ChatFormatting.GOLD)).append(" ");
                message.append(new TranslatableComponent("entity.friendblaze.blaze.level_up_health", (int) (BlazeLevels.LEVELS[level].healthBonus - BlazeLevels.LEVELS[level - 1].healthBonus))).append(" ");
            }
            if (BlazeLevels.LEVELS[level].attackBonus > BlazeLevels.LEVELS[level - 1].attackBonus) {
                message.append(new TextComponent("+").withStyle(ChatFormatting.GOLD)).append(" ");
                message.append(new TranslatableComponent("entity.friendblaze.blaze.level_up_attack", (int) (BlazeLevels.LEVELS[level].attackBonus - BlazeLevels.LEVELS[level - 1].attackBonus))).append(" ");
            }
            if (BlazeLevels.LEVELS[level].speedBonus > BlazeLevels.LEVELS[level - 1].speedBonus) {
                message.append(new TextComponent("+").withStyle(ChatFormatting.GOLD)).append(" ");
                message.append(new TranslatableComponent("entity.friendblaze.blaze.level_up_speed", Math.round(100 - ((BASE_SPEED + BlazeLevels.LEVELS[level - 1].speedBonus) / (BASE_SPEED + BlazeLevels.LEVELS[level].speedBonus) * 100)))).append(" ");
            }
            ((Player) Objects.requireNonNull(this.getOwner())).displayClientMessage(message, false);
        }
    }

    private boolean isWithinDistanceToAttack(LivingEntity entity) {
        return this.distanceToSqr(entity) < (double) (this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + entity.getBbWidth());
    }

    protected void setLevelAttributes(int level) {
        this.getAttributes().getInstance(Attributes.MAX_HEALTH).setBaseValue(getLevelHealth(level));
        this.getAttributes().getInstance(Attributes.ATTACK_DAMAGE).setBaseValue(getLevelAttack(level));
        this.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).setBaseValue(getLevelSpeed(level));
    }

    @Override
    protected PathNavigation createNavigation(@NotNull Level pLevel) {
        FlyingPathNavigation birdNavigation = new FlyingPathNavigation(this, pLevel);
        birdNavigation.setCanOpenDoors(false);
        birdNavigation.setCanFloat(true);
        birdNavigation.setCanPassDoors(true);
        return birdNavigation;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODE.get(), StatusMode.DOCILE);
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);

    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new BlazeAttackGoal(this));
        this.goalSelector.addGoal(4, new BlazeFollowPlayerGoal(this, 1.0D, 2.5f, 24f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));


        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
        this.targetSelector.addGoal(6, new BerserkerModeGoal<>(this, Monster.class, false));
    }


    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel p_146743_, @NotNull AgeableMob p_146744_) {
        return null;
    }

    @Override
    public void aiStep() {
        if (!this.onGround && this.getDeltaMovement().y < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }

        if (this.level.isClientSide) {
            if (this.random.nextInt(24) == 0 && !this.isSilent()) {
                this.level.playLocalSound(this.getX() + 0.5D, this.getY() + 0.5D, this.getZ() + 0.5D, SoundEvents.BLAZE_BURN, this.getSoundSource(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
            }

            for (int i = 0; i < 1; ++i) {
                this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
            }
        }

        super.aiStep();
    }


    @Override
    public void die(@NotNull DamageSource source) {
        if (!this.returnToPlayerInventory()) super.die(source);
    }

//    @Override
//    public boolean hurt(@NotNull DamageSource source, float amount) {
//        if (this.isInvulnerableTo(source)) {
//            return false;
//        }else {
//            if (!source.isBypassArmor()) {
//                    if (source.getEntity() instanceof LivingEntity target) {
//                        LivingEntity owner = this.getOwner();
//                        if (target != owner  && this.isWithinDistanceToAttack(target)){
//                            this.level.playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND , SoundSource.PLAYERS, 0.15F, this.getVoicePitch() + 2F);
//                            float targetHealth = target.getHealth();
//                            target.hurt(source, (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE) + amount);
//                            this.onAttack(target, targetHealth, target.getHealth());
//                            return false;
//                        }
//                    }
//                    this.level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.2F, this.getVoicePitch());
//                    return false;
//
//            }
//
//            return super.hurt(source, amount);
//        }
//    }

    @Override
    public @NotNull InteractionResult mobInteract(Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pHand);
        if (this.level.isClientSide) {
            return this.canEat(itemStack) ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            boolean res = this.healWithItem(itemStack);
            if (res) {
                if (!pPlayer.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                this.level.broadcastEntityEvent(this, (byte) 8);
                return InteractionResult.SUCCESS;
            } else if (IS_FOOD_ITEM.test(itemStack) && pPlayer == this.getOwner()) {
                ItemStack ringItemStack = this.getRingItemStack();
                if (ringItemStack == null) {
                    pPlayer.addItem(this.toItemStack());
                    this.remove(RemovalReason.DISCARDED);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        EntityData entityData = new EntityData(pCompound);
        this.setCurrentLevel(entityData.getExp());
        this.setLevelAttributes(this.currentLevel);
        super.readAdditionalSaveData(pCompound);

    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        EntityData entityData = new EntityData(pCompound);
        entityData.putExp(this.getExp());
        this.writeExpToTag(entityData);
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected void customServerAiStep() {
        --this.nextHeightOffsetChangeTick;
        if (this.nextHeightOffsetChangeTick <= 0) {
            this.nextHeightOffsetChangeTick = 100;
            this.allowedHeightOffset = 0.5F + (float) this.random.nextGaussian() * 3.0F;
        }

        LivingEntity livingentity = this.getTarget();
        if (livingentity != null && livingentity.getEyeY() > this.getEyeY() + (double) this.allowedHeightOffset && this.canAttack(livingentity)) {
            Vec3 vec3 = this.getDeltaMovement();
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, ((double) 0.3F - vec3.y) * (double) 0.3F, 0.0D));
            this.hasImpulse = true;
        }
        super.customServerAiStep();
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, @NotNull DamageSource pSource) {
        return false;
    }


    @Override
    public boolean isOnFire() {
        return this.isCharged();
    }


    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

}
