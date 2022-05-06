package nova.committee.friendblaze.init.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nova.committee.friendblaze.common.entity.FriendBlazeEntity;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 20:12
 * Version: 1.0
 */
public class ServerProxy implements IProxy {


    public void onSetUpEvent(FMLCommonSetupEvent event) {
        FriendBlazeEntity.initDataParameters();
    }


    @Override
    public void init() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        //ModEntities.ENTITIES.register(modBus);

        modBus.addListener(this::onSetUpEvent);


        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
    }
}
