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
    private void tlaughmod$applyVisibleStance(AvatarRenderState state, CallbackInfo ci) {
        if (!TLaughModClient.isAnimationActive()) {
            return;
        }

        float time = System.nanoTime() / 1_000_000_000.0f;
        float speedFactor = 10.0f + TLaughModClient.getSpeed() * 1.6f;
        float sway = (float) Math.sin(time * speedFactor);
        float bounce = (float) Math.sin(time * speedFactor * 1.85f);

        // Clear, readable cartoon stance: arms raised high and legs spread slightly.
        this.rightArm.xRot = -2.72f + bounce * 0.11f;
        this.rightArm.yRot = -0.10f + sway * 0.08f;
        this.rightArm.zRot = -0.72f - sway * 0.16f;

        this.leftArm.xRot = -2.72f - bounce * 0.11f;
        this.leftArm.yRot = 0.10f - sway * 0.08f;
        this.leftArm.zRot = 0.72f + sway * 0.16f;

        this.rightLeg.zRot = -0.20f + sway * 0.035f;
        this.leftLeg.zRot = 0.20f - sway * 0.035f;

        // Light body/head wobble so the stance is visibly animated without breaking movement.
        this.body.zRot = sway * 0.10f;
        this.head.zRot += -sway * 0.07f;
        this.head.xRot += bounce * 0.025f;
    }
}
