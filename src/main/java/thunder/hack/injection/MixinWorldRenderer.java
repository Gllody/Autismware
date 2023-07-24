package thunder.hack.injection;

import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.injection.Redirect;
import thunder.hack.Thunderhack;
import thunder.hack.modules.render.Fullbright;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import thunder.hack.modules.render.Shaders;
import thunder.hack.utility.Util;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.ShaderManager;

import static thunder.hack.utility.Util.mc;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @ModifyVariable(method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "STORE"), ordinal = 0)
    private static int getLightmapCoordinatesModifySkyLight(int sky) {
        if(Thunderhack.moduleManager.get(Fullbright.class).isOn())
            return  (Thunderhack.moduleManager.get(Fullbright.class).brightness.getValue());
        return sky;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/PostEffectProcessor;render(F)V", ordinal = 0))
    void replaceShaderHook(PostEffectProcessor instance, float tickDelta) {
        Shaders shaders = Thunderhack.moduleManager.get(Shaders.class);
        if (shaders.isEnabled() && mc.world != null) {
            if(shaders.mode.getValue() == Shaders.Mode.Default) {
                ShaderManager.OUTLINE.setAlpha(shaders.outlineColor.getValue().getAlpha(), shaders.glow.getValue());
                ShaderManager.OUTLINE.setLineWidth(shaders.lineWidth.getValue());
                ShaderManager.OUTLINE.setQuality(shaders.quality.getValue());
                ShaderManager.OUTLINE.setColor(shaders.fillColor1.getValue().getColorObject());
                ShaderManager.OUTLINE.setOutlineColor(shaders.outlineColor.getValue().getColorObject());
                ShaderManager.OUTLINE.render(tickDelta);
            } else if (shaders.mode.getValue() == Shaders.Mode.Smoke) {
                ShaderManager.SMOKE.setAlpha(shaders.outlineColor.getValue().getAlpha(), shaders.glow.getValue());
                ShaderManager.SMOKE.setAlpha1(shaders.fillAlpha.getValue());
                ShaderManager.SMOKE.setLineWidth(shaders.lineWidth.getValue());
                ShaderManager.SMOKE.setQuality(shaders.quality.getValue());

                ShaderManager.SMOKE.setFirst(shaders.outlineColor.getValue().getColorObject());
                ShaderManager.SMOKE.setSecond(shaders.outlineColor1.getValue().getColorObject());
                ShaderManager.SMOKE.setThird(shaders.outlineColor2.getValue().getColorObject());

                ShaderManager.SMOKE.setFFirst(shaders.fillColor1.getValue().getColorObject());
                ShaderManager.SMOKE.setFSecond(shaders.fillColor2.getValue().getColorObject());
                ShaderManager.SMOKE.setFThird(shaders.fillColor3.getValue().getColorObject());

                ShaderManager.SMOKE.setOctaves(shaders.octaves.getValue());
                ShaderManager.SMOKE.setResolution(Util.getScaledResolution().getScaledWidth(),Util.getScaledResolution().getScaledHeight());
                ShaderManager.SMOKE.setTime();
                ShaderManager.SMOKE.render(tickDelta);
            } else if (shaders.mode.getValue() == Shaders.Mode.Gradient) {
                ShaderManager.GRADIENT.setAlpha(shaders.outlineColor.getValue().getAlpha(), shaders.glow.getValue());
                ShaderManager.GRADIENT.setAlpha1(shaders.fillAlpha.getValue());
                ShaderManager.GRADIENT.setAlpha2(shaders.alpha2.getValue());
                ShaderManager.GRADIENT.setLineWidth(shaders.lineWidth.getValue());
                ShaderManager.GRADIENT.setQuality(shaders.quality.getValue());
                ShaderManager.GRADIENT.setOctaves(shaders.octaves.getValue());
                ShaderManager.GRADIENT.setMoreGradient(shaders.gradient.getValue());
                ShaderManager.GRADIENT.setFactor(shaders.factor.getValue());
                ShaderManager.GRADIENT.setResolution(Util.getScaledResolution().getScaledWidth(),Util.getScaledResolution().getScaledHeight());
                ShaderManager.GRADIENT.setTime();
                ShaderManager.GRADIENT.render(tickDelta);
            }
        } else {
            instance.render(tickDelta);
        }
    }
}