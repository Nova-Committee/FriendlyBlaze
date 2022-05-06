package nova.committee.friendblaze.common.serializer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import nova.committee.friendblaze.common.ai.StatusMode;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/5/4 18:52
 * Version: 1.0
 */
public class ModeSerializer implements EntityDataSerializer<StatusMode> {

    @Override
    public void write(FriendlyByteBuf buf, StatusMode value) {
        buf.writeByte(value.getIndex());
    }

    @Override
    public StatusMode read(FriendlyByteBuf buf) {
        return StatusMode.byIndex(buf.readByte());
    }

    @Override
    public StatusMode copy(StatusMode value) {
        return value;
    }
}
