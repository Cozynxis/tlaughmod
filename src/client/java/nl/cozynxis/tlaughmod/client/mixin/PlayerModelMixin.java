package nl.cozynxis.tlaughmod.client.mixin;

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
            // Stronger laugh-like cartoon wobble without injecting into the 26.2 render pipeline.
            this.body.zRot = wobble * 0.72f;
            this.body.xRot = snap * 0.035f;
            this.head.zRot = -wobble * 0.48f;
            this.head.xRot += fast * 0.045f;
            this.rightLeg.xRot += snap * 0.07f;
            this.leftLeg.xRot -= snap * 0.07f;

            // Extra arm motion keeps the visual effect energetic while remaining compatible
            // with Minecraft 26.2's PlayerModel render-state architecture.
            this.rightArm.yRot = 0.10f + snap * 0.10f;
            this.leftArm.yRot = -0.10f - snap * 0.10f;
        }
    }
}
