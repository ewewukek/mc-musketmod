### Musket Mod

Small mod that adds craftable muskets and pistols.

[Download from CurseForge](https://www.curseforge.com/minecraft/mc-mods/ewewukeks-musket-mod/files/all)\
[Download from modrinth](https://modrinth.com/mod/ewewukeks-musket-mod/versions)

### Forge

No additional dependencies required.

### Fabric

Requires [Fabric API](https://modrinth.com/mod/fabric-api).

### Mechanics

'Use' key loads and fires a weapon. Can't be done underwater.

#### Musket
![](doc/musket_recipe.png?raw=true)

Damage: 20.5 - 21.0  
Durability: 250

Can't be used from offhand.

#### Musket with bayonet
![](doc/musket_with_bayonet_crafting.png?raw=true)

Deals 5 melee damage.

#### Pistol
![](doc/pistol_recipe.png?raw=true)

Damage: 12.0 - 12.5  
Durability: 150

Fires slower bullets. Can be used from both hands.
Only pistol in main hand is loaded. To load second, swap pistols in hands.

#### Paper cartridge (ammunition)
![](doc/cartridge_recipe.png?raw=true)

### Configuration:

Stored in `GAME_DIRECTORY/config/musketmod.txt`

Configurable values are durability, damage, bullet speed, maximum travel distance and accuracy.

Bullet spread follows normal distribution like in real life but it is awkward to configure. Here is a simple table that should help with that: [bulletStdDev](STDDEV.md "bulletStdDev")

Reload duration is not configurable with current implementation. It may change in the future.

### Permissions:

You may include this mod in any modpack, private or public, along with any modifications.

My sprites are free to use.

Sounds that I based mine on are listed here: [credits.txt](src/main/resources/assets/musketmod/sounds/credits.txt)

### Credits:
- Mojang (duh)
- Minecraft Forge and guys from the forum
- FabricMC
- lilypuree (fixes and Korean translation)
- MikhailTapio (Chinese translation)
- Xannosz & SkpC9 (1.20.x update)
- [Some sounds I've used](src/main/resources/assets/musketmod/sounds/credits.txt)
