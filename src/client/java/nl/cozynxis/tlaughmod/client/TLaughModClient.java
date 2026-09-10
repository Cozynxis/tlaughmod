package nl.cozynxis.tlaughmod.client;

import net.fabricmc.api.ClientModInitializer;

public class TLaughModClient implements ClientModInitializer {
    private static boolean stanceActive = false;
    private static boolean featherAnimationActive = false;

    @Override
    public void onInitializeClient() {
        // Client rendering and procedural pose hooks are added here.
        // Kept separate from common code so multiplayer/server logic remains clean.
    }

    public static void setStanceActive(boolean active) {
        stanceActive = active;
    }

    public static boolean isStanceActive() {
        return stanceActive;
    }

    public static void setFeatherAnimationActive(boolean active) {
        featherAnimationActive = active;
    }

    public static boolean isFeatherAnimationActive() {
        return featherAnimationActive;
    }
}
