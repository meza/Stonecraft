package gg.meza.stonecraft.e2e;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*? if fabric {*/
import net.fabricmc.api.ModInitializer;
/*?}*/

/*? if forge {*/
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*/
/*?}*/

/*? if neoforge {*/
/*import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

*//*?}*/


/*? if forgeLike {*/
/*@Mod("stonecraft_testmod")
public class TestMod {
*//*?}*/

/*? if fabric {*/
public class TestMod implements ModInitializer {
/*?}*/
    public static final String MOD_ID = "stonecraft_testmod";
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /*? if forge {*/
    /*public TestMod(final FMLJavaModLoadingContext context) {
        LOGGER.info("Hello Forge world!");
    }*/
    /*?}*/

    /*? if neoforge {*/
    /*public TestMod(IEventBus modEventBus, ModContainer modContainer) {
        CodeGameTests.register(modEventBus);
        LOGGER.info("Hello Neoforge world!");
    }
    *//*?}*/

    /*? if fabric {*/
    @Override
    public void onInitialize() {
        CodeGameTests.register();
        LOGGER.info("Hello Fabric world!");
    }
    /*?}*/
}
