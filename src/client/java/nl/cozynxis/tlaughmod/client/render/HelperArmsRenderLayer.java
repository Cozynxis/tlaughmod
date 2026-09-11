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
        float time = (System.nanoTime() / 1_000_000_000.0f)
            * (1.35f + TLaughModClient.getSpeed() * 0.28f);

        float sweep = (float) Math.sin(time * 4.4f);
        float quick = (float) Math.sin(time * 8.8f);
        float bob = (float) Math.cos(time * 6.2f);

        var skinTexture = state.skin.body().texturePath();
        RenderType renderType = RenderTypes.entityTranslucentCullItemTarget(skinTexture);

        // Extra vanilla-style Minecraft arms. They sit clearly OUTSIDE and slightly
        // BEHIND the player's torso, then sweep inward/outward along the torso sides.
        submitHelperArm(
            poseStack, nodeCollector, model.rightArm, renderType, light,
            -1.0f, sweep, quick, bob
        );
        submitHelperArm(
            poseStack, nodeCollector, model.leftArm, renderType, light,
            1.0f, -sweep, -quick, -bob
        );
    }

    private static void submitHelperArm(
        PoseStack poseStack,
        SubmitNodeCollector nodeCollector,
        ModelPart arm,
        RenderType renderType,
        int light,
        float side,
        float sweep,
        float quick,
        float bob
    ) {
        poseStack.pushPose();

        // The previous values were too close to the body and caused clipping.
        // These offsets keep the helper arms beside the model, with enough rear depth
        // to make them look like separate arms approaching from behind.
        float outward = 0.52f + sweep * 0.095f;
        float vertical = 0.08f + bob * 0.028f;
        float behind = 0.34f + quick * 0.018f;

        poseStack.translate(side * outward, vertical, behind);

        // Angle the arms toward the torso while keeping their bases clearly outside it.
        poseStack.mulPose(Axis.XP.rotation(-0.62f + quick * 0.14f));
        poseStack.mulPose(Axis.YP.rotation(side * (-0.22f + sweep * 0.10f)));
        poseStack.mulPose(Axis.ZP.rotation(side * (0.94f + quick * 0.17f)));
        poseStack.scale(0.84f, 0.84f, 0.84f);

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
