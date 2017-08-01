package net.schoperation.schopcraft.cap.thirst;

import java.util.Iterator;

import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeBeach;
import net.minecraft.world.biome.BiomeOcean;
import net.minecraft.world.biome.BiomeSwamp;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.schoperation.schopcraft.cap.temperature.ITemperature;
import net.schoperation.schopcraft.cap.temperature.TemperatureProvider;
import net.schoperation.schopcraft.lib.ModDamageSources;
import net.schoperation.schopcraft.packet.PotionEffectPacket;
import net.schoperation.schopcraft.packet.SchopPackets;
import net.schoperation.schopcraft.packet.SummonInfoPacket;
import net.schoperation.schopcraft.packet.ThirstPacket;
import net.schoperation.schopcraft.util.SchopServerEffects;

/*
 * Where thirst is modified.
 */

public class ThirstModifier {
	
	// This allows the client to tell the server of any changes to the player's thirst that the server can't detect.
	public static void getClientChange(String uuid, float newThirst, float newMaxThirst, float newMinThirst) {
	
		// basic server variables
		MinecraftServer serverworld = FMLCommonHandler.instance().getMinecraftServerInstance();
		int playerCount = serverworld.getCurrentPlayerCount();
		String[] playerlist = serverworld.getOnlinePlayerNames();	
		
		// loop through each player and see if the uuid matches the sent one.
		for (int num = 0; num < playerCount; num++) {
			
			EntityPlayerMP player = serverworld.getPlayerList().getPlayerByUsername(playerlist[num]);
			String playeruuid = player.getCachedUniqueIdString();
			IThirst thirst = player.getCapability(ThirstProvider.THIRST_CAP, null);
			boolean equalStrings = uuid.equals(playeruuid);
			
			if (equalStrings) {
	
				thirst.increase(newThirst-10);
				thirst.setMax(newMaxThirst);
				thirst.setMin(newMinThirst);
			}
		}
	}
	
	public static void onPlayerUpdate(EntityPlayer player) {
		
		// get capabilities
		IThirst thirst = player.getCapability(ThirstProvider.THIRST_CAP, null);
		ITemperature temperature = player.getCapability(TemperatureProvider.TEMPERATURE_CAP, null);
		
		// sizzlin' server side stuff (crappy attempt at a tongue twister there)
		if (!player.world.isRemote) {
			
			// lava fries you well. This might be removed someday.
			if (player.isInLava()) {
				
				thirst.decrease(0.5f);
			}
			
			// the nether is also good at frying.
			else if (player.dimension == -1) {
				
				thirst.decrease(0.006f);
			}
			
			// overheating dehydrates very well.
			else if (temperature.getTemperature() > 90.0f) {
				
				float amountOfDehydration = temperature.getTemperature() / 10000;
				thirst.decrease(amountOfDehydration);
			}
			
			// natural dehydration. "Slow" is an understatement here.
			else {
				
				thirst.decrease(0.003f);
			}
			
			// =========================================================================================================
			//                                    The side effects of thirst.
			// Side effects of dehydration include fatigue and dizzyness. Those are replicated here. Well, attempted.
			// =========================================================================================================
			
			// Does the player have existing attributes with the same name? Remove them.
			// Iterate through all of modifiers. If one of them is a thirst one, delete it so another one can take its place.
			Iterator<AttributeModifier> speedModifiers = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getModifiers().iterator();
			Iterator<AttributeModifier> damageModifiers = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getModifiers().iterator();
			Iterator<AttributeModifier> attackSpeedModifiers = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getModifiers().iterator();
			
			// Speed
			while (speedModifiers.hasNext()) {
				
				AttributeModifier element = speedModifiers.next();
				
				if (element.getName().equals("thirstSpeedDebuff")) {
					
					player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(element);
				}
			}
			
			// Attack Damage
			while (damageModifiers.hasNext()) {
				
				AttributeModifier element = damageModifiers.next();
				
				if (element.getName().equals("thirstDamageDebuff")) {
					
					player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).removeModifier(element);
				}
			}

			// Attack Speed
			while (attackSpeedModifiers.hasNext()) {
				
				AttributeModifier element = attackSpeedModifiers.next();
				
				if (element.getName().equals("thirstAttackSpeedDebuff")) {
					
					player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).removeModifier(element);
				}
			}
			
