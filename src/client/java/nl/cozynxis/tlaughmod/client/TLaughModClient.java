package nl.cozynxis.tlaughmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class TLaughModClient implements ClientModInitializer {
    private static boolean stanceActive = false;
    private static boolean featherAnimationActive = false;
    private static long animationTicks = 0L;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("ticklestance").executes(context -> {
                stanceActive = !stanceActive;
                if (!stanceActive) {
                    featherAnimationActive = false;
                }

                context.getSource().sendFeedback(Component.literal(
                    stanceActive ? "T-Laugh stance enabled." : "T-Laugh stance disabled."
                ));
                return 1;
            }));

            dispatcher.register(ClientCommands.literal("ticklestart").executes(context -> {
                stanceActive = true;
                featherAnimationActive = true;
                context.getSource().sendFeedback(Component.literal("T-Laugh animation started."));
                return 1;
            }));

            dispatcher.register(ClientCommands.literal("ticklestop").executes(context -> {
                featherAnimationActive = false;
                stanceActive = false;
                context.getSource().sendFeedback(Component.literal("T-Laugh animation stopped."));
                return 1;
            }));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                return;
            }

            animationTicks++;

            if (!featherAnimationActive) {
                return;
            }

            // Spawn every other tick to keep the effect smooth without flooding particles.
            if ((animationTicks & 1L) != 0L) {
                return;
            }

            double t = animationTicks * 0.22;
            double px = client.player.getX();
            double py = client.player.getY();
            double pz = client.player.getZ();

            var feather = new ItemParticleOption(ParticleTypes.ITEM, Items.FEATHER);

            // Two orbiting feather trails around the upper torso.
            spawnOrbit(client, feather, px, py + 1.25, pz, t, 0.48, 0.16, 0.0);
            spawnOrbit(client, feather, px, py + 1.20, pz, -t * 1.08, 0.50, 0.14, Math.PI);

            // A smaller middle-torso orbit for extra movement.
            spawnOrbit(client, feather, px, py + 0.90, pz, t * 1.32, 0.38, 0.12, Math.PI / 2.0);

            // Low feather sweeps around the boots.
            spawnOrbit(client, feather, px, py + 0.16, pz, t * 1.18, 0.34, 0.06, 0.0);
            spawnOrbit(client, feather, px, py + 0.16, pz, -t * 1.15, 0.34, 0.06, Math.PI);
        });
    }

    private static void spawnOrbit(
        net.minecraft.client.Minecraft client,
        ItemParticleOption feather,
        double centerX,
        double centerY,
        double centerZ,
        double time,
        double radius,
        double verticalBob,
        double phase
    ) {
        double angle = time + phase;
        double x = centerX + Math.cos(angle) * radius;
        double y = centerY + Math.sin(time * 1.7 + phase) * verticalBob;
        double z = centerZ + Math.sin(angle) * radius;

        double vx = -Math.sin(angle) * 0.015;
        double vy = Math.cos(time * 1.7 + phase) * 0.006;
        double vz = Math.cos(angle) * 0.015;

        client.level.addParticle(feather, x, y, z, vx, vy, vz);
    }

    public static boolean isStanceActive() {
        return stanceActive;
    }

    public static boolean isFeatherAnimationActive() {
        return featherAnimationActive;
    }
}
