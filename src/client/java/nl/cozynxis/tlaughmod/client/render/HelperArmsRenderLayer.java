package nl.cozynxis.tlaughmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import nl.cozynxis.tlaughmod.client.TLaughModClient;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class HelperArmsRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public HelperArmsRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(
        PoseStack poseStack,
        SubmitNodeCollector nodeCollector,
        int light,
        AvatarRenderState state,
        float limbAngle,
        float limbDistance
    ) {
        if (!TLaughModClient.isAnimationActive() || !TLaughModClient.areHelperArmsEnabled()) {
            return;
        }

        PlayerModel model = getParentModel();
        float time = (System.nanoTime() / 1_000_000_000.0f) * (1.45f + TLaughModClient.getSpeed() * 0.30f);
        float sweep = (float) Math.sin(time * 4.2f);
        float tap = (float) Math.sin(time * 8.4f);

        var skinTexture = state.skin.body().texturePath();
        RenderType renderType = RenderTypes.entityTranslucentCullItemTarget(skinTexture);

        // Two extra blocky Minecraft arms, offset slightly behind the player and
        // moving inward/outward beside the torso in a cartoon style.
        submitHelperArm(poseStack, nodeCollector, model.rightArm, renderType, light, -1.0f, sweep, tap);
        submitHelperArm(poseStack, nodeCollector, model.leftArm, renderType, light, 1.0f, -sweep, -tap);
    }

    private static void submitHelperArm(
        PoseStack poseStack,
        SubmitNodeCollector nodeCollector,
        ModelPart arm,
        RenderType renderType,
        int light,
        float side,
        float sweep,
        float tap
    ) {
        poseStack.pushPose();

        // Offset from the player's regular arm position. Positive Z places these
        // slightly behind the player in model space; X keeps each one beside the torso.
        poseStack.translate(side * (0.18f + sweep * 0.025f), 0.07f + tap * 0.012f, 0.18f);
        poseStack.mulPose(Axis.XP.rotation(-0.48f + tap * 0.15f));
        poseStack.mulPose(Axis.ZP.rotation(side * (0.58f + sweep * 0.20f)));
        poseStack.scale(0.90f, 0.90f, 0.90f);

        nodeCollector.submitModelPart(
            arm,
            poseStack,
            renderType,
            light,
            OverlayTexture.NO_OVERLAY,
            null
        );

        poseStack.popPose();
    }
}
