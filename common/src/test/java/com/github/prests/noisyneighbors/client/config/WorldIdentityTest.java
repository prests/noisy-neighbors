package com.github.prests.noisyneighbors.client.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class WorldIdentityTest {
  @Test void localSavesAreIsolatedByDirectory() {
    assertNotEquals(WorldIdentity.local("first-save", "Same name").key(), WorldIdentity.local("second-save", "Same name").key());
  }

  @Test void serverIdentityNormalizesEndpointButNotDisplayName() {
    WorldIdentity first = WorldIdentity.server(" Example.COM:25565 ", "First label");
    WorldIdentity second = WorldIdentity.server("example.com:25565", "Renamed label");

    assertEquals(first.key(), second.key());
    assertNotEquals(first.displayName(), second.displayName());
  }
}
