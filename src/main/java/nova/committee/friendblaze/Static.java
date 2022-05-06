package nova.committee.friendblaze;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/4 18:51
 * Version: 1.0
 */
public class Static {
    public static final String MOD_ID = "friendblaze";

    public static final Logger logger = LogManager.getLogger(MOD_ID);


    public static ResourceLocation getResource(String name) {
        return getResource(MOD_ID, name);
    }

    public static ResourceLocation getResource(String modId, String name) {
        return new ResourceLocation(modId, name);
    }

    public static String getResourcePath(String name) {
        return getResourcePath(MOD_ID, name);
    }

    public static String getResourcePath(String modId, String name) {
        return getResource(modId, name).toString();
    }

}
