package cn.snowflake.rose;


import cn.snowflake.rose.asm.MinecraftHook;
import cn.snowflake.rose.events.impl.EventFMLChannels;
import cn.snowflake.rose.events.impl.EventMotion;
import cn.snowflake.rose.events.impl.EventMove;
import cn.snowflake.rose.mod.mods.PLAYER.NoSlow;
import cn.snowflake.rose.utils.RotationUtil;
import com.darkmagician6.eventapi.EventManager;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.List;

public class Test{
    public double posY;
    public float yaw;
    public float pitch;
    public boolean onGround;

    private void test() {
        EventMotion e =new EventMotion(posY,yaw,pitch,onGround);
        EventManager.call(e);
    }
}
