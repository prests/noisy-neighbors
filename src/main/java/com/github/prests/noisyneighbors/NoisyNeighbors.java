package com.github.prests.noisyneighbors;

import com.github.prests.noisyneighbors.config.GlobalConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

import java.nio.file.Path;

public class NoisyNeighbors implements ModInitializer {
	private static final GlobalConfig globalConfig = GlobalConfig.getInstance();
	private static final Logger logger = globalConfig.getLogger();

	@Override
	public void onInitialize() {
		logger.info("Initialized!");
	}
}