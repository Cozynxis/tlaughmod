package nl.cozynxis.tlaughmod.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class TLaughModClient implements ClientModInitializer {
    private static boolean animationActive = false;
    private static boolean particlesEnabled = true;
    private static long animationTicks = 0L;
    private static int intensity = 3;
    private static int speed = 3;

    @Override
    public void onInitializeClient() {
        registerCommands();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                return;
            }

            animationTicks++;
            if (!animationActive || !particlesEnabled) {
                return;
            }

            int interval = switch (speed) {
                case 1 -> 4;
                case 2 -> 3;
                case 3 -> 2;
                default -> 1;
            };

            if (animationTicks % interval != 0L) {
                return;
            }

            double t = animationTicks * (0.11 + speed * 0.045);
            double px = client.player.getX();
            double py = client.player.getY();
            double pz = client.player.getZ();
            var feather = new ItemParticleOption(ParticleTypes.ITEM, Items.FEATHER);

            spawnOrbit(client, feather, px, py + 1.22, pz, t, 0.44, 0.13, 0.0);
            spawnOrbit(client, feather, px, py + 1.16, pz, -t * 1.06, 0.46, 0.12, Math.PI);

            if (intensity >= 2) {
                spawnOrbit(client, feather, px, py + 0.88, pz, t * 1.25, 0.35, 0.10, Math.PI / 2.0);
            }
            if (intensity >= 3) {
                spawnOrbit(client, feather, px, py + 0.26, pz, t * 1.13, 0.32, 0.05, 0.0);
                spawnOrbit(client, feather, px, py + 0.26, pz, -t * 1.11, 0.32, 0.05, Math.PI);
            }
            if (intensity >= 4) {
                spawnOrbit(client, feather, px, py + 1.48, pz, -t * 1.35, 0.28, 0.09, Math.PI / 3.0);
                spawnOrbit(client, feather, px, py + 0.64, pz, t * 1.42, 0.42, 0.08, Math.PI * 1.5);
            }
            if (intensity >= 5 && animationTicks % 6L == 0L) {
                spawnBurst(client, feather, px, py + 1.0, pz, t);
            }
        });
    }

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("tlaugh")
                .then(ClientCommands.literal("start").executes(context -> {
                    animationActive = true;
                    context.getSource().sendFeedback(Component.literal(
                        "T-Laugh started. Intensity " + intensity + "/5, speed " + speed + "/5."
                    ));
                    return 1;
                }))
                .then(ClientCommands.literal("stop").executes(context -> {
                    animationActive = false;
                    context.getSource().sendFeedback(Component.literal("T-Laugh stopped."));
                    return 1;
                }))
                .then(ClientCommands.literal("toggle").executes(context -> {
                    animationActive = !animationActive;
                    context.getSource().sendFeedback(Component.literal(
                        animationActive ? "T-Laugh enabled." : "T-Laugh disabled."
                    ));
                    return 1;
                }))
                .then(ClientCommands.literal("status").executes(context -> {
                    context.getSource().sendFeedback(Component.literal(
                        "T-Laugh: " + (animationActive ? "ON" : "OFF")
                            + " | particles: " + (particlesEnabled ? "ON" : "OFF")
                            + " | intensity: " + intensity + "/5"
                            + " | speed: " + speed + "/5"
                    ));
                    return 1;
                }))
                .then(ClientCommands.literal("particles")
                    .then(ClientCommands.literal("on").executes(context -> {
                        particlesEnabled = true;
                        context.getSource().sendFeedback(Component.literal("T-Laugh particles enabled."));
                        return 1;
                    }))
                    .then(ClientCommands.literal("off").executes(context -> {
                        particlesEnabled = false;
                        context.getSource().sendFeedback(Component.literal("T-Laugh particles disabled."));
                        return 1;
                    })))
                .then(ClientCommands.literal("intensity")
                    .then(ClientCommands.argument("level", IntegerArgumentType.integer(1, 5)).executes(context -> {
                        intensity = IntegerArgumentType.getInteger(context, "level");
                        context.getSource().sendFeedback(Component.literal(
                            "T-Laugh intensity set to " + intensity + "/5."
                        ));
                        return 1;
                    })))
                .then(ClientCommands.literal("speed")
                    .then(ClientCommands.argument("level", IntegerArgumentType.integer(1, 5)).executes(context -> {
                        speed = IntegerArgumentType.getInteger(context, "level");
                        context.getSource().sendFeedback(Component.literal(
                            "T-Laugh speed set to " + speed + "/5."
                        ));
                        return 1;
                    })))
                .then(ClientCommands.literal("preset")
                    .then(ClientCommands.literal("calm").executes(context -> applyPreset(context, 1, 1, "calm")))
                    .then(ClientCommands.literal("normal").executes(context -> applyPreset(context, 3, 3, "normal")))
                    .then(ClientCommands.literal("crazy").executes(context -> applyPreset(context, 5, 5, "crazy"))))
                .then(ClientCommands.literal("burst").executes(context -> {
                    var client = context.getSource().getClient();
                    if (client.player != null && client.level != null) {
                        var feather = new ItemParticleOption(ParticleTypes.ITEM, Items.FEATHER);
                        for (int i = 0; i < 3; i++) {
                            spawnBurst(client, feather, client.player.getX(), client.player.getY() + 1.0, client.player.getZ(), animationTicks + i);
                        }
                    }
                    context.getSource().sendFeedback(Component.literal("T-Laugh feather burst!"));
                    return 1;
                }))
            );

            // Backwards-compatible aliases from older versions.
            dispatcher.register(ClientCommands.literal("ticklestart").executes(context -> {
                animationActive = true;
                context.getSource().sendFeedback(Component.literal("T-Laugh animation started."));
                return 1;
            }));

            dispatcher.register(ClientCommands.literal("ticklestop").executes(context -> {
                animationActive = false;
                context.getSource().sendFeedback(Component.literal("T-Laugh animation stopped."));
                return 1;
            }));

            dispatcher.register(ClientCommands.literal("ticklestance").executes(context -> {
                animationActive = !animationActive;
                context.getSource().sendFeedback(Component.literal(
                    animationActive ? "T-Laugh effect enabled." : "T-Laugh effect disabled."
                ));
                return 1;
            }));
        });
    }

    private static int applyPreset(
        com.mojang.brigadier.context.CommandContext<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> context,
        int newIntensity,
        int newSpeed,
        String name
    ) {
        intensity = newIntensity;
        speed = newSpeed;
        animationActive = true;
        particlesEnabled = true;
        context.getSource().sendFeedback(Component.literal(
            "T-Laugh preset '" + name + "' enabled (intensity " + intensity + "/5, speed " + speed + "/5)."
        ));
        return 1;
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

    private static void spawnBurst(
        net.minecraft.client.Minecraft client,
        ItemParticleOption feather,
        double centerX,
        double centerY,
        double centerZ,
        double time
    ) {
        int count = 5 + intensity * 2;
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2.0 * i / count) + time * 0.05;
            double radius = 0.30 + (i % 3) * 0.08;
            double x = centerX + Math.cos(angle) * radius;
            double y = centerY + ((i % 4) - 1.5) * 0.08;
            double z = centerZ + Math.sin(angle) * radius;
            double vx = Math.cos(angle) * 0.025;
            double vy = 0.012 + (i % 2) * 0.006;
            double vz = Math.sin(angle) * 0.025;
            client.level.addParticle(feather, x, y, z, vx, vy, vz);
        }
    }
}
