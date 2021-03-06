package schoperation.schopcraft.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import schoperation.schopcraft.SchopCraft;
import schoperation.schopcraft.season.Season;
import schoperation.schopcraft.season.WorldSeason;

public class WorldDataMgr {
	
	/*
	 * Manage your world data today!
	 */
	
	public static void loadFromDisk() {
		
		// Instance of the server.
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		
		// Instance of the server world.
		World world = server.getEntityWorld();
		
		// Load stuff from world data file
		SchopWorldData data = SchopWorldData.load(world);
		
		// Notification
		SchopCraft.logger.info("Loaded world data. Current season is " + data.getSeasonFromData() + " and we are " + data.daysIntoSeason + " days into this season.");
		
		// Now send this data to all around the mod
		// Seasons
		WorldSeason.getSeasonData(data.getSeasonFromData(), data.daysIntoSeason);
	}
	
	public static void save(Season seasonNew, int daysIntoSeasonNew) {
		
		// Instance of the server.
		// Yes we have to load it again so we can overwrite it in this function yay yay yay
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		
		// Instance of the server world.
		World world = server.getEntityWorld();
		
		// Load stuff from world data file
		SchopWorldData data = SchopWorldData.load(world);
		
		// Overwrite data
		data.season = SchopWorldData.seasonToInt(seasonNew);
		data.daysIntoSeason = daysIntoSeasonNew;
		
		SchopCraft.logger.info("Saving data to disk.");
		
		// Mark dirty for saving
		data.markDirty();
	}
}
