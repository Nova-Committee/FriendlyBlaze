package nova.committee.friendblaze;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nova.committee.friendblaze.common.entity.FriendBlazeEntity;
import nova.committee.friendblaze.init.proxy.ClientProxy;
import nova.committee.friendblaze.init.proxy.IProxy;
import nova.committee.friendblaze.init.proxy.ServerProxy;
import nova.committee.friendblaze.init.registry.ModEntities;
import nova.committee.friendblaze.init.registry.ModSerializers;

@Mod(Static.MOD_ID)
public class Friendblaze {

    public static final IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public Friendblaze() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModSerializers.SERIALIZERS.register(modBus);

        modBus.addListener(this::attribute);
        proxy.init();
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    public void attribute(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FRIEND_BLAZE.get(), FriendBlazeEntity.createAttributes().build());
    }

}
