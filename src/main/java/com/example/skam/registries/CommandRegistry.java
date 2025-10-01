package com.example.skam.registries;

import com.example.skam.command.SkamCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandRegistry {

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(SkamCommands::register);
    }
}
