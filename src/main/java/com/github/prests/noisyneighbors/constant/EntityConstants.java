package com.github.prests.noisyneighbors.constant;

import net.minecraft.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class EntityConstants {
    public static final Map<EntityType<?>, String> ENTITY_FRIENDLY_NAME_MAP = new HashMap<>();

    public static final String PIG_FRIENDLY_NAME = "pig";

    static {
        ENTITY_FRIENDLY_NAME_MAP.put(EntityType.PIG, PIG_FRIENDLY_NAME);
    }
}
