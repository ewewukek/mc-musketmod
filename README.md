### Musket Mod

Small mod that adds craftable flintlock weapons.

[Download from CurseForge](https://www.curseforge.com/minecraft/mc-mods/ewewukeks-musket-mod/files/all)\
[Download from modrinth](https://modrinth.com/mod/ewewukeks-musket-mod/versions)

### Forge

No additional dependencies required.

### Neoforge

Requires [Cloth Config API](https://modrinth.com/mod/cloth-config).

### Fabric

Requires [Fabric API](https://modrinth.com/mod/fabric-api) and [Cloth Config API](https://modrinth.com/mod/cloth-config).

### Mechanics

'Use' key loads and fires a weapon. Can't be done underwater.\
Bullets slow down and deal less damage with distance.

#### Musket
![](doc/musket_recipe.png?raw=true)

Damage: 20.5 - 21.0\
Durability: 250

Can't be used from offhand.

#### Musket with bayonet
![](doc/musket_with_bayonet_crafting.png?raw=true)

Deals 5 melee damage.

#### Blunderbuss
![](doc/blunderbuss_recipe.png?raw=true)

Damage: 20.5 - 21.0\
Durability: 200

Fires 10 pellets with higher spread than musket.\
For simplicity, uses same ammunition as musket.

#### Pistol
![](doc/pistol_recipe.png?raw=true)

Damage: 12.0 - 12.5\
Durability: 150

Fires slower bullets. Can be used from both hands.\
When dual wielding, only the pistol in main hand is loaded. To load second, swap pistols in hands.

#### Paper cartridge (ammunition)
![](doc/cartridge_recipe.png?raw=true)

Can be fired from Dispenser dealing 10.0 - 10.5 damage.

### Configuration:

Stored in `GAME_DIRECTORY/config/musketmod.txt`

You can access settings from NeoForge's mods menu or via [Mod Menu](https://modrinth.com/mod/modmenu) on Fabric.

Bullet spread follows normal distribution like in real life but it is awkward to configure. Here is a simple table that should help with that: [bulletStdDev](STDDEV.md "bulletStdDev")

Reload duration is not configurable with current implementation. It may change in the future.

### Permissions:

You may include this mod in any modpack, private or public, along with any modifications.

My sprites are free to use for anyone. ([CC0](https://creativecommons.org/publicdomain/zero/1.0/))

Sounds that I based mine on are listed here: [credits.txt](src/main/resources/assets/musketmod/sounds/credits.txt)

### Credits:
- Mojang (duh)
- Minecraft Forge and guys from the forum
- FabricMC
- lilypuree (fixes and Korean translation)
- MikhailTapio (Chinese translation)
- Xannosz & SkpC9 (1.20.x update)
- [Some sounds I've used](src/main/resources/assets/musketmod/sounds/credits.txt)
