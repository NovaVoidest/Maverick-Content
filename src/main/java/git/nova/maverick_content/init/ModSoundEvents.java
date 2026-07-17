package git.nova.maverick_content.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import git.nova.maverick_content.MaverickContentMod;

public class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MaverickContentMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> MENU_MUSIC =
            SOUND_EVENTS.register("menu_music",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(MaverickContentMod.MODID, "menu_music")));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
