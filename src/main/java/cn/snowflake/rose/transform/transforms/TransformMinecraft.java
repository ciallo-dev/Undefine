package cn.snowflake.rose.transform.transforms;

import cn.snowflake.rose.Client;
import cn.snowflake.rose.NativeMethod;
import cn.snowflake.rose.Season;
import cn.snowflake.rose.events.impl.EventGuiOpen;
import cn.snowflake.rose.events.impl.EventKey;
import cn.snowflake.rose.events.impl.EventTick;
import cn.snowflake.rose.mod.Module;
import cn.snowflake.rose.utils.asm.ASMUtil;
import com.darkmagician6.eventapi.EventManager;
import me.skids.margeleisgay.AuthMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.injection.ClientLoader;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class TransformMinecraft implements Opcodes{

    public static void transformMinecraft(ClassNode clazz, MethodNode method) {
        if (method.name.equals("func_71407_l") || method.name.equals("runTick")){
            NativeMethod.method1(method);
        }
        if (method.name.equals("func_152348_aa")) {
            method.instructions.insert(method.instructions.getFirst(),new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(TransformMinecraft.class), "dispatchKeypressesHook", "()V", false));
        }
        if(method.name.equals("func_71411_J") || method.name.equals("runGameLoop")){
            AbstractInsnNode target = ASMUtil.findMethodInsn(method,INVOKEVIRTUAL,"cpw/mods/fml/common/FMLCommonHandler", "onRenderTickEnd", "(F)V");
           if(target != null){
               method.instructions.insert(target,new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(TransformMinecraft.class), "drawgui", "()V", false));
           }

        }

        if (method.name.equals("displayGuiScreen") || method.name.equals("func_147108_a")){
            AbstractInsnNode abstractInsnNode = ASMUtil.findFieldInsnNode(method,GETFIELD, "net/minecraftforge/client/event/GuiOpenEvent", "gui", "Lnet/minecraft/client/gui/GuiScreen;");
            if (abstractInsnNode != null){
                InsnList insnList = new InsnList();
                insnList.add(new VarInsnNode(ALOAD,1));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(TransformMinecraft.class), "openGui", "(Lnet/minecraft/client/gui/GuiScreen;)V", false));
                method.instructions.insert(abstractInsnNode.getNext().getNext(),insnList);
            }
        }

    }

    public static void drawgui(){
        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft(),Minecraft.getMinecraft().displayWidth,Minecraft.getMinecraft().displayHeight);
        float y = scaledresolution.getScaledHeight() - 35;
        if(Minecraft.getMinecraft().theWorld != null && Client.init)
        if(!(Minecraft.getMinecraft().currentScreen instanceof GuiChat))
            if (Client.instance.getNotificationManager() != null && Client.instance.getNotificationManager().getNotifications() != null)
                for (int n = 0; n < Client.instance.getNotificationManager().getNotifications().size(); n++) {
                    Client.instance.getNotificationManager().getNotifications().get(n).draw(y);
                    y -= 14;
                }
    }

    public static void openGui(GuiScreen guiScreen){
        EventGuiOpen guiOpen = new EventGuiOpen(guiScreen);
        EventManager.call(guiOpen);
    }


    public static void dispatchKeypressesHook(){
        if (Keyboard.getEventKeyState()) {

                EventManager.call(new EventKey(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey()));

            for (Module mod : Client.instance.modManager.getModList()) {
                if (mod.getKey() != (Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey()))continue;
                if (Minecraft.getMinecraft().currentScreen == null) {

                    mod.set(!mod.isEnabled());
//                 }else if (mod.getCategory() == Category.FORGE){
//                    mod.set(!mod.isEnabled());
                }
//                break;
            }
        }
    }


    public static void runTick() {
        Client.onGameLoop();
        EventManager.call(new EventTick());
    }


}