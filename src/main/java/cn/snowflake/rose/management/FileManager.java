package cn.snowflake.rose.management;

import cn.snowflake.rose.Client;
import cn.snowflake.rose.mod.Module;
import cn.snowflake.rose.mod.mods.WORLD.Spammer;
import cn.snowflake.rose.mod.mods.WORLD.Xray;
import cn.snowflake.rose.notification.Notification;
import cn.snowflake.rose.utils.Value;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.io.*;

public class FileManager {
    private Minecraft mc = Minecraft.getMinecraft();
    private String fileDir;

    public FileManager() {
        this.fileDir = this.mc.mcDataDir.getAbsolutePath() + "/Season";
        File fileFolder = new File(this.fileDir);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }

        try {
            this.loadKeys();
            this.loadValues();
            this.loadMods();
            this.loadBlocks();
            this.loadHidden();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void saveSpammerText() {
        File f = new File(fileDir + "/spammertext.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            PrintWriter pw = new PrintWriter(f);
            pw.print(Spammer.customtext);
            pw.close();
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }

    public void loadSpammerText() {
        try{
            File f = new File(fileDir + "/spammertext.txt");
            if (!f.exists()) {
                f.createNewFile();
            } else {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while((line = br.readLine()) != null) {
                    try {
                        String name = String.valueOf(line);
                        Spammer.customtext = name;
                    } catch (Exception var4) {
                        ;
                    }
                }
            }
        }catch (Exception e){
        }
    }

    public void loadBlocks() throws IOException {

        File f = new File(String.valueOf((Object)this.fileDir) + "/blocks.txt");
        if (!f.exists()) {
            f.createNewFile();
        } else {
            String line;
            BufferedReader br = new BufferedReader((Reader)new FileReader(f));
            while ((line = br.readLine()) != null) {
                if (!line.contains((CharSequence)":")) continue;
                String[] split = line.split(":");
                String id = split[1];
                Block block = Block.getBlockById(Integer.parseInt(id));
                Xray.block.add(block);
            }
        }
    }

    public void saveBlocks() {
        File f = new File(String.valueOf((Object)this.fileDir) + "/blocks.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            PrintWriter pw = new PrintWriter(f);
            for (Block b : Xray.block) {
                pw.print(String.valueOf((Object)b.getLocalizedName()) + ":" + Block.getIdFromBlock(b) + "\n");
            }
            pw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveKeys() {
        File f = new File(String.valueOf((Object)this.fileDir) + "/keys.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            PrintWriter pw = new PrintWriter(f);
            for (Module m : Client.instance.modManager.getModList()) {
                String keyName = m.getKey() < 0 ? "None" : Keyboard.getKeyName((int)m.getKey());
                pw.write(String.valueOf((Object)m.getName()) + ":" + keyName + "\n");
            }
            pw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadKeys() throws IOException {
        File f = new File(String.valueOf((Object)this.fileDir) + "/keys.txt");
        if (!f.exists()) {
            f.createNewFile();
        } else {
            String line;
            BufferedReader br = new BufferedReader((Reader)new FileReader(f));
            while ((line = br.readLine()) != null) {
                if (!line.contains((CharSequence)":")) continue;
                String[] split = line.split(":");
                Module m = ModManager.getModByName((String)split[0]);
                int key = Keyboard.getKeyIndex((String)split[1]);
                if (m == null || key == -1) continue;
                m.setKey(key);
            }

        }
    }

    public void saveMods() {
        File f = new File(String.valueOf((Object)this.fileDir) + "/mods.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            PrintWriter pw = new PrintWriter(f);
            for (Module m : Client.instance.modManager.getModList()) {
                pw.print(String.valueOf((Object)m.getName()) + ":" + m.isEnabled() + "\n");
            }
            pw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMods() throws IOException {
        File f = new File(String.valueOf((Object)this.fileDir) + "/mods.txt");
        if (!f.exists()) {
            f.createNewFile();
        } else {
            String line;
            BufferedReader br = new BufferedReader((Reader)new FileReader(f));
            while ((line = br.readLine()) != null) {
                if (!line.contains((CharSequence)":")) continue;
                String[] split = line.split(":");
                Module m = ModManager.getModByName((String)split[0]);
                if (m == null) continue;
                boolean state = Boolean.parseBoolean((String)split[1]);
                if (m.isEnabled() != state)
                m.set(state, false);
            }
        }
    }

    public void saveValues() {
        File f = new File(String.valueOf((Object)this.fileDir) + "/values.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            PrintWriter pw = new PrintWriter(f);
            for (Value value : Value.list) {
                String valueName = value.getValueName();
                if (value.isValueBoolean) {
                    pw.print(String.valueOf((Object)valueName) + ":b:" + value.getValueState() + "\n");
                    continue;
                }
                if (value.isValueDouble) {
                    pw.print(String.valueOf((Object)valueName) + ":d:" + value.getValueState() + "\n");
                    continue;
                } if (value.isValueString) {
                    pw.print(String.valueOf((Object)valueName) + ":m:" + value.getText() + "\n");
                    continue;
                }
                if (!value.isValueMode) continue;
                pw.print(String.valueOf((Object)valueName) + ":s:" + value.getModeTitle() + ":" + value.getCurrentMode() + "\n");
            }
            pw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadValues() throws IOException {
        File f = new File(String.valueOf((Object)this.fileDir) + "/values.txt");
        if (!f.exists()) {
            f.createNewFile();
        } else {
            String line;
            BufferedReader br = new BufferedReader((Reader)new FileReader(f));
            while ((line = br.readLine()) != null) {
                if (!line.contains((CharSequence)":")) continue;
                String[] split = line.split(":");
                for (Value value : Value.list) {
                    if (!split[0].equalsIgnoreCase(value.getValueName())) continue;
                    if (value.isValueBoolean && split[1].equalsIgnoreCase("b")) {
                        value.setValueState((Object)Boolean.parseBoolean((String)split[2]));
                        continue;
                    }
                    if (value.isValueDouble && split[1].equalsIgnoreCase("d")) {
                        value.setValueState((Object)Double.parseDouble((String)split[2]));
                        continue;
                    }
                    if (value.isValueString && split[1].equalsIgnoreCase("m")) {
                        value.setText(split[2]);
                        continue;
                    }
                    if (!value.isValueMode || !split[1].equalsIgnoreCase("s") || !split[2].equalsIgnoreCase(value.getModeTitle())) continue;
                    value.setCurrentMode(Integer.parseInt((String)split[3]));
                }
            }
        }
    }

    public void saveHidden() {
        File f = new File(String.valueOf((Object)this.fileDir) + "/hidden.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            PrintWriter pw = new PrintWriter(f);
            for (Module m : Client.instance.modManager.getModList()) {
                pw.print(String.valueOf((Object)m.getName()) + ":" + m.isHidden() + "\n");
            }
            pw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadHidden() throws IOException {
        File f = new File(String.valueOf((Object)this.fileDir) + "/hidden.txt");
        if (!f.exists()) {
            f.createNewFile();
        } else {
            String line;
            BufferedReader br = new BufferedReader((Reader)new FileReader(f));
            while ((line = br.readLine()) != null) {
                if (!line.contains((CharSequence)":")) continue;
                String[] split = line.split(":");
                Module m = ModManager.getModByName((String)split[0]);
                boolean state = Boolean.parseBoolean((String)split[1]);
                if (m == null) continue;
                m.setHidden(state);
            }
        }
    }

    public void saveConfig() {
        File f = new File(System.getProperty("user.home") + File.separator + "seasoncfg.cfg");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            PrintWriter pw = new PrintWriter(f);
            for (Module m : Client.instance.modManager.getModList()) {
                pw.print(String.valueOf((Object)m.getName()) + ":mod:" + m.isEnabled() + "\n");
            }

            for (Module m : Client.instance.modManager.getModList()) {
                String keyName = m.getKey() < 0 ? "None" : Keyboard.getKeyName((int)m.getKey());
                pw.write(String.valueOf((Object)m.getName()) + ":key:" + keyName + "\n");
            }

            for (Value value : Value.list) {
                String valueName = value.getValueName();
                if (value.isValueBoolean) {
                    pw.print(String.valueOf((Object) valueName) + ":b:" + value.getValueState() + "\n");
                    continue;
                }
                if (value.isValueDouble) {
                    pw.print(String.valueOf((Object) valueName) + ":d:" + value.getValueState() + "\n");
                    continue;
                }
                 if (value.isValueString) {
                    pw.print(String.valueOf((Object)valueName) + ":m:" + value.getText() + "\n");
                    continue;
                  }
                if (!value.isValueMode)
                    continue;
                pw.print(String.valueOf((Object) valueName) + ":s:" + value.getModeTitle() + ":"
                        + value.getCurrentMode() + "\n");
            }
            pw.close();
            Client.instance.getNotificationManager().addNotification("Config","\247aSave config to your system !", Notification.Type.SUCCESS);
        } catch (Exception e) {
            Client.instance.getNotificationManager().addNotification("Config","\247cSaved Error !", Notification.Type.SUCCESS);
//            e.printStackTrace();
        }
    }

    public void loadConfig() {
        File f = new File(System.getProperty("user.home") + File.separator + "seasoncfg.cfg");
        try {
            String line2;
            BufferedReader br2 = new BufferedReader((Reader) new FileReader(f));
            while ((line2 = br2.readLine()) != null) {
                if (!line2.contains((CharSequence) ":mod:") && !line2.contains((CharSequence) ":key:") && !line2.contains((CharSequence) ":"))continue;
                String[] splits = line2.split(":mod:");
                Module m = ModManager.getModByName((String) splits[0]);
                if(m == null)continue;
                boolean state = (Boolean.parseBoolean((String) splits[1]));
                if (m.isEnabled() != state)
                    m.set(state);
            }

            String key;
            BufferedReader brkey = new BufferedReader((Reader)new FileReader(f));
            while ((key = brkey.readLine()) != null) {
                if (!key.contains((CharSequence)":key:")) continue;
                String[] split = key.split(":key:");
                Module m = ModManager.getModByName((String)split[0]);
                int k = Keyboard.getKeyIndex((String)split[1]);
                if (m == null || k == -1) continue;
                m.setKey(k);
            }

            String line;
            BufferedReader br = new BufferedReader((Reader) new FileReader(f));
            while ((line = br.readLine()) != null) {
                if (!line.contains((CharSequence) ":"))
                    continue;
                String[] split = line.split(":");
                for (Value value : Value.list) {
                    if (!split[0].equalsIgnoreCase(value.getValueName()))
                        continue;
                    if (value.isValueBoolean && split[1].equalsIgnoreCase("b")) {
                        value.setValueState((Object) Boolean.parseBoolean((String) split[2]));
                        continue;
                    }
                    if (value.isValueDouble && split[1].equalsIgnoreCase("d")) {
                        value.setValueState((Object) Double.parseDouble((String) split[2]));
                        continue;
                    }
                    if (!value.isValueMode || !split[1].equalsIgnoreCase("s")
                            || !split[2].equalsIgnoreCase(value.getModeTitle()))
                        continue;
                    value.setCurrentMode(Integer.parseInt((String) split[3]));
                }
            }
            Client.instance.getNotificationManager().addNotification("Config", "\u00a7a" +"Loaded Successfully !", Notification.Type.SUCCESS);
            saveMods();
            saveValues();
            saveKeys();
        } catch (IOException e) {
            Client.instance.getNotificationManager().addNotification("Config", "\u00a7c" +"You did not save the  !", Notification.Type.ERROR);
//            e.printStackTrace();
        }
    }




}
