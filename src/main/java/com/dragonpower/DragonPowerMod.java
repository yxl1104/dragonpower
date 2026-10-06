package com.dragonpower;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonPowerMod implements ModInitializer {
    public static final String MOD_ID = "dragonpower";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean dragonPowerEnabled = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Ender Dragon Power Mod Loaded");
    }
}
