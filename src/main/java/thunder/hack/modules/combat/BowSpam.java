package thunder.hack.modules.combat;

import com.google.common.eventbus.Subscribe;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Util;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class BowSpam extends Module {

    public Setting<Integer> ticks = new Setting<>("Delay", 3, 0, 20);
    public BowSpam() {
        super("BowSpam", "Спамит стрелами", Module.Category.COMBAT);
    }


    @Subscribe
    public void onSync(EventSync event) {
        if ((mc.player.getOffHandStack().getItem() == Items.BOW || mc.player.getMainHandStack().getItem() == Items.BOW) && mc.player.isUsingItem()) {
            if (mc.player.getItemUseTime() >= this.ticks.getValue()) {
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(mc.player.getOffHandStack().getItem() == Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND, Util.getWorldActionId(Util.mc.world)));
                mc.player.stopUsingItem();
            }
        }
    }
}