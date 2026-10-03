# Noisy Neighbor

**Enjoy Minecraft's sounds everywhere... except the ones that wear out their welcome.**

Minecraft's built-in sound controls are broad: turning down a noisy animal farm can also silence the creatures and ambient moments that make exploring feel alive. Noisy Neighbor gives you granular controls for vanilla mob sounds, so you can quiet a crowded farm without turning the whole game down.

Set a volume for each creature across every world, then create sound zones for places that need their own rules. Keep the sounds you enjoy and silence those noisy neighbors.

## Getting started

Noisy Neighbor is client-side only: install it on your client and use it in single-player or multiplayer worlds—no server installation is needed.

### Set global sound controls

1. Open **Options → Music & Sound → Advanced Sound Controls**.
2. Use the sliders to set a volume for each mob.
3. Set a mob to **0%** to mute it everywhere, or choose a lower volume to keep it in the background.
4. Select the **⚙ Advanced Controls** button beside a mob to adjust its individual sound events.

These controls are a great default when a particular creature is always too loud. Event controls multiply the mob control, and zone mob/event controls multiply again for every matching zone.

## Create a sound zone

Use a zone when you only want quieter sounds in one place, such as an animal farm, mob grinder, or trading hall.

1. Open **Edit World Zones**.
2. Select **Create Zone**.
3. Right-click two opposite corner blocks to mark the zone's bounds. The action bar guides you through the selection.
4. Open the new zone to adjust each mob's volume, or use its **⚙ Advanced Controls** button for individual events.

Press your configured **Create Zone** key again to cancel while choosing corners.

## Edit a zone

Open **Edit World Zones**, then select a zone to manage it:

- **Rename Zone** gives the zone a helpful name.
- **Edit Zone Color** changes its outline color.
- **Edit Zone Bounds** lets you choose two new corner blocks.
- **Delete Zone** removes it.
- **Zone Outlines: On** shows enabled zones in your current dimension, making it easy to see exactly where they apply.

## What Noisy Neighbor changes

Noisy Neighbor changes only catalogued vanilla mob sounds. Player, block, unknown, and modded sounds continue to use Minecraft's normal audio behavior. Your settings are stored locally in `config/noisy-neighbors.json`.

## License

MIT. See [LICENSE](LICENSE).
