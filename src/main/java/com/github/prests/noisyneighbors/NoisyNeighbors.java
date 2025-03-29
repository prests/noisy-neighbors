package com.github.prests.noisyneighbors;

import com.github.prests.noisyneighbors.config.GlobalConfig;
import com.github.prests.noisyneighbors.config.GlobalEntityVolumeConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.nio.file.Path;

public class NoisyNeighbors implements ModInitializer {
	private static final GlobalConfig globalConfig = GlobalConfig.getInstance();
	private static final Logger logger = globalConfig.getLogger();

	@Override
	public void onInitialize() {
		final Path globalEntityVolumneConfigPath = FabricLoader.getInstance().getConfigDir().resolve(globalConfig.getModId());
		GlobalEntityVolumeConfig.initialize(globalEntityVolumneConfigPath);

		logger.info("Initialized!");
	}
}