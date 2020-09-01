package cn.snowflake.rose.asm;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.function.BiConsumer;

import cn.snowflake.rose.Client;
import cn.snowflake.rose.events.impl.EventMove;
import cn.snowflake.rose.events.impl.EventPacket;
import cn.snowflake.rose.mod.mods.WORLD.Xray;
import cn.snowflake.rose.utils.asm.ASMUtil;
import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.types.EventType;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.swing.*;

public class ClassTransformer implements IClassTransformer, ClassFileTransformer,Opcodes{
	
	public static Set<String> classNameSet;
	private byte[] transformMethods(byte[] bytes, BiConsumer<ClassNode, MethodNode> transformer) {
		ClassReader classReader = new ClassReader(bytes);
		ClassNode classNode = new ClassNode();
		classReader.accept(classNode, 0);
		LogManager.getLogger().info("transform "+classNode.name);
		classNode.methods.forEach(m ->
					transformer.accept(classNode, m)

		);
		ClassWriter classWriter = new ClassWriter(0);
		classNode.accept(classWriter);
		return classWriter.toByteArray();
	}
	static {
		classNameSet = new HashSet<String>();
		String[] nameArray = new String[] {
				"net.minecraft.client.entity.EntityClientPlayerMP",
				"net.minecraft.client.Minecraft",
				"net.minecraft.network.NetworkManager",
				"net.minecraft.network.NetHandlerPlayServer",
				"net.minecraft.client.entity.EntityPlayerSP",
				"net.minecraft.block.Block",
				"net.minecraft.client.renderer.EntityRenderer",
				"net.minecraftforge.client.GuiIngameForge",
		};
		for (int i=0; i<nameArray.length; i++) {
			classNameSet.add(nameArray[i]);
		}
	}

	public static boolean needTransform(String name) {
		return classNameSet.contains(name);
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] classByte) {
		return transform(transformedName, classByte);
	}

	//  TODO SHIT OF runtimeDeobfuscationEnabled
	public static  boolean runtimeDeobfuscationEnabled = false;

