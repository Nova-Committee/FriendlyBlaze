package nova.committee.friendblaze.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/5 12:08
 * Version: 1.0
 */
public class EntityData {

    private CompoundTag tag;

    public EntityData(ItemStack itemStack) {
        this.tag = itemStack.getTagElement("EntityTag");
        if (this.tag == null) {
            this.tag = new CompoundTag();
        }
    }

    public EntityData(CompoundTag tag) {
        this.tag = tag;
    }

    public static EntityData fromFriendBlazeEntity(FriendBlazeEntity entity) {
        CompoundTag entityTag = new CompoundTag();
        entity.save(entityTag);
        EntityData entityData = new EntityData(entityTag);
        entityData.clearAttributes();
        return entityData;
    }

    public static CompoundTag getFromStack(ItemStack itemStack) {
        return itemStack.getTagElement("EntityTag");
    }

    public static void toStack(ItemStack itemStack, CompoundTag entityTag) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.put("EntityTag", entityTag);
    }

    public static void createIfMissing(ItemStack batItemStack) {
        CompoundTag tag = batItemStack.getOrCreateTag();
        if (!tag.contains("EntityTag")) {
            EntityData entityData = new EntityData(batItemStack);
            if (tag.contains("entityData")) {
                CompoundTag oldEntityData = tag.getCompound("entityData");
                entityData.putHealth(oldEntityData.getFloat("health"));
                entityData.putExp(oldEntityData.getInt("exp"));
            } else {
                entityData.putHealth(FriendBlazeEntity.BASE_HEALTH);
            }
            entityData.toStack(batItemStack);
        }
    }

    public void clearAttributes() {
        // Vanilla Position attributes
        this.tag.remove("Pos");
        this.tag.remove("Motion");
        this.tag.remove("Rotation");
        // Vanilla Stat attibrutes
        this.tag.remove("Attributes");
        // Vanilla Negative effects
        this.tag.remove("Fire");
        // Probably from some mod
        this.tag.remove("BoundingBox");
    }

    public void toStack(ItemStack itemStack) {
        toStack(itemStack, this.tag);
    }

    public float getHealth() {
        return this.tag.getFloat("Health");
    }

    public void putHealth(float health) {
        this.tag.putFloat("Health", health);
    }

    public int getExp() {
        return this.tag.getInt("Exp");
    }

    public void putExp(int exp) {
        this.tag.putInt("Exp", exp);
    }

    public void putOwner(Player player) {
        this.tag.putUUID("Owner", player.getUUID());
    }

}
