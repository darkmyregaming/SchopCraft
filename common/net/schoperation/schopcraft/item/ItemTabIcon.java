package net.schoperation.schopcraft.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.schoperation.schopcraft.SchopCraft;

public class ItemTabIcon extends Item {
	
	/*
	 * This is the tab icon. Not meant to be an actual part of the experience. But, may as well make it do something
	 * ...other than being the creative tab icon.
	 */
	
	public ItemTabIcon() {
		
		// setting registry name and crap
		setRegistryName(new ResourceLocation(SchopCraft.MOD_ID, "tabicon"));
		setUnlocalizedName(SchopCraft.RESOURCE_PREFIX + "tabicon");
		
		// additional properties
		setMaxStackSize(1);
		setCreativeTab(SchopCraft.mainTab);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		
		if (world.isRemote) {
			
			String versionMessage = "§eSchopCraft v" + SchopCraft.VERSION + " for Minecraft " + SchopCraft.MCVERSION;
			player.sendMessage(new TextComponentString(versionMessage));
		}
		
		return super.onItemRightClick(world, player, hand);
	}
}