//	public static  boolean runtimeDeobfuscationEnabled = false;

	public byte[] transform(String name, byte[] classByte) {
		try {
			if (name.equals("net.minecraft.client.Minecraft")) {
				Field field = LaunchClassLoader.class.getDeclaredField("transformers");
				field.setAccessible(true);
				List<IClassTransformer> transformers = (List<IClassTransformer>) field.get(net.minecraft.launchwrapper.Launch.classLoader);
				if (transformers.getClass().getName().equals("com.xue.vapu.ModList")) {
					Field needInterupt = transformers.getClass().getDeclaredField("needInterupt");
					needInterupt.set(transformers, new Boolean(false));
				}
				return transformMethods(classByte, this::transformMinecraft);
			}
			else if (name.equalsIgnoreCase("net.minecraft.client.renderer.EntityRenderer")){//3d
				return transformMethods(classByte, this::transformRenderEntityRenderer);
			}
			else if(name.equals("net.minecraft.client.entity.EntityPlayerSP")){  //fixed
				return  transformMethods(classByte, this::transformEntityPlayerSP);
			}
			else if (name.equalsIgnoreCase("net.minecraftforge.client.GuiIngameForge")){
				return this.transformMethods(classByte,this::transform2D);
			}
			else if (name.equals("net.minecraft.client.entity.EntityClientPlayerMP")) {
				return this.transformMethods(classByte,this::transformEntityClientPlayerMP);
			}
			else if (name.equalsIgnoreCase("net.minecraft.network.NetHandlerPlayServer")){
				return this.transformMethods(classByte,this::transformNetHandlerPlayServer);
			}
			else if (name.equalsIgnoreCase("net.minecraft.network.NetworkManager")){ //EventPacket
				return this.transformMethods(classByte,this::transformNetworkManager);
			}
			else if (name.equalsIgnoreCase("net.minecraft.block.Block")){
				return this.transformMethods(classByte,this::transformBlock);
			}
			else if (name.equalsIgnoreCase("luohuayu.anticheat.message.CPacketInjectDetect")){
				return this.transformMethods(classByte,this::transformCPacketInjectDetect);
			}
		}catch(Exception e) {
			LogManager.getLogger().log(Level.ERROR, ExceptionUtils.getStackTrace(e));
			
		}
		return classByte;
	}

	private void transformCPacketInjectDetect(ClassNode classNode, MethodNode methodNode) {
		if (methodNode.name.equalsIgnoreCase("<init>") && methodNode.desc.equalsIgnoreCase("(Ljava/util/List<Ljava/lang/String;>;)V")){
			Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
			if (iterator.hasNext()){
				AbstractInsnNode insnNode = (AbstractInsnNode) iterator.next();
				if (insnNode instanceof VarInsnNode && insnNode.getOpcode() == ALOAD &&
						insnNode.getNext() instanceof VarInsnNode && insnNode.getNext().getOpcode() == ALOAD &&
						insnNode.getNext().getNext() instanceof FieldInsnNode && insnNode.getNext().getNext().getOpcode() == PUTFIELD){

					methodNode.instructions.remove(insnNode.getNext().getNext());
					methodNode.instructions.remove(insnNode.getNext());
					methodNode.instructions.remove(insnNode);
					JOptionPane.showConfirmDialog(null,"猫反已击杀","提示",1);
				}
			}
		}
	}

	private void transformBlock(ClassNode classNode, MethodNode methodNode) {
		if (methodNode.name.equalsIgnoreCase("shouldSideBeRendered") || methodNode.name.equalsIgnoreCase("func_149646_a")){
			LogManager.getLogger().info(methodNode.name);
			final InsnList insnList = new InsnList();
			insnList.add(new MethodInsnNode(INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "isXrayEnabled", "()Z", false));
			LabelNode jmp = new LabelNode();
			insnList.add(new JumpInsnNode(IFEQ,jmp));
			insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Xray.class), "block", "Ljava/util/ArrayList;"));
			insnList.add(new VarInsnNode(ALOAD,0));// == this
			insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/ArrayList", "contains", "(Ljava/lang/Object;)Z", false));
			insnList.add(new InsnNode(IRETURN));
			insnList.add(jmp);
			insnList.add(new FrameNode(F_SAME, 0, null, 0, null));
			methodNode.instructions.insert(insnList);
		}
	}
	//transformNetworkManager start
	private void transformNetworkManager(ClassNode classNode, MethodNode methodNode) {
		if (methodNode.name.equalsIgnoreCase("channelRead0")){
			LogManager.getLogger().info(methodNode.name);
			final InsnList preInsn = new InsnList();
			preInsn.add(new VarInsnNode(Opcodes.ALOAD, 2));
			preInsn.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/darkmagician6/eventapi/types/EventType", "PRE", "Lcom/darkmagician6/eventapi/types/EventType;"));
			preInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(this.getClass()), "channelRead0Hook","(Ljava/lang/Object;Lcom/darkmagician6/eventapi/types/EventType;)Z", false));
			final LabelNode jmp = new LabelNode();
			preInsn.add(new JumpInsnNode(Opcodes.IFEQ, jmp));
			preInsn.add(new InsnNode(Opcodes.RETURN));
			preInsn.add(jmp);
			preInsn.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
			methodNode.instructions.insert(preInsn);

			/**
			 * if(channelRead0Hook(packet,EventType.PRE)){
			 *  return ;
			 * }
			 */

			final InsnList postInsn = new InsnList();
			postInsn.add(new VarInsnNode(Opcodes.ALOAD, 2));
			postInsn.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/darkmagician6/eventapi/types/EventType", "POST", "Lcom/darkmagician6/eventapi/types/EventType;"));
			postInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(this.getClass()), "channelRead0Hook","(Ljava/lang/Object;Lcom/darkmagician6/eventapi/types/EventType;)Z", false));
			preInsn.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
			methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), postInsn);

			/**
			 * if(channelRead0Hook(packet,EventType.POST)){
			 *      return;
			 * }
			 */
		}
	}
	public static boolean channelRead0Hook(Object packet, EventType eventType) {
		if(packet != null) {
			final EventPacket event = new EventPacket(eventType,packet);
			EventManager.call(event);
			return event.isCancelled();
		}
		return false;
	}
	private void transformNetHandlerPlayServer(ClassNode classNode, MethodNode methodNode) {
		InsnList nodelist = methodNode.instructions;
		if (methodNode.name.equalsIgnoreCase("func_147360_c") || methodNode.name.equalsIgnoreCase("kickPlayerFromServer")){
			InsnList insnList = new InsnList();
			insnList.add(new VarInsnNode(ALOAD,1));
			insnList.add(new LdcInsnNode("Illegal stance"));
			insnList.add(new MethodInsnNode(INVOKEVIRTUAL,"java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false));
			LabelNode l1 = new LabelNode();
			insnList.add(new JumpInsnNode(IFEQ,l1));
			insnList.add(new InsnNode(RETURN));
			insnList.add(l1);
			insnList.add(new FrameNode(F_SAME, 4, null, 0, null));
			methodNode.instructions.insertBefore(nodelist.getFirst().getNext(),insnList);
		}
	}



	private void transformRenderEntityRenderer(ClassNode classNode, MethodNode method) {
		if ((method.name.equalsIgnoreCase("renderWorld") || method.name.equalsIgnoreCase("func_78471_a") )) {
			
			Iterator<AbstractInsnNode> iter = method.instructions.iterator();
			while (iter.hasNext()) {
				AbstractInsnNode insn = iter.next();
				if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
					MethodInsnNode methodInsn = (MethodInsnNode) insn;
					if (methodInsn.name.equals("dispatchRenderLast")) {
						method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "Event3D", "()V", false));
					}
				}
			}
		}
		if ((method.name.equalsIgnoreCase("orientCamera") || method.name.equalsIgnoreCase("func_78467_g")) && method.desc.equalsIgnoreCase("(F)V")){
			
			AbstractInsnNode target = ASMUtil.findMethodInsn(method, INVOKEVIRTUAL,"net/minecraft/util/Vec3", runtimeDeobfuscationEnabled ?"func_72438_d" : "distanceTo","(Lnet/minecraft/util/Vec3;)D");
			if (target != null){
				InsnList insnList2 = new InsnList();

				InsnList insnList = new InsnList();
				insnList.add(new MethodInsnNode(INVOKESTATIC,"cn/snowflake/rose/asm/MinecraftHook","isViewClipEnabled","()Z",false));
				LabelNode labelNode = new LabelNode();
				insnList.add(new JumpInsnNode(IFNE,labelNode));
				method.instructions.insertBefore(ASMUtil.forward(target,8)
						,insnList);
				insnList2.add(labelNode);
				insnList2.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
				method.instructions.insert(ASMUtil.forward(target,13)
						,insnList2);
				//   dump net.minecraft.client.renderer.EntityRenderer
				// jad net.minecraft.client.renderer.EntityRenderer
			}
		}
	}

	private void transform2D(ClassNode clazz, MethodNode method) {
		Iterator<AbstractInsnNode> iter = method.instructions.iterator();
		while (iter.hasNext()) {
			AbstractInsnNode insn = iter.next();
			if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode methodInsn = (MethodInsnNode) insn;
				if (methodInsn.name.equals("renderSleepFade")){
					
					method.instructions.insert(insn,new MethodInsnNode(Opcodes.INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "Event2D", "()V", false));
				}
			}
		}
	}

	//Minecraft start
	private void transformMinecraft(ClassNode clazz, MethodNode method) {
		Iterator<AbstractInsnNode> iter = method.instructions.iterator();
		while (iter.hasNext()) {
			AbstractInsnNode insn = iter.next();
			if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode methodInsn = (MethodInsnNode) insn;
				if (methodInsn.name.equals("func_71407_l") || methodInsn.name.equals("runTick")){
					method.instructions.insert(insn,new MethodInsnNode(Opcodes.INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "runClient", "()V", false));
				}
				if (methodInsn.name.equals("func_152348_aa")) {
					method.instructions.insert(insn,new MethodInsnNode(Opcodes.INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "runTick", "()V", false));
				}
			}
		}
	}
	//minecraft end

	//noslow start
	private void transformEntityPlayerSP(ClassNode clazz, MethodNode method) {
		if (method.name.equalsIgnoreCase("func_70636_d") || method.name.equalsIgnoreCase("onLivingUpdate")) {
			Iterator<AbstractInsnNode> iter = method.instructions.iterator();
			while (iter.hasNext()) {
				AbstractInsnNode insn = iter.next();
				if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
					MethodInsnNode methodInsn = (MethodInsnNode) insn;
					if (methodInsn.name.equals("updatePlayerMoveState") || methodInsn.name.equals("func_78898_a")) {
						method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "onNoSlowEnable", "()V", false));
					}
					if (methodInsn.name.equals("func_145771_j")) {
						method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "onToggledTimerZero", "()V", false));
					}
				}
			}
		}
