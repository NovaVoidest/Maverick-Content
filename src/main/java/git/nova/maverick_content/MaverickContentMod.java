package git.nova.maverick_content;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import git.nova.maverick_content.init.ModSoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(MaverickContentMod.MODID)
public class MaverickContentMod {
    public static final String MODID = "maverick_content";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaverickContentMod(IEventBus modEventBus, ModContainer modContainer) {
        ModSoundEvents.register(modEventBus);
    }
}
