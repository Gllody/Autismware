package thunder.hack.modules.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.modules.Module;
import thunder.hack.setting.Setting;

import java.util.Objects;

public class MessageAppend extends Module {

    public Setting<String> word = new Setting<>("word", " TH RECODE");

    public MessageAppend() {
        super("MessageAppend", "добавляет фразу-в конце сообщения", Category.MISC);
    }

    String skip;

    @Subscribe
    public void onPacketSend(PacketEvent.Send e) {
        if (fullNullCheck()) {
            return;
        }
        if (e.getPacket() instanceof ChatMessageC2SPacket pac) {
            if(Objects.equals(pac.chatMessage(), skip)){
                return;
            }

            // Чтоб не добавляло когда ты вводишь капчу на сервере
            if(mc.player.getMainHandStack().getItem() == Items.FILLED_MAP || mc.player.getOffHandStack().getItem() == Items.FILLED_MAP)
                return;

            if (pac.chatMessage().startsWith("/") || pac.chatMessage().startsWith(Thunderhack.commandManager.getPrefix()))
                return;

            skip = pac.chatMessage() + word.getValue();
            mc.player.networkHandler.sendChatMessage(pac.chatMessage() + word.getValue());
            e.cancel();
        }
    }
}