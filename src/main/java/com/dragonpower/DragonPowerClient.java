package com.dragonpower;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DragonPowerClient implements ClientModInitializer {
    private static KeyBinding togglePowerKey;
    public static KeyBinding dashKey;
    public static KeyBinding fireDragonBallKey;

    @Override
    public void onInitializeClient() {
        togglePowerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dragonpower.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.dragonpower"
        ));

        dashKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dragonpower.dash",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "category.dragonpower"
        ));

        fireDragonBallKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dragonpower.fireball",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.dragonpower"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (togglePowerKey.wasPressed()) {
                DragonPowerMod.dragonPowerEnabled = !DragonPowerMod.dragonPowerEnabled;
                if(client.player != null){
                    if(DragonPowerMod.dragonPowerEnabled){
                        client.player.sendMessage(Text.literal("§a末影龙能力【开启】"), true);
                    }else{
                        client.player.sendMessage(Text.literal("§c末影龙能力【关闭】"), true);
                    }
                }
            }
        });
    }
}
