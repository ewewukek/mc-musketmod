### Musket Mod

Adds craftable flintlock weapons.

[Download from CurseForge](https://www.curseforge.com/minecraft/mc-mods/ewewukeks-musket-mod/files/all)\
[Download from modrinth](https://modrinth.com/mod/ewewukeks-musket-mod/versions)

### Forge

No additional dependencies required.

### Neoforge

Requires [Cloth Config API](https://modrinth.com/mod/cloth-config).

### Fabric

Requires [Fabric API](https://modrinth.com/mod/fabric-api) and [Cloth Config API](https://modrinth.com/mod/cloth-config).

### Mechanics

RMB loads and fires guns. Full sized ones can't be used from offhand.

All guns are unusable inside water and bullets lose power when flying through it.

Bullets can ignite TNT and destroy some blocks (only non-stained glass panes by default).

Headshots increase bullet damage by 30% (applies to most bipedal mobs).

#### Musket
![](doc/musket_recipe.png?raw=true)

Damage: 16\
Durability: 250\
Enchantments: Knockback

#### Musket upgrade smithing template
![](doc/musket_upgrade_recipe.png?raw=true)

Required to craft upgraded muskets. Has a high chance to spawn in strongholds and woodland mansions and with a lower chance in other overworld structures.

#### Musket with Bayonet
![](doc/musket_with_bayonet_smithing.png?raw=true)

Melee damage: 5\
Enchantments: Sharpness, Smite, Bane of Arthropods

#### Scoped Musket
![](doc/musket_with_scope_smithing.png?raw=true)

Zoom factor: 3x\
Durability: 150\
Enchantments: Knockback

Greatly improved accuracy and reduced bullet drop. Use RMB to aim and LMB to fire.

#### Blunderbuss
![](doc/blunderbuss_recipe.png?raw=true)

Damage: 21\
Durability: 200\
Enchantments: Flame

Fires 9 pellets with much higher spread than musket.

#### Flintlock Pistol
![](doc/pistol_recipe.png?raw=true)

Damage: 12\
Durability: 200\
Enchantments: Quick Charge

Can be fired from any and both hands. You can reload both by holding down RMB.

#### Paper cartridge (ammunition)
![](doc/cartridge_recipe.png?raw=true)

Dispensers can fire them dealing 10 damage.

### Monsters

Pillagers and skeletons (all kinds) can use guns. They also may spawn with one (20% for pillagers with a pistol, 5% for skeletons with a musket).

Monsters deal 50% damage with guns by default.

### Configuration

Settings can be accessed from NeoForge's mods menu or via [Mod Menu](https://modrinth.com/mod/modmenu) on Fabric.

Bullet spread follows normal distribution like in real life but it is awkward to configure. Here is a simple table that could help with that: [bulletStdDev](STDDEV.md "bulletStdDev")

Loot tables in namespace `musketmod` add items to matching vanilla loot tables.

`item/enchantable/*` tags control what enchantments can be added to the guns.\
`block/*_by_bullets` tags control which blocks would be destroyed or dropped by bullets.\
`entity_type/headshotable` tag controls which mobs are considered for headshot detection.

[Blockbench models](blockbench) have preconfigured display settings making exporting easier. You'll only need to copy "overrides" from defaults.

### Permissions

You may include this mod in any modpack, private or public, along with any modifications.

My art is free to use for anyone. ([CC0](https://creativecommons.org/publicdomain/zero/1.0/))

Sounds that I based mine on are listed here: [credits.txt](src/main/resources/assets/musketmod/sounds/credits.txt)

### Credits

- Mojang (duh)
- Minecraft Forge and guys from the forum
- FabricMC
- lilypuree (fixes and Korean translation)
- MikhailTapio (Chinese translation)
- Xannosz & SkpC9 (1.20.x update)
