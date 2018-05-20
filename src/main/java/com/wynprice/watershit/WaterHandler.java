package com.wynprice.watershit;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.WaveData;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WaterHandler {
	public static final HashMap<BlockPos, List<WaterData>> map = Maps.newHashMap();
	
	public static void makeSplash(BlockPos pos, double power) {
		power = Math.max(power, 5);
		World world = Minecraft.getMinecraft().world;
		for(double x = -power; x <= power; x++) {
			for(double z = -power; z <= power; z++) {
				BlockPos testPos = pos.add(x, 0, z);
				float distance = (float) Math.sqrt(testPos.distanceSq(pos));
				if(distance > power) {
					continue;
				}
				
				WaterData data = new WaterData(testPos);
				data.multiplier = (1 - (distance / power));

				data.maxCountTimer = power * WaterData.WAVE_TIME;
				data.countDownTimer = data.maxCountTimer;
				data.countDownStartTimer = Math.max(distance - 1.5f, 0) * WaterData.WAVE_SPEED;
				List<WaterData> list = map.get(testPos);
				if(list == null) {
					list = Lists.newArrayList();
				}
				
				list.add(data);
				
				map.put(testPos, list);
			}
		}
	}
	
	static List<Pair<BlockPos, WaterData>> removeList = Lists.newArrayList();
	
	private static void markRemoval(BlockPos pos, WaterData data) {
		removeList.add(Pair.of(pos, data));
	}
	
	public static void doRemovals() {
		removeList.forEach(pair -> {
			List<WaterData> list = map.get(pair.getKey());
			if(list != null) {
				list.remove(pair.getValue());
			}
		});
		removeList.clear();
	}
	
	public static class WaterData {
		
		private static final double WAVELENGTH = 25D; 
		private static final double WAVEHEIGHT_MULTIPLIER = 2D;
		private static final double WAVE_SPEED = 20D; //The higher the number, the faster the wave
		private static final double WAVE_TIME = 10D; //How long the wave lasts i think. tbh i dont actually fucking know what this does
		
		private final BlockPos pos;
		
		public WaterData(BlockPos pos) {
			this.pos = pos;
		}
		
		public double countDownStartTimer;
		public double maxCountTimer;
		public double countDownTimer;
		public int timer;
		public double multiplier;
		
		public void tick() {
			if(countDownStartTimer-- <= 0) {
				countDownStartTimer = 0;
				if(countDownTimer-- <= 0) {
					countDownTimer = 0;
					markRemoval(pos, this);
				} else {
					timer--;
				}
			}
		}
		
		public double getOffset(float partialTicks) {
			if(countDownStartTimer > 0) {
				return 0;
			}
			return (Math.sin(timer / WAVELENGTH) * (countDownTimer / maxCountTimer)) * multiplier * WAVEHEIGHT_MULTIPLIER;
		}
	}
}