//		MethodNode mainMethod = new MethodNode( ACC_PUBLIC, runtimeDeobfuscationEnabled ? "func_70091_d" : "moveEntity", "(DDD)V", null, null);
//		mainMethod.instructions.add(new TypeInsnNode(NEW, "cn/snowflake/rose/events/impl/EventMove"));
//		mainMethod.instructions.add(new InsnNode(DUP));
//		mainMethod.instructions.add(new VarInsnNode(DLOAD,1));
//		mainMethod.instructions.add(new VarInsnNode(DLOAD,3));
//		mainMethod.instructions.add(new VarInsnNode(DLOAD,5));
//		mainMethod.instructions.add(new MethodInsnNode(INVOKESPECIAL, "cn/snowflake/rose/events/impl/EventMove", "<init>", "(DDD)V", false));
//		mainMethod.instructions.add(new VarInsnNode(ASTORE,7));
//
//		mainMethod.instructions.add(new VarInsnNode(ALOAD,7));
//		mainMethod.instructions.add(new MethodInsnNode(INVOKESTATIC, "com/darkmagician6/eventapi/EventManager", "call", "(Lcom/darkmagician6/eventapi/events/Event;)Lcom/darkmagician6/eventapi/events/Event;", false));
//		mainMethod.instructions.add(new InsnNode(POP));
//
//		mainMethod.instructions.add(new VarInsnNode(ALOAD,7));
//		mainMethod.instructions.add(new FieldInsnNode(GETFIELD, "cn/snowflake/rose/events/impl/EventMove", "x", "D"));
//		mainMethod.instructions.add(new VarInsnNode(DSTORE,1));
//
//		mainMethod.instructions.add(new VarInsnNode(ALOAD,7));
//		mainMethod.instructions.add(new FieldInsnNode(GETFIELD, "cn/snowflake/rose/events/impl/EventMove", "y", "D"));
//		mainMethod.instructions.add(new VarInsnNode(DSTORE,3));
//
//		mainMethod.instructions.add(new VarInsnNode(ALOAD,7));
//		mainMethod.instructions.add(new FieldInsnNode(GETFIELD, "cn/snowflake/rose/events/impl/EventMove", "z", "D"));
//		mainMethod.instructions.add(new VarInsnNode(DSTORE,5));
//
//
//		mainMethod.instructions.add(new VarInsnNode(ALOAD,0));
//		mainMethod.instructions.add(new VarInsnNode(DLOAD,1));
//		mainMethod.instructions.add(new VarInsnNode(DLOAD,3));
//		mainMethod.instructions.add(new VarInsnNode(DLOAD,5));
//		mainMethod.instructions.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/client/entity/AbstractClientPlayer", runtimeDeobfuscationEnabled ? "func_70091_d" : "moveEntity", "(DDD)V", false));
//		mainMethod.instructions.add(new InsnNode(Opcodes.RETURN));
//		clazz.methods.add(mainMethod);
	}
	private void transformEntityClientPlayerMP(ClassNode clazz, MethodNode method) {
		if (method.name.equals("onUpdate") || method.name.equals("func_70071_h_") ) {
			method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "onUpdate", "()V", false));
		}
		if (method.name.equalsIgnoreCase("sendMotionUpdates") || method.name.equalsIgnoreCase("func_71166_b")){


			InsnList preInsn = new InsnList();
			preInsn.add(new FieldInsnNode(GETSTATIC, "com/darkmagician6/eventapi/types/EventType", "PRE", "Lcom/darkmagician6/eventapi/types/EventType;"));
			preInsn.add(new MethodInsnNode(INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "onUpdateWalkingPlayerHook","(Lcom/darkmagician6/eventapi/types/EventType;)V", false));
			method.instructions.insert(preInsn);


			//EventMotionPost2
			InsnList postInsn = new InsnList();
			postInsn.add(new FieldInsnNode(GETSTATIC, "com/darkmagician6/eventapi/types/EventType", "POST", "Lcom/darkmagician6/eventapi/types/EventType;"));
			postInsn.add(new MethodInsnNode(INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "onUpdateWalkingPlayerHook","(Lcom/darkmagician6/eventapi/types/EventType;)V", false));
			method.instructions.insertBefore(ASMUtil.bottom(method), postInsn);
		}
		if (method.name.equalsIgnoreCase("func_71165_d") || method.name.equalsIgnoreCase("sendChatMessage")) {

			final InsnList insnList = new InsnList();
			insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
			insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "command", "(Ljava/lang/String;)V", false));

			insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
			insnList.add(new LdcInsnNode("-"));
			insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "cn/snowflake/rose/asm/MinecraftHook", "isNoCommandEnabled", "(Ljava/lang/String;Ljava/lang/String;)Z", false));

			final LabelNode jmp = new LabelNode();
			insnList.add(new JumpInsnNode(Opcodes.IFEQ, jmp));
			insnList.add(new InsnNode(Opcodes.RETURN));
			insnList.add(jmp);
			insnList.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
			method.instructions.insert(insnList);
		}
	}


	@Override
	public byte[] transform(ClassLoader arg0, String name, Class<?> clazz, ProtectionDomain arg3, byte[] classByte)
			throws IllegalClassFormatException {
		return transform(clazz.getName(), classByte);
	}


}