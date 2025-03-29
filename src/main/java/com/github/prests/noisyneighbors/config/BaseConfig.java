package com.github.prests.noisyneighbors.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

public class BaseConfig<T> {
    private final Path configDir;
    private final String configFileName;
    private final Logger logger;

    private final Class<T> schema;
    private final Supplier<T> defaultConfig;
    private T configData;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BaseConfig(Class<T> schema, Supplier<T> defaultConfig, Path _configDir, String _configFileName, Logger _logger) {
        this.configDir = _configDir;
        this.configFileName = _configFileName;
        this.logger = _logger;
        this.schema = schema;
        this.defaultConfig = defaultConfig;
    }

    private void createConfigDir() {
        try {
            Files.createDirectory(this.configDir);
            this.logger.info("New config directory created.");
        } catch (IOException e) {
            this.logger.error("Failed to create config directory.");
            throw new RuntimeException(e);
        }
    }

    private void createConfigFile() {
        try {
            T defaultInstance = defaultConfig.get();
            String defaultJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(defaultInstance);
            Files.writeString(this.getConfigFilePath(), defaultJson, StandardOpenOption.CREATE_NEW);
            this.logger.info("New config file created.");
        } catch (IOException e) {
            this.logger.error("Failed to create config file.");
            throw new RuntimeException(e);
        }
    }

    private void loadConfig() {
        try {
            byte[] jsonData = Files.readAllBytes(this.getConfigFilePath());
            this.configData = objectMapper.readValue(jsonData, this.schema);
            this.logger.info("Config file loaded successfully.");
        } catch (IOException e) {
            this.logger.error("Failed to load config file.");
            throw new RuntimeException(e);
        }
    }

    protected void initializeConfig() {
        if (!Files.exists(this.configDir)) {
            this.createConfigDir();
        }

        if (!Files.exists(this.getConfigFilePath())) {
            this.createConfigFile();
        }

        this.loadConfig();
    }

    private Path getConfigFilePath() {
        return this.configDir.resolve(this.configFileName);
    }

    public T getConfigData() {
        return this.configData;
    }

    public void save() {
        try {
            String jsonAsStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.configData);

            Files.writeString(this.getConfigFilePath(), jsonAsStr, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            this.logger.error("Failed to save config file.", e);
            throw new RuntimeException("Failed to save config file", e);
        }
    }
}
