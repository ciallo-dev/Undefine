package cn.snowflake.rose.mod.mods.RENDER;

import cn.snowflake.rose.Client;
import cn.snowflake.rose.mod.Category;
import cn.snowflake.rose.mod.Module;
import cn.snowflake.rose.notification.Notification;
import cn.snowflake.rose.ui.CSGOGUI;
import cn.snowflake.rose.utils.Value;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class ClickGui extends Module {
    private CSGOGUI clickgui = null;
    public static Value<Double> r = new Value<Double>("ClickGui_Red", 255.0D, 0.0D,255.0D, 5.0D);
    public static Value<Double> g = new Value<Double>("ClickGui_Green", 95.0D, 0.0D, 255.0D, 5.0D);
    public static Value<Double> b = new Value<Double>("ClickGui_Blue", 205.0D, 0.0D, 255.0D, 5.0D);
    public static Value<String> mode = new Value<String>("ClickGui_Mode","Mode",0);
    public static Value<Boolean> info = new Value<>("ClickGui_Desc",true);

    public ClickGui() {
        super("ClickGui","Click Gui", Category.RENDER);
        this.setKey(Keyboard.KEY_RSHIFT);
        this.mode.mode.add("CSGO");
		this.mode.mode.add("Skeet");
        setChinesename("\u529f\u80fd\u7a97\u53e3");
    }

    @Override
    public String getDescription() {
        return "功能控制面板!";
    }

    @Override
    public void onEnable() {
        if(this.mode.isCurrentMode("CSGO")) {
            if(clickgui == null){
                clickgui = new CSGOGUI();
            }
            Minecraft.getMinecraft().displayGuiScreen(clickgui);
        }
        if(this.mode.isCurrentMode("Skeet")) {
            Minecraft.getMinecraft().displayGuiScreen(Client.getSkeetClickGui());
        }
        set(false);
    }

}