package schoperation.schopcraft;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import schoperation.schopcraft.cap.CapEvents;
import schoperation.schopcraft.cap.CapabilityHandler;
import schoperation.schopcraft.cap.ghost.Ghost;
import schoperation.schopcraft.cap.ghost.GhostStorage;
import schoperation.schopcraft.cap.ghost.IGhost;
import schoperation.schopcraft.cap.sanity.ISanity;
import schoperation.schopcraft.cap.sanity.Sanity;
import schoperation.schopcraft.cap.sanity.SanityStorage;
import schoperation.schopcraft.cap.temperature.ITemperature;
import schoperation.schopcraft.cap.temperature.Temperature;
import schoperation.schopcraft.cap.temperature.TemperatureStorage;
import schoperation.schopcraft.cap.thirst.IThirst;
import schoperation.schopcraft.cap.thirst.Thirst;
import schoperation.schopcraft.cap.thirst.ThirstStorage;
import schoperation.schopcraft.cap.wetness.IWetness;
import schoperation.schopcraft.cap.wetness.Wetness;
import schoperation.schopcraft.cap.wetness.WetnessStorage;
import schoperation.schopcraft.packet.SchopPackets;
import schoperation.schopcraft.season.WorldSeason;
import schoperation.schopcraft.season.modifier.BiomeTempController;
import schoperation.schopcraft.tweak.ServerCommands;
import schoperation.schopcraft.tweak.TweakEvents;
import schoperation.schopcraft.util.Registererer;
import schoperation.schopcraft.util.WorldDataMgr;

public class CommonProxy {
	
	public void preInit(FMLPreInitializationEvent event) {
		
		// Register all new items and blocks.
		MinecraftForge.EVENT_BUS.register(new Registererer());
		
		// Register capabilities.
		CapabilityManager.INSTANCE.register(IWetness.class, new WetnessStorage(), Wetness::new);
		CapabilityManager.INSTANCE.register(IThirst.class, new ThirstStorage(), Thirst::new);
		CapabilityManager.INSTANCE.register(ISanity.class, new SanityStorage(), Sanity::new);
		CapabilityManager.INSTANCE.register(ITemperature.class, new TemperatureStorage(), Temperature::new);
		CapabilityManager.INSTANCE.register(IGhost.class, new GhostStorage(), Ghost::new);
	}
	
	public void init(FMLInitializationEvent event) {
		
		// Register event handlers.
		MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
		MinecraftForge.EVENT_BUS.register(new CapEvents());
		MinecraftForge.EVENT_BUS.register(new TweakEvents());
		MinecraftForge.EVENT_BUS.register(new WorldSeason());
		
		// Register network packets.
		SchopPackets.initPackets();
	}
	
	public void postInit(FMLPostInitializationEvent event) {
		
	}
	
	public void serverStarted(FMLServerStartedEvent event) {
		
		// Fire some simple commands on the server before players log on.
		ServerCommands.fireCommandsOnStartup();
		
		// Grab initial biome temperatures.
		BiomeTempController biomeTemp = new BiomeTempController();
		biomeTemp.storeOriginalTemperatures();
		biomeTemp = null;
				
		// Load world data from file.
		WorldDataMgr.loadFromDisk();
	}
}