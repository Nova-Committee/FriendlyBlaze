package nova.committee.friendblaze.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import nova.committee.friendblaze.common.entity.EntityData;
import nova.committee.friendblaze.common.entity.FriendBlazeEntity;
import nova.committee.friendblaze.init.registry.ModEntities;
import nova.committee.friendblaze.init.registry.ModTab;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/5 12:25
 * Version: 1.0
 */
public class RingItem extends Item {
    public RingItem() {
        super(new Properties().tab(ModTab.TAB).stacksTo(1));

        setRegistryName("ring_blaze");
    }


    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player user, @NotNull InteractionHand hand) {
        ItemStack inHand = user.getItemInHand(hand);
        if (world instanceof ServerLevel) {
            CompoundTag tag = inHand.getOrCreateTag();
            //如果有实体标签，则右击是释放
            if (tag.contains("EntityTag")) {
                EntityData.createIfMissing(inHand);
                EntityData entityData = new EntityData(inHand);
                float entityHealth = entityData.getHealth();
                //如果生物血量为零，需要投食治疗
                if (entityHealth == 0) {
                    List<ItemEntity> list = world.getEntitiesOfClass(ItemEntity.class, user.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), (itemEntity) -> true);
                    if (list.size() > 0) {
                        ItemEntity foodItemEntity = list.get(0);
                        ItemStack stack = foodItemEntity.getItem();
                        entityHealth += FriendBlazeEntity.getItemHealAmount(stack);
                        entityData.putHealth(entityHealth);
                        stack.shrink(1);
                    }
                }
                //大于零则释放
                if (entityHealth > 0) {
                    FriendBlazeEntity blazeEntity = FriendBlazeEntity.spawnFromItemStack((ServerLevel) world, inHand, user);
                    inHand.removeTagKey("EntityTag");
                    tag.putUUID("EntityUUID", blazeEntity.getUUID());
                    Component customName = blazeEntity.getCustomName();
                    if (customName != null) {
                        tag.putString("EntityName", customName.getString());
                        inHand.setHoverName(new TranslatableComponent("item.friendblaze.blaze_ring.custom_name", customName.getString()));
                    }
                    return InteractionResultHolder.sidedSuccess(inHand, world.isClientSide());
                } else {//失败处理
                    user.displayClientMessage(new TranslatableComponent("item.friendblaze.blaze_ring.exausted", inHand.hasCustomHoverName() ? inHand.getHoverName() : new TranslatableComponent("entity.friendblaze.blaze.your_blaze")), false);
                    return InteractionResultHolder.fail(inHand);
                }
            } else {//否则是召回
                if (inHand.getOrCreateTag().contains("EntityUUID")) {//物品是否有uuid标签
                    FriendBlazeEntity entity = (FriendBlazeEntity) ((ServerLevel) world).getEntity(tag.getUUID("EntityUUID"));
                    if (entity != null) {
                        //entity.returnToPlayerInventory();
                        EntityData.fromFriendBlazeEntity(entity).toStack(inHand);
                        entity.remove(Entity.RemovalReason.KILLED);
                        return InteractionResultHolder.success(inHand);
                    } else {
                        //召回失败
                        MutableComponent name = tag.contains("EntityName") ? new TextComponent(tag.getString("EntityName")) : new TranslatableComponent("entity.friendblaze.blaze.your_blaze");
                        user.displayClientMessage(name.append(new TranslatableComponent("item.friendblaze.blaze_ring.fail")), true);
                        return InteractionResultHolder.fail(inHand);
                    }
                }
                return InteractionResultHolder.fail(inHand);
            }
            // user.displayClientMessage(new TranslatableComponent("item.friendblaze.blaze_ring.to_find"), true);
        } else {
            return InteractionResultHolder.success(inHand);
        }
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player pPlayer, @NotNull LivingEntity pInteractionTarget, @NotNull InteractionHand pUsedHand) {
        if (!pPlayer.level.isClientSide) {
            ItemStack pStack = pPlayer.getItemInHand(pUsedHand);
            if (pInteractionTarget instanceof Blaze blazeO) {

                if (pStack.getOrCreateTag().contains("EntityUUID")) {
                    FriendBlazeEntity entity = (FriendBlazeEntity) ((ServerLevel) pPlayer.level).getEntity(pStack.getOrCreateTag().getUUID("EntityUUID"));
                    if (entity != null) {
                        EntityData.fromFriendBlazeEntity(entity).toStack(pStack);
                        //entity.returnToPlayerInventory();
                        entity.remove(Entity.RemovalReason.KILLED);
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    FriendBlazeEntity blaze = ModEntities.FRIEND_BLAZE.get().create(pPlayer.level);
                    blaze.setOwnerUUID(pPlayer.getUUID());
                    pStack.getOrCreateTag().putUUID("EntityUUID", blaze.getUUID());
                    EntityData.fromFriendBlazeEntity(blaze).toStack(pStack);
                    blazeO.remove(Entity.RemovalReason.KILLED);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}
