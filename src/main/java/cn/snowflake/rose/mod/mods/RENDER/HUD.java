package cn.snowflake.rose.mod.mods.RENDER;

import cn.snowflake.rose.Client;
import cn.snowflake.rose.NativeMethod;
import cn.snowflake.rose.events.impl.EventRender2D;
import cn.snowflake.rose.mod.Category;
import cn.snowflake.rose.mod.Module;
import cn.snowflake.rose.utils.Value;
import cn.snowflake.rose.utils.other.JReflectUtility;
import cn.snowflake.rose.utils.render.RenderUtil;
import cn.snowflake.rose.utils.render.UnicodeFontRenderer;
import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.injection.ClientLoader;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;


import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class HUD extends Module {
    public static Value<String> text = new Value<>("HUD_Text","","Season");
    public static Value<String> rainbow = new Value<>("HUD","ColorMode",0);
    public static Value<Double> rainbowindex = new Value<Double>("HUD_rainbow", 1.0, 1.0, 20.0, 1.0);
    public static Value<Boolean> logo = new Value<>("HUD_Logo",true);
    public static Value<Boolean> info = new Value<>("HUD_Info",false);


    public HUD() {
        super("HUD","HUD", Category.RENDER);
        this.rainbow.addValue("Gray");
        this.rainbow.addValue("Rainbow");
        this.rainbow.addValue("Green");
        setChinesename("");
    }

    @Override
    public String getDescription() {
        return "功能显示!";
    }


    @EventTarget
    public void on2D(EventRender2D e){
        UnicodeFontRenderer font = Client.instance.fontManager.simpleton12;
        if (this.info.getValueState()) {
            ScaledResolution sr = new ScaledResolution(mc,mc.displayWidth,mc.displayHeight);
            String xyz = "\247bX: \247f" + (int) mc.thePlayer.posX + " \247bY: \247f" + (int) mc.thePlayer.posY + " \247bZ: \247f" + (int) mc.thePlayer.posZ;
            NativeMethod.method1();
            font.drawStringWithColor(xyz, sr.getScaledWidth() - font.getStringWidth(clean(xyz)) - 6, sr.getScaledHeight() - font.FONT_HEIGHT - (mc.currentScreen instanceof GuiChat ? 15 : 0), -1,0);
        }

        if (this.logo.getValueState()){
            Date date = new Date();
            SimpleDateFormat sdformat = new SimpleDateFormat("KK:mm a", Locale.ENGLISH);
            String result = sdformat.format(date);
            String server = mc.isSingleplayer() ? "local_server" : mc.func_147104_D().serverIP.toLowerCase();//getCurrentServerData
            String text = null;
            String text2 = null;
            try {
                text2 = JReflectUtility.getField(mc.getClass(), ClientLoader.runtimeDeobfuscationEnabled ? "field_71470_ab": "debugFPS",true).getInt(mc) + " fps | "+" username : "+Client.username +" | " +result +" | " + server;
                text = this.text.getText()+"\2472sense\247f | " +text2;
            } catch (IllegalAccessException illegalAccessException) {
                illegalAccessException.printStackTrace();
            }
            float width = Client.instance.fontManager.simpleton11.getStringWidth(text);
            int height = Client.instance.fontManager.simpleton11.FONT_HEIGHT + 9;
            int posX = 2;
            int posY = 2;
            RenderUtil.drawRect(posX, posY, posX + width + 2, posY + height, new Color(5, 5, 5, 150).getRGB());
//            RenderUtil.drawBorderedRect(posX + .5, posY + .5, posX + width + 1.5, posY + height - .5, 0.5, new Color(40, 40, 40, 255).getRGB(), new Color(60, 60, 60, 255).getRGB(), true);
//            RenderUtil.drawBorderedRect(posX + 2, posY + 2, posX + width, posY + height - 2, 0.5, new Color(22, 22, 22, 255).getRGB(), new Color(60, 60, 60, 255).getRGB(), true);
//            RenderUtil.drawRect(posX + 2.5, posY + 2.5, posX + width - .5, posY + 4.5, new Color(9, 9, 9, 255).getRGB());

            RenderUtil.drawGradientRect(4, posY + 3, 4 + (width / 3), posY + 4,true,
                    rainbow(100),
                    rainbow(1000));
            RenderUtil.drawGradientRect(4 + (width / 3), posY + 3, 4 + ((width / 3) * 2), posY + 4,true,
                    rainbow(1000),
                    rainbow(1900));
            RenderUtil.drawGradientRect(4 + ((width / 3) * 2), posY + 3, ((width / 3) * 3) + 1, posY + 4,true,
                    rainbow(1900),
                    rainbow(2800));


            Client.instance.fontManager.simpleton11.drawStringWithColor(text, 2 + posX, 5 + posY, -1,0);
        }
        RenderArraylist();
    }
    public static String clean(String text) {
        String cleaned = text.replaceAll("§a", "");
        cleaned = cleaned.replaceAll("§b", "");
        cleaned = cleaned.replaceAll("§c", "");
        cleaned = cleaned.replaceAll("§d", "");
        cleaned = cleaned.replaceAll("§e", "");
        cleaned = cleaned.replaceAll("§f", "");
        cleaned = cleaned.replaceAll("§0", "");
        cleaned = cleaned.replaceAll("§1", "");
        cleaned = cleaned.replaceAll("§2", "");
        cleaned = cleaned.replaceAll("§3", "");
        cleaned = cleaned.replaceAll("§4", "");
        cleaned = cleaned.replaceAll("§5", "");
        cleaned = cleaned.replaceAll("§6", "");
        cleaned = cleaned.replaceAll("§7", "");
        cleaned = cleaned.replaceAll("§8", "");
        cleaned = cleaned.replaceAll("§9", "");
        return cleaned;
    }

    public static int rainbow(int delay) {
        double rainbow = Math.ceil((double)((double)(System.currentTimeMillis() + (long)delay) / 5.0));
        return Color.getHSBColor((float)((float)((rainbow %= 720.0) / 720.0)), (float)0.5f, (float)0.7f).brighter().getRGB();
    }
    public static Color rainbow1(long time, float count, float fade) {
        long color = Long.parseLong(Integer.toHexString(Color.HSBtoRGB((time + count * -3000000f) / 2 / 1.0E9f, 0.6f, 0.9f)), 16);
        Color c = new Color((int)color);
        return new Color((float)c.getRed() / 255.0F * fade, (float)c.getGreen() / 255.0F * fade, (float)c.getBlue() / 255.0F * fade, (float)c.getAlpha() / 255.0F);
    }
    public static Color rainbow(long time, float count, float fade) {
        float hue = ((float)time + (1.0F + count) * 2.0E8F) /(HUD.rainbowindex.getValueState().intValue() * 1.0E9F) % 1.0F;
        long color = Long.parseLong(Integer.toHexString(Integer.valueOf(Color.HSBtoRGB(hue, 0.5F, 1.0F)).intValue()), 16);
        Color c = new Color((int)color);
        return new Color((float)c.getRed() / 255.0F * fade, (float)c.getGreen() / 255.0F * fade, (float)c.getBlue() / 255.0F * fade, (float)c.getAlpha() / 255.0F);
    }

    private void RenderArraylist() {
        ScaledResolution sr = new ScaledResolution(mc,mc.displayWidth,mc.displayHeight);
        UnicodeFontRenderer arraylistfont = Client.chinese ? Client.instance.fontManager.wqy19 :Client.instance.fontManager.robotoregular19;
        ArrayList<Module> mods = new ArrayList<>(Client.instance.modManager.getModList());


//        mods.sort(new Comparator<Module>() {
//            @Override
//            public int compare(Module m1, Module m2) {
//
//                String mName = m1.getdisplayName() == null ? m1.getRenderName() :  m1.getRenderName()+ " "+ EnumChatFormatting.GRAY +m1.getdisplayName();
//                String m1Name = m2.getdisplayName() == null ? m2.getRenderName() :  m2.getRenderName()+ " "+ EnumChatFormatting.GRAY +m2.getdisplayName();
//
//                return font.getStringWidth(String.valueOf(mName)) - font.getStringWidth(String.valueOf(m1Name));
//            }});


          mods.sort(Comparator.comparingDouble(m1 -> - arraylistfont.getStringWidthnew(m1.getRenderName() + (m1.getdisplayName() == null ? "" : " " + m1.getdisplayName()))));



        int countMod = 0;
        int color = -1;
        float yAxis = 0;
        for (Module m2 : mods) {
            if (m2.hidden)continue;
            ++countMod;
            Color col2 = new Color(rainbow(System.nanoTime(), (float) countMod, 1).getRGB());
            if(m2.isEnabled()) {
                switch (rainbow.getModeName()){
                    case "Rainbow" :
                        int rainbowCol = rainbow1(System.nanoTime() + 400l, (float) yAxis * 2, 1.1F).getRGB();
                        int c1 = rainbowCol;
                        Color col = new Color(c1);
                        color = new Color(col.getRed(), col.getGreen(), col.getBlue()).brighter().getRGB();
                        break;
                    case "Gray":
                        color = (new Color(col2.getRed() / 2, col2.getRed() / 2, col2.getRed() / 2)).getRGB();
                        break;
                    case "Green":
                        color = new Color(44, 255, 0).getRGB();
                        break;
                }

                String disname = m2.getdisplayName() == null ? "" : " " + m2.getdisplayName();
                arraylistfont.drawStringWithShadow(m2.getRenderName(), sr.getScaledWidth() - arraylistfont.getStringWidthnew(m2.getRenderName() + disname) -3, yAxis ,color);
                arraylistfont.drawStringWithShadow(disname, sr.getScaledWidth() - arraylistfont.getStringWidthnew(disname) - 3, yAxis,new Color(166,168,168).getRGB());

                yAxis += 12F;
            }
        }
    }

}