package nova.committee.friendblaze.client.render.entity;

import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import nova.committee.friendblaze.common.entity.FriendBlazeEntity;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/4 19:32
 * Version: 1.0
 */
public class FriendBlazeRender extends MobRenderer<FriendBlazeEntity, BlazeModel<FriendBlazeEntity>> {

    private static final ResourceLocation BLAZE_LOCATION = new ResourceLocation("textures/entity/blaze.png");

    public FriendBlazeRender(EntityRendererProvider.Context context) {
        super(context, new BlazeModel<>(context.bakeLayer(ModelLayers.BLAZE)), 0.5F);
    }


    @Override
    protected int getBlockLightLevel(FriendBlazeEntity pEntity, BlockPos pPos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(FriendBlazeEntity pEntity) {
        return BLAZE_LOCATION;
    }
}
