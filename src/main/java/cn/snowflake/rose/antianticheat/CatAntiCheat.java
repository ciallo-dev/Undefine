package cn.snowflake.rose.antianticheat;

import cn.snowflake.rose.events.impl.EventFMLChannels;
import cn.snowflake.rose.mod.mods.FORGE.ScreenProtect;
import cn.snowflake.rose.utils.antianticheat.ScreenshotUtil;
import cn.snowflake.rose.utils.client.ChatUtil;
import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CatAntiCheat {

    public CatAntiCheat(){
        EventManager.register(this);
    }


    static {
    }

    Minecraft mc = Minecraft.getMinecraft();

    @EventTarget
    public void onFml(EventFMLChannels eventFMLChannels){
        // 1.2.7 以下猫反
        if (eventFMLChannels.iMessage.getClass().toString().contains("luohuayu.anticheat.message")){


            if (eventFMLChannels.iMessage.getClass().toString().contains("CPacketVanillaData")){
                eventFMLChannels.setCancelled(true);
                try {
                    eventFMLChannels.sendToServer(eventFMLChannels.iMessage.getClass().getDeclaredConstructor(boolean.class,boolean.class)
                            .newInstance(false,false));
                }catch (Exception e2313213){

                }
            }
            // screenhost check
            if (eventFMLChannels.iMessage.getClass().toString().contains("CPacketImageData")){
                try {
                    handleScreenshot(eventFMLChannels);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (eventFMLChannels.iMessage.getClass().toString().contains("CPacketInjectDetect")){
                eventFMLChannels.setCancelled(true); //     拦截检测注入包
                List<String> list = new ArrayList();
                try {
                    eventFMLChannels.sendToServer(eventFMLChannels.iMessage.getClass().getDeclaredConstructor(List.class).newInstance(list));//发送无参packet
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            if (eventFMLChannels.iMessage.getClass().toString().contains("CPacketClassFound")){
                eventFMLChannels.setCancelled(true);

                try {
                    Field ffoundClassList = eventFMLChannels.iMessage.getClass().getDeclaredField("foundClassList");
                    Field fsalt = eventFMLChannels.iMessage.getClass().getDeclaredField("salt");
                    fsalt.setAccessible(true);
                    ffoundClassList.setAccessible(true);

                    byte salt = fsalt.getByte(eventFMLChannels.iMessage);
                    List<String> foundClassList = (List<String>) ffoundClassList.get(eventFMLChannels.iMessage);
                    List<String> finallist = foundClassList;

                    finallist.removeIf(clazz ->
                            clazz.contains(this.getClass().getPackage().getName())
                    );
                    eventFMLChannels.sendToServer(eventFMLChannels.iMessage.getClass().getDeclaredConstructor(List.class,byte.class)
                            .newInstance(finallist,salt));
                }catch (Exception e2313213){

                }
            }
            if (eventFMLChannels.iMessage.getClass().toString().contains("CPacketFileHash")){
                eventFMLChannels.setCancelled(true);
                try {
                    //fileHashList
                    Field fieldlist = eventFMLChannels.iMessage.getClass().getDeclaredField("fileHashList");
                    fieldlist.setAccessible(true);
                    Field field = eventFMLChannels.iMessage.getClass().getDeclaredField("salt");
                    field.setAccessible(true);

                    List<String> list = (List<String>) fieldlist.get(eventFMLChannels.iMessage);
                    list.removeIf(inject ->
                            inject.toString().endsWith(".tmp")
                    );
                    list.removeIf(mod ->
                            mod.toString().toLowerCase().endsWith("-skipverify.jar")
                    );
                    try {
                        eventFMLChannels.sendToServer(
                                eventFMLChannels.iMessage.getClass().getDeclaredConstructor(List.class,byte.class)
                                        .newInstance(new ArrayList<String>(list),field.getByte(eventFMLChannels.iMessage))
                        );
                    } catch (InstantiationException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
        if (eventFMLChannels.iMessage.getClass().toString().contains("1710")) {
            try {//filehash and classfound check
                Constructor<? extends IMessage> fwithc = eventFMLChannels.iMessage.getClass().getConstructor(List.class, byte.class);

                Field[] fields = eventFMLChannels.iMessage.getClass().getDeclaredFields();
                Field fieldlist = null;
                Field salt = null;

                Field field1 = fields[0];
                Field field2 = fields[1];
                Field field3 = fields[2];


                field1.setAccessible(true);
                field2.setAccessible(true);

                try{
                    field1.getBoolean(eventFMLChannels.iMessage);
                    try{
                        field2.getByte(eventFMLChannels.iMessage);
                        salt = field2;
                        fieldlist = field3;
                    }catch (Exception e){
                        salt = field3;
                        fieldlist = field2;
                    }
                }catch(Exception e1){
                    try{
                        field1.getByte(eventFMLChannels.iMessage);
                        salt = field1;
                        fieldlist = field2;
                    }catch (Exception e){
                        salt = field2;
                        fieldlist = field1;
                    }
                }

                fieldlist.setAccessible(true);
                salt.setAccessible(true);

                if (fieldlist != null){

                    try {
                        List<String> list = ((List<String>) fieldlist.get(eventFMLChannels.iMessage));

                        if (list.get(1).contains(".jar")) {

                            eventFMLChannels.setCancelled(true);
                            list.removeIf(inject ->
                                    inject.toString().endsWith(".tmp")
                            );
                            list.removeIf(mod ->
                                    mod.toString().toLowerCase().endsWith("-skipverify.jar")
                            );
                            eventFMLChannels.sendToServer(
                                    fwithc.newInstance(new ArrayList<>(list),
                                            salt.getByte(eventFMLChannels.iMessage))
                            );

                        }else{//classcheck

                            eventFMLChannels.setCancelled(true);
                            List<String> foundClassList = (List<String>) (fieldlist.get(eventFMLChannels.iMessage));

                            foundClassList.removeIf(clazz ->
                                    clazz.contains(this.getClass().getPackage().getName())
                            );
                            eventFMLChannels.sendToServer(
                                    fwithc.newInstance(new ArrayList<>(foundClassList),
                                            salt.getByte(eventFMLChannels.iMessage))
                            );
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }

            } catch (NoSuchMethodException eee) {
                List<String> list = new ArrayList<String>();
                try {//injectdetect check
                    Constructor<? extends IMessage> injectdetect = eventFMLChannels.iMessage.getClass().getConstructor(List.class);

                    try {
                        eventFMLChannels.setCancelled(true);
                        eventFMLChannels.sendToServer(injectdetect.newInstance(list));
                    } catch (InstantiationException | InvocationTargetException | IllegalAccessException ignored) {
                    }
                } catch (NoSuchMethodException e) {
                    try {
                        handleScreenshot(eventFMLChannels);
                    } catch (NoSuchMethodException noSuchMethodException) {

                        try {    //lighting and transparentTexture check
                            Constructor<? extends IMessage> datacheck = eventFMLChannels.iMessage.getClass().getConstructor(boolean.class, boolean.class);
                            if (datacheck != null) {
                                eventFMLChannels.setCancelled(true);
                                try {
                                    eventFMLChannels.sendToServer((IMessage) datacheck.newInstance(
                                            false, false));
                                } catch (Exception e1) {
                                }
                            }
                        } catch (NoSuchMethodException e1) {
                        }

                    }
                }
            }
        }
    }

    public void handleScreenshot(EventFMLChannels eventFMLChannels) throws NoSuchMethodException {
        // screenhost check
        Constructor<? extends IMessage> screenhost = eventFMLChannels.iMessage.getClass().getConstructor(boolean.class, byte[].class);
        if (screenhost != null) {

            eventFMLChannels.setCancelled(true);
            ByteArrayInputStream in = null;
            if (ScreenProtect.mode.isCurrentMode("leave") && mc.theWorld != null){
                mc.theWorld.sendQuittingDisconnectingPacket();
                mc.loadWorld(null);
                this.mc.displayGuiScreen(new GuiMainMenu());
            }

            if (ScreenProtect.mode.isCurrentMode("Custom")) {
                if (ScreenshotUtil.catanticheatImage != null) {
                    in = new ByteArrayInputStream(ScreenshotUtil.catanticheatImage);
                } else {
                    ScreenshotUtil.catanticheatImage = ScreenshotUtil.catAnticheatScreenhost();
                    in = new ByteArrayInputStream(ScreenshotUtil.catanticheatImage);
                }
            }

            try {
                byte[] networkData = new byte[32763];
                int size;
                while ((size = in.read(networkData)) >= 0) {
                    try {
                        if (networkData.length == size) {
                            eventFMLChannels.sendToServer(screenhost.newInstance(
                                    in.available() == 0, networkData));
                        } else {
                            eventFMLChannels.sendToServer(screenhost.newInstance(
                                    in.available() == 0, Arrays.copyOf(networkData, size)));
                        }
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException instantiationException) {
                    }
                }
            } catch (IOException evv) {
            }
        }
    }

}
