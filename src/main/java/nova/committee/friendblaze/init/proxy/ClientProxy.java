package nova.committee.friendblaze.init.proxy;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nova.committee.friendblaze.init.registry.ModEntities;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 12:41
 * Version: 1.0
 */
public class ClientProxy implements IProxy {

    public void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        //Register entity rendering handlers
        //event.registerLayerDefinition(PodModel.LAYER_LOCATION, PodModel::createBodyLayer);
    }


    public void onClientSetUpEvent(FMLClientSetupEvent event) {
        //ModMenus.onClientSetup();
        ModEntities.onClientSetup();
    }

    @Override
    public void init() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::registerLayer);
        modBus.addListener(this::onClientSetUpEvent);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;


    }
}
