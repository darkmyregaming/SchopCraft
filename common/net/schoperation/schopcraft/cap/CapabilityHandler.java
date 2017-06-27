package net.schoperation.schopcraft.cap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.schoperation.schopcraft.SchopCraft;
import net.schoperation.schopcraft.cap.wetness.WetnessProvider;

public class CapabilityHandler {
	/*
	 * This attaches all capabilities to the player
	 */
	public static final ResourceLocation WETNESS_CAP = new ResourceLocation(SchopCraft.MOD_ID, "wetness");
	
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		
		if(!(event.getObject() instanceof EntityPlayer)) return;
		
		event.addCapability(WETNESS_CAP, new WetnessProvider());
	}

}
