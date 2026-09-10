package nl.cozynxis.tlaughmod;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TLaughMod implements ModInitializer {
    public static final String MOD_ID = "tlaughmod";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("ticklestance")
                .executes(context -> {
                    context.getSource().getPlayerOrException()
                        .sendSystemMessage(Component.literal("T-Laugh stance toggled!"));
                    return Command.SINGLE_SUCCESS;
                }));

            dispatcher.register(Commands.literal("ticklestart")
                .executes(context -> {
                    context.getSource().getPlayerOrException()
                        .sendSystemMessage(Component.literal("T-Laugh feather animation started!"));
                    return Command.SINGLE_SUCCESS;
                }));

            dispatcher.register(Commands.literal("ticklestop")
                .executes(context -> {
                    context.getSource().getPlayerOrException()
                        .sendSystemMessage(Component.literal("T-Laugh animation stopped."));
                    return Command.SINGLE_SUCCESS;
                }));
        });
    }
}
