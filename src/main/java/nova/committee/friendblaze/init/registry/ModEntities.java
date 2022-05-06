package nova.committee.friendblaze.init.registry;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import nova.committee.friendblaze.Static;
import nova.committee.friendblaze.client.render.entity.FriendBlazeRender;
import nova.committee.friendblaze.common.entity.FriendBlazeEntity;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/4 18:57
 * Version: 1.0
 */
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES, Static.MOD_ID);
    public static final RegistryObject<EntityType<FriendBlazeEntity>> FRIEND_BLAZE = register("friend_blaze", FriendBlazeEntity::new, MobCategory.CREATURE, (b) -> b
            .fireImmune()
            .sized(0.6F, 1.8F)
            .clientTrackingRange(8)
    );

    private static <E extends Entity, T extends EntityType<E>> RegistryObject<EntityType<E>> register(final String name, final EntityType.EntityFactory<E> sup, final MobCategory classification, final Function<EntityType.Builder<E>, EntityType.Builder<E>> builder) {
        return register(name, () -> builder.apply(EntityType.Builder.of(sup, classification)).build(Static.getResourcePath(name)));
    }

    private static <E extends Entity, T extends EntityType<E>> RegistryObject<T> register(final String name, final Supplier<T> sup) {
        return ENTITIES.register(name, sup);
    }

    @OnlyIn(Dist.CLIENT)
    public static void onClientSetup() {
        EntityRenderers.register(ModEntities.FRIEND_BLAZE.get(), FriendBlazeRender::new);
    }


}
