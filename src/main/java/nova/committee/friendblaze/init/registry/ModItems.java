package nova.committee.friendblaze.init.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import nova.committee.friendblaze.common.item.RingItem;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/5 12:39
 * Version: 1.0
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static Item blaze_ring;
    public static Item blaze_item;

    @SubscribeEvent
    public static void registryItems(RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        registry.registerAll(
                blaze_ring = new RingItem()
                //blaze_item = new
        );
    }
}