			// Scale the modifiers according to current thirst.
			double speedDebuffAmount = (40 - thirst.getThirst()) * -0.002;
			double damageDebuffAmount = (40 - thirst.getThirst()) * -0.02;
			double attackSpeedDebuffAmount = (40 - thirst.getThirst()) * -0.08;
			
			// Create attribute modifiers
			AttributeModifier speedDebuff = new AttributeModifier("thirstSpeedDebuff", speedDebuffAmount, 0);
			AttributeModifier damageDebuff = new AttributeModifier("thirstDamageDebuff", damageDebuffAmount, 0);
			AttributeModifier attackSpeedDebuff = new AttributeModifier("thirstAttackSpeedDebuff", attackSpeedDebuffAmount, 0);
			
			// now determine when to debuff the player
			if (thirst.getThirst() < 5.0f) {
				
				player.attackEntityFrom(ModDamageSources.DEHYDRATION, 4.0f);
			}
			
			if (thirst.getThirst() < 15.0f) {
				
				SchopServerEffects.affectPlayer(player.getCachedUniqueIdString(), "nausea", 100, 5, false, false);
				SchopServerEffects.affectPlayer(player.getCachedUniqueIdString(), "mining_fatigue", 100, 3, false, false);
			}
			
			if (thirst.getThirst() < 40.0f) {
				
				// speed + damage + attack speed oh my
				player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(speedDebuff);
				player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(damageDebuff);
				player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).applyModifier(attackSpeedDebuff);	
			}
		}
	}
	
	public static void onPlayerInteract(EntityPlayer player) {
		
		// get capability
		IThirst thirst = player.getCapability(ThirstProvider.THIRST_CAP, null);
		
		// client-side crap
		if (player.world.isRemote) {
			
			thirst.set(10f);
			
			// this is for drinking water with your bare hands. Pretty ineffective.
			RayTraceResult raytrace = player.rayTrace(2, 1.0f);
			
			// if there's something
			if (raytrace != null) {
				
				// if it isn't a block (water isn't considered a block in this case).
				if (raytrace.typeOfHit == RayTraceResult.Type.MISS) {
					
					BlockPos pos = raytrace.getBlockPos();
					Iterator<ItemStack> handItems = player.getHeldEquipment().iterator();
					
					// if it is water and the player isn't holding jack squat (main hand)
					if (player.world.getBlockState(pos).getMaterial() == Material.WATER && handItems.next().isEmpty()) {
						
						// still more if statements. now see what biome the player is in, and quench thirst accordingly.
						Biome biome = player.world.getBiome(pos);
						
						if (biome instanceof BiomeOcean || biome instanceof BiomeBeach) {
							
							thirst.decrease(0.5f);
						}
						else if (biome instanceof BiomeSwamp) {
							
							thirst.increase(0.25f);
							
							// damage player for drinking dirty water
							IMessage potionMsg = new PotionEffectPacket.PotionEffectMessage(player.getCachedUniqueIdString(), "poison", 12, 3, false, false);
							SchopPackets.net.sendToServer(potionMsg);
						}
						else {
							
							thirst.increase(0.25f);
							
							// random chance to damage player
							double randomNum = Math.random();
							if (randomNum <= 0.50) { // 50% chance
								
								IMessage potionMsg = new PotionEffectPacket.PotionEffectMessage(player.getCachedUniqueIdString(), "poison", 12, 1, false, false);
								SchopPackets.net.sendToServer(potionMsg);
							}
						}
												
						// spawn particles and sounds for drinking water
						IMessage msgStuff = new SummonInfoPacket.SummonInfoMessage(player.getCachedUniqueIdString(), "WaterSound", "DrinkWaterParticles", pos.getX(), pos.getY(), pos.getZ());
						SchopPackets.net.sendToServer(msgStuff);
					}		
				}
			}
			
			// send thirst packet to server
			IMessage msg = new ThirstPacket.ThirstMessage(player.getCachedUniqueIdString(), thirst.getThirst(), thirst.getMaxThirst(), thirst.getMinThirst());
			SchopPackets.net.sendToServer(msg);	
		}
	}
}
