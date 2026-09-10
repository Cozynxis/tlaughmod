package nl.cozynxis.tlaughmod;

import net.fabricmc.api.ModInitializer;

public class TLaughMod implements ModInitializer {
    public static final String MOD_ID = "tlaughmod";

    @Override
    public void onInitialize() {
        // Common initializer intentionally stays lightweight.
        // T-Laugh's pose and feather effects are client-side visuals.
    }
}
