package cn.snowflake.rose.mod.mods.COMBAT;

import cn.snowflake.rose.events.impl.EventPacket;
import cn.snowflake.rose.mod.Category;
import cn.snowflake.rose.mod.Module;
import com.darkmagician6.eventapi.EventTarget;


import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public class Velocity extends Module {

    public Velocity() {
        super("Velocity", Category.COMBAT);
    }

    @EventTarget
    private void onPacket(EventPacket event) {
        if(mc.theWorld != null && mc.thePlayer != null && !mc.thePlayer.isDead) {
            if (event.getPacket() instanceof S12PacketEntityVelocity)
                event.setCancelled(true);
            if (event.getPacket() instanceof S27PacketExplosion)
                event.setCancelled(true);
        }
    }

}