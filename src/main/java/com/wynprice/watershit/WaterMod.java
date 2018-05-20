package com.wynprice.watershit;

import java.util.Map;

import com.google.common.collect.Maps;
import com.wynprice.watershit.WaterHandler.WaterData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid=WaterMod.MODID, name=WaterMod.NAME, version=WaterMod.VERSION)
@EventBusSubscriber
public class WaterMod {
	
	public static final String MODID = "waterthing";
    public static final String NAME = "Water Thing";
    public static final String VERSION = "1.0";
    
    @EventHandler
    public void p(FMLPreInitializationEvent event) {
    	MinecraftForge.EVENT_BUS.register(new RenderHandler());
    }

	public static class WaterBlock extends BlockStaticLiquid {
		
		protected WaterBlock(Material materialIn) {
			super(materialIn);
		}
		
		@Override
		public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
			if(worldIn.isRemote) {
				System.out.println(Math.log(1f + (fallDistance * entityIn.height * entityIn.width * 4f)) * 3f);
				WaterHandler.makeSplash(pos, Math.log(1f + (fallDistance * entityIn.height * entityIn.width * 4f) ) * 3f);
			}
			super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
		}
		
		private final Map<String, Long> maps = Maps.newHashMap();
		
		@Override
		public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
			if(worldIn.isRemote && !entityIn.isInWater()) {
				if(!maps.containsKey(entityIn.getUniqueID().toString())) {
					maps.put(entityIn.getUniqueID().toString(), entityIn.world.getTotalWorldTime());
					WaterHandler.makeSplash(pos, 20D);
				} else if(entityIn.world.getTotalWorldTime() - maps.get(entityIn.getUniqueID().toString()) > 10) {
					WaterHandler.makeSplash(pos, 20D);
					maps.put(entityIn.getUniqueID().toString(), entityIn.world.getTotalWorldTime());
				}
				
			}
			super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
		}
		
		@Override
		public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
				EnumFacing side) {
			if(side == EnumFacing.UP && blockState.getValue(LEVEL) == 0) {
				return false;
			}
			return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
		}
		
	}
	
	private static Block b = (new WaterBlock(Material.WATER)).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("water").setRegistryName("minecraft:water");
	
	@SubscribeEvent
	public static void onRegistry(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(b);
	}
	
	@SubscribeEvent
	public static void onClientTick(RenderWorldLastEvent event) {
		WaterHandler.map.values().forEach(list -> list.forEach(WaterData::tick));
		WaterHandler.doRemovals();
	}

}
