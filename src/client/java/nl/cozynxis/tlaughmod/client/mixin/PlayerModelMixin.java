package nl.cozynxis.tlaughmod.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import nl.cozynxis.tlaughmod.client.TLaughModClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {
    protected PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(
        method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
        at = @At("TAIL")
    )
    private void tlaughmod$applyPose(AvatarRenderState state, CallbackInfo ci) {
        if (!TLaughModClient.isStanceActive()) {
            return;
        }

        float time = (System.nanoTime() / 1_000_000_000.0f);
        float fast = (float) Math.sin(time * 15.0f);
        float snap = (float) Math.sin(time * 25.0f);
        float wobble = TLaughModClient.isFeatherAnimationActive()
            ? fast * 0.20f + snap * 0.055f
            : 0.0f;

        // Strong cartoon stance: both arms up, legs apart.
        this.rightArm.xRot = -2.58f + wobble * 0.65f;
        this.rightArm.zRot = -0.62f - wobble * 0.75f;
        this.leftArm.xRot = -2.58f - wobble * 0.65f;
        this.leftArm.zRot = 0.62f + wobble * 0.75f;

        this.rightLeg.zRot = -0.24f + wobble * 0.24f;
        this.leftLeg.zRot = 0.24f - wobble * 0.24f;

        if (TLaughModClient.isFeatherAnimationActive()) {
            this.body.zRot = wobble * 0.72f;
            this.body.xRot = snap * 0.035f;
            this.head.zRot = -wobble * 0.48f;
            this.head.xRot += fast * 0.045f;
            this.rightLeg.xRot += snap * 0.07f;
            this.leftLeg.xRot -= snap * 0.07f;
        }
    }

    // Re-render the player's own Minecraft arm geometry as two animated helper arms.
    // They use the same active player texture/VertexConsumer as the normal model.
    @Inject(method = "renderToBuffer", at = @At("TAIL"))
    private void tlaughmod$renderHelperArms(
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
        int packedLight,
        int packedOverlay,
        int color,
        CallbackInfo ci
    ) {
        if (!TLaughModClient.isFeatherAnimationActive()) {
            return;
        }

        float time = (System.nanoTime() / 1_000_000_000.0f);
        float sweep = (float) Math.sin(time * 18.0f);
        float jab = (float) Math.sin(time * 30.0f);

        poseStack.pushPose();
        poseStack.translate(-0.28 + sweep * 0.045, 0.10 + jab * 0.012, 0.16);
        poseStack.scale(0.78f, 0.78f, 0.78f);
        this.rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.28 - sweep * 0.045, 0.10 - jab * 0.012, 0.16);
        poseStack.scale(0.78f, 0.78f, 0.78f);
        this.leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }
}
