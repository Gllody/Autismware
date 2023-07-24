package thunder.hack.injection;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventAttack;
import thunder.hack.events.impl.EventPlayerJump;
import thunder.hack.events.impl.EventPlayerTravel;
import thunder.hack.modules.client.Media;
import thunder.hack.modules.movement.AutoSprint;
import thunder.hack.modules.player.FreeCam;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import static thunder.hack.utility.Util.mc;

@Mixin(value = PlayerEntity.class, priority = 800)
public class MixinPlayerEntity{
    
    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;noClip:Z", opcode = Opcodes.PUTFIELD))
    void noClipHook(PlayerEntity playerEntity, boolean value) {
        if(Thunderhack.moduleManager.get(FreeCam.class).isEnabled() && !mc.player.isOnGround()){
            playerEntity.noClip = true;
        } else {
            playerEntity.noClip = playerEntity.isSpectator();
        }
    }


    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    public void getDisplayNameHook(CallbackInfoReturnable<Text> cir) {
        if(Thunderhack.moduleManager.get(Media.class).isEnabled() && Thunderhack.moduleManager.get(Media.class).nickProtect.getValue()){
            cir.setReturnValue(Text.of("Protected"));
        }
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V", shift = At.Shift.AFTER))
    public void attackAHook(CallbackInfo callbackInfo) {
        AutoSprint ks = Thunderhack.moduleManager.get(AutoSprint.class);
        if (ks.isEnabled() && ks.sprint.getValue()) {
            final float multiplier = 0.6f + 0.4f * ks.motion.getValue();
            mc.player.setVelocity(mc.player.getVelocity().x / 0.6 * multiplier, mc.player.getVelocity().y, mc.player.getVelocity().z / 0.6 * multiplier);
            mc.player.setSprinting(true);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attackAHook2(Entity target, CallbackInfo ci) {
        final EventAttack event = new EventAttack(target);
        Thunderhack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravelhookPre(Vec3d movementInput, CallbackInfo ci) {
        final EventPlayerTravel event = new EventPlayerTravel(movementInput,true);
        Thunderhack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            mc.player.move(MovementType.SELF, mc.player.getVelocity());
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("RETURN"), cancellable = true)
    private void onTravelhookPost(Vec3d movementInput, CallbackInfo ci) {
        final EventPlayerTravel event = new EventPlayerTravel(movementInput,false);
        Thunderhack.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            mc.player.move(MovementType.SELF, mc.player.getVelocity());
            ci.cancel();
        }
    }

    @Inject(method = "jump", at = @At("HEAD"))
    private void onJumpPre(CallbackInfo ci) {
        final EventPlayerJump event = new EventPlayerJump(true);
        Thunderhack.EVENT_BUS.post(event);

    }

    @Inject(method = "jump", at = @At("RETURN"))
    private void onJumpPost(CallbackInfo ci) {
        final EventPlayerJump event = new EventPlayerJump(false);
        Thunderhack.EVENT_BUS.post(event);
    }
}