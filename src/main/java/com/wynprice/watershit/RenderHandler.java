package com.wynprice.watershit;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import java.util.ArrayList;
import java.util.Random;

import javax.vecmath.Vector4f;

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.DoubleHolder;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class RenderHandler implements Runnable
{
	static int localPosX, localPosY, localPosZ, prevLocalPosX, prevLocalPosZ;
		
	private static Thread thread = null;
	private static long nextTimeMs = System.currentTimeMillis();
	private static final int delayMs = 200;
	
	private static float partialTicks;
	
	private static ArrayList<BlockPos> finalBlockPos = new ArrayList<>();
	
	@SubscribeEvent
	public void clientTick(ClientTickEvent event)
	{
		EntityPlayer player = Minecraft.getMinecraft().player;
		if ( (event.phase == TickEvent.Phase.END) && (player != null) )
		{
			localPosX = MathHelper.floor(player.posX);
			localPosY = MathHelper.floor(player.posY);
			localPosZ = MathHelper.floor(player.posZ);
			prevLocalPosX = MathHelper.floor(player.prevPosX);
			prevLocalPosZ = MathHelper.floor(player.prevPosZ);

			if(((this.thread == null) || !this.thread.isAlive()) && (player.world != null) && (player != null))
			{
				this.thread = new Thread(this);
				this.thread.setDaemon(false);
				this.thread.setPriority(Thread.MAX_PRIORITY);
				this.thread.start();
			}
		}
	}
	
	
	@SubscribeEvent
	public void renderWorldLast(RenderWorldLastEvent event)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		World world = Minecraft.getMinecraft().world;
        GlStateManager.enableBlend();
//        GlStateManager.enableLighting();
        GlStateManager.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();
        GlStateManager.shadeModel(org.lwjgl.opengl.GL11.GL_SMOOTH);
        GlStateManager.pushMatrix(); 
		{
			BufferBuilder buff = Tessellator.getInstance().getBuffer();
			EntityPlayer entityplayer = Minecraft.getMinecraft().player;
	        double d0 = (entityplayer.lastTickPosX + (entityplayer.posX - entityplayer.lastTickPosX) * (double)event.getPartialTicks());
	        double d1 = (entityplayer.lastTickPosY + (entityplayer.posY - entityplayer.lastTickPosY) * (double)event.getPartialTicks());
	        double d2 = (entityplayer.lastTickPosZ + (entityplayer.posZ - entityplayer.lastTickPosZ) * (double)event.getPartialTicks());
	        Tessellator.getInstance().getBuffer().setTranslation(-d0, -d1, -d2);
			ArrayList<BlockPos> finalBlockPositions = new ArrayList<>();
	        buff.begin(GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
			for(BlockPos pos : finalBlockPos) {
				int i = entityplayer.world.getCombinedLight(pos, 0);
	            int j = i % 65536;
	            int k = i / 65536;
	            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
	            
		        float posX = pos.getX() + 0.5f;
		        float posY = pos.getY() + 0.5f;
		        float posZ = pos.getZ() + 0.5f;
		        Random rand = new Random(MathHelper.getPositionRandom(pos));
		        Vector4f color = new Vector4f(0.1f, 0f, 1f, 1f);
		    	Vec3d plusVec00 = getVec(pos, partialTicks);
		    	Vec3d plusVec01 = getVec(pos.add(0, 0, 1), partialTicks);
		    	Vec3d plusVec11 = getVec(pos.add(1, 0, 1), partialTicks);
		    	Vec3d plusVec10 = getVec(pos.add(1, 0, 0), partialTicks);

		        Vec3d vec3d2 = plusVec00.crossProduct(plusVec11).normalize();
		        float f = 0;
		        float f1 = 0;
		        float f2 = 0;

		        TextureAtlasSprite water = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.WATER.getDefaultState());
		        
				buff.pos(plusVec00.x, plusVec00.y, plusVec00.z).tex(water.getInterpolatedU(0), water.getInterpolatedV(0)).normal(f, f1, f2).endVertex();
				buff.pos(plusVec01.x, plusVec01.y, plusVec01.z).tex(water.getInterpolatedU(0), water.getInterpolatedV(16)).normal(f, f1, f2).endVertex();
				buff.pos(plusVec11.x, plusVec11.y, plusVec11.z).tex(water.getInterpolatedU(16), water.getInterpolatedV(16)).normal(f, f1, f2).endVertex();
				buff.pos(plusVec10.x, plusVec10.y, plusVec10.z).tex(water.getInterpolatedU(16), water.getInterpolatedV(0)).normal(f, f1, f2).endVertex();

				buff.pos(plusVec00.x, plusVec00.y, plusVec00.z).tex(water.getInterpolatedU(0), water.getInterpolatedV(0)).normal(f, f1, f2).endVertex();
				buff.pos(plusVec10.x, plusVec10.y, plusVec10.z).tex(water.getInterpolatedU(16), water.getInterpolatedV(0)).normal(f, f1, f2).endVertex();
				buff.pos(plusVec11.x, plusVec11.y, plusVec11.z).tex(water.getInterpolatedU(16), water.getInterpolatedV(16)).normal(f, f1, f2).endVertex();
				buff.pos(plusVec01.x, plusVec01.y, plusVec01.z).tex(water.getInterpolatedU(0), water.getInterpolatedV(16)).normal(f, f1, f2).endVertex();
			}
			Tessellator.getInstance().draw();
	        Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
		}
        GlStateManager.popMatrix();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(org.lwjgl.opengl.GL11.GL_FLAT);
	}

	private Vec3d getVec(BlockPos pos, float particleTicks) {
		if(Minecraft.getMinecraft().world.getBlockState(pos).getBlock() != Blocks.WATER) {
//			return new Vec3d(pos).addVector(0, 0.75, 0);
		}
		DoubleHolder offset = new DoubleHolder(Integer.MIN_VALUE);
		BooleanHolder bool = new BooleanHolder(false);
		if(WaterHandler.map.get(pos) != null) {
			WaterHandler.map.get(pos).forEach(data -> {
				double scopeOffset = Integer.MIN_VALUE;
				if(data != null) {
					scopeOffset = data.getOffset(particleTicks);
					if(Double.isNaN(scopeOffset)) {
						scopeOffset = Integer.MIN_VALUE;
					}
				}
				if(scopeOffset > offset.value) {
					offset.value = scopeOffset;
					bool.value = true;
				}
			});;
		}
		if(!bool.value) {
			offset.value = 0;
		}
		double y = Math.sin(System.currentTimeMillis() / 300D + (MathHelper.getPositionRandom(pos) % 360)) / 10D + offset.value;
//		y = offset.value;
        return new Vec3d(pos.getX(), pos.getY() + y + 0.75D, pos.getZ());
	}

	@Override
	public void run() 
	{
		try
		{
			while(!this.thread.isInterrupted())
			{
				Minecraft mc = Minecraft.getMinecraft();
				boolean interupt = false;
				if (mc.world != null && mc.player != null)
				{
					if (nextTimeMs > System.currentTimeMillis())
						interupt = true;

					int px = localPosX;
					int py = localPosY;
					int pz = localPosZ;
					if(px == prevLocalPosX && pz == prevLocalPosZ)
						interupt = true;
					EntityPlayer player = mc.player;
					ArrayList<BlockPos> posList = new ArrayList<>();
					for(int x = px - 50; x < px + 50; x++) {
						for(int z = pz - 50; z < pz + 50; z++) {
							for(int y = Math.min(0, py - 50); y < py + 50 || y < 256; y++)
							{
								BlockPos pos = new BlockPos(x, y, z);
								if(mc.world.getBlockState(pos).getBlock() == Blocks.WATER && mc.world.getBlockState(pos).getValue(BlockLiquid.LEVEL) == 0 && mc.world.getBlockState(pos.up()).getMaterial() != Material.WATER) {
									posList.add(pos);
								}
							}	
						}	
					}
					this.finalBlockPos = posList;
					nextTimeMs = System.currentTimeMillis() + delayMs;
				}
				else
				    interupt = true;
				if(interupt)
					this.thread.interrupt();
			}
			this.thread = null;
		}
		catch (Exception exc)
		{
			System.out.println("ClientTick Thread Interrupted!!! " + exc.toString());
		}
	}
}

