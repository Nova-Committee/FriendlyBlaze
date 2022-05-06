package nova.committee.friendblaze.init.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/3/31 10:36
 * Version: 1.0
 */
public class ModTab extends CreativeModeTab {
    public static CreativeModeTab TAB = new ModTab();

    public ModTab() {
        super("tab.FriendBlaze");
    }

    @Override
    public @NotNull ItemStack makeIcon() {
        return new ItemStack(Items.DIAMOND);
    }
}
