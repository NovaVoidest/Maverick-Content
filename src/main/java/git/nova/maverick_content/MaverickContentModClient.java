package git.nova.maverick_content;

import git.nova.maverick_content.client.gui.CustomTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.sounds.MusicManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Field;

@Mod(value = MaverickContentMod.MODID, dist = Dist.CLIENT)
public class MaverickContentModClient {

    private static Field[] intFields;

    public MaverickContentModClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(MaverickContentModClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(MaverickContentModClient.class);

        var fields = MusicManager.class.getDeclaredFields();
        var intList = new java.util.ArrayList<Field>();
        for (Field f : fields) {
            f.setAccessible(true);
            if (f.getType() == int.class) {
                intList.add(f);
            }
        }
        intFields = intList.toArray(new Field[0]);
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen) {
            event.setNewScreen(new CustomTitleScreen());
        }
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null) {
            CustomTitleScreen.stopMusic(mc);
            return;
        }

        mc.getMusicManager().stopPlaying();

        if (intFields != null) {
            try {
                for (Field f : intFields) {
                    f.setInt(mc.getMusicManager(), Integer.MAX_VALUE);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
