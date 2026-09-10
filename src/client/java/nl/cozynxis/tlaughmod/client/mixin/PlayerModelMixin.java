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
        float wobble = TLaughModClient.isFeatherAnimationActive()
            ? (float) Math.sin(time * 9.0f) * 0.08f
            : 0.0f;

        // Cartoon victory-style stance: arms raised and legs angled outward.
        this.rightArm.xRot = -2.55f + wobble;
        this.rightArm.zRot = -0.55f - wobble * 0.7f;
        this.leftArm.xRot = -2.55f - wobble;
        this.leftArm.zRot = 0.55f + wobble * 0.7f;

        this.rightLeg.zRot = -0.18f + wobble * 0.25f;
        this.leftLeg.zRot = 0.18f - wobble * 0.25f;

        if (TLaughModClient.isFeatherAnimationActive()) {
            this.body.zRot = wobble * 0.45f;
            this.head.zRot = -wobble * 0.25f;
        }
    }
}
