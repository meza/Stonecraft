package __STONECRAFT_BASE_PACKAGE__;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*? if fabric {*/
import net.fabricmc.api.ModInitializer;
/*?}*/

/*? if forge {*/
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*//*?}*/

/*? if neoforge {*/
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
*//*?}*/

/*? if neoforge {*/
/*@Mod(__STONECRAFT_ENTRYPOINT_CLASS__.MOD_ID)
*//*?}*/
/*? if forge {*/
/*@Mod(__STONECRAFT_ENTRYPOINT_CLASS__.MOD_ID)
*//*?}*/
public class __STONECRAFT_ENTRYPOINT_CLASS__ /*? if fabric {*/ implements ModInitializer /*?}*/ {
    public static final String MOD_ID = "__STONECRAFT_MOD_ID__";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /*? if forge {*/
    /*public __STONECRAFT_ENTRYPOINT_CLASS__(FMLJavaModLoadingContext context) {
        LOGGER.info("Hello Forge world!");
    }
    *//*?}*/

    /*? if neoforge {*/
    /*public __STONECRAFT_ENTRYPOINT_CLASS__(IEventBus modEventBus) {
        LOGGER.info("Hello NeoForge world!");
    }
    *//*?}*/

    /*? if fabric {*/
    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
    }
    /*?}*/
}
