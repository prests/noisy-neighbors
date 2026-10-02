package com.github.prests.noisyneighbors.client.sound;

import com.github.prests.noisyneighbors.client.config.WorldIdentity;
import com.github.prests.noisyneighbors.client.zone.Zone;
import java.util.Map;
import java.util.UUID;

/** Small no-framework regression check: ./gradlew focusedCheck */
public final class FocusedCheck {
  public static void main(String[] args) {
    MobSoundCatalog.load();
    assert !WorldIdentity.local("first-save", "First").key().equals(WorldIdentity.local("second-save", "Second").key())
        : "local saves must have isolated settings";
    Zone zone = new Zone(UUID.randomUUID(), "negative", true, "minecraft:overworld", 4, 8, 2, -2, 8, -4, Map.of("minecraft:cow", 50));
    assert zone.contains(-2, 8.9, -4) : "normalized corners include negative edge";
    assert zone.contains(4.99, 8, 2.99) : "same-Y is one block high";
    assert !zone.contains(0, 9, 0) : "same-Y does not leak vertically";
    Zone overlap = new Zone(UUID.randomUUID(), "overlap", true, "minecraft:overworld", 0, 0, 0, 1, 1, 1, Map.of("minecraft:cow", 50));
    assert Math.abs(VolumePolicy.multiplier(Map.of("minecraft:cow", 100), zone, overlap) - .25F) < .001F : "zones multiply";
    assert "minecraft:cow".equals(MobSoundCatalog.mobFor("minecraft:entity.cow_moody.hurt", "minecraft:cow")) : "cow sound variants map to cows";
    assert MobSoundCatalog.mobFor("minecraft:entity.cow.ambient", "minecraft:pig") == null : "mismatched entity passes through";
  }
}
