package cn.snowflake.rose.transform.transforms;

import cn.snowflake.rose.mod.mods.WORLD.Xray;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class TransformTessellator implements Opcodes {

    public static void transformTessellator(ClassNode classNode, MethodNode methodNode) {
        if (methodNode.name.equalsIgnoreCase("setColorRGBA") || methodNode.name.equalsIgnoreCase("func_78370_a")){
            InsnList insnList = new InsnList();
            insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(TransformBlock.class), "isXrayEnabled", "()Z", false));
            LabelNode labelNode = new LabelNode();
            insnList.add(new JumpInsnNode(IFEQ,labelNode));
            insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(TransformTessellator.class), "getOpacity", "()I", false));
            insnList.add(new VarInsnNode(ISTORE,4));
            insnList.add(labelNode);
            methodNode.instructions.insert(insnList);
        }
    }
    public static int getOpacity(){
        return Xray.OPACITY.getValueState().intValue();
    }

}
