# Structure Restrictions
Allows modification of some gameplay aspects within structures (f.e. mining/placing blocks) via datapacks


## [CurseForge](https://www.curseforge.com/minecraft/mc-mods/structure-restrictions)
## [Modrinth](https://modrinth.com/mod/structure-restrictions)

------------------------------------------------------------------------------------------------------------

# Table of contents

- [Creating structure restrictions](#creating-structure-restrictions)
- [Structure restriction types](#structure-restriction-types)
- [Structure restrictions templates](#structure-restrictions-templates)
- [Source entries](#source-entries)
- [Field explanations](#field-explanations)

---------------------------------------------------

# Creating structure restrictions:
Create json file in `data/frycstructmod/structure_restrictions/`, select one of the [templates](#structure-restrictions-templates) and change the values.

-----

# Structure restriction types:

### Default

Selected blocks cannot be placed/mined.

### Status effect

Selected entities are immune to selected status effects.
Additionally, selected status effects are applied (to player or mobs) when player enters structure.

### Fire

Fire ticks are disabled. Every stopped tick tries to extinguish fire.

### Explosion

Explosions don't destroy blocks.

-----

# Structure restrictions templates

### Default

```json
{
  "type": "default",
  "structure_id": "mod_id:structure",
  "welcome_message": "Example welcome message",
  "leave_message": "Example leave message",
  "mining": {
    "allow": false,
    "except": [
      "minecraft:some_minable_block",
      "mod_id:some_other_minable_block"
    ],
    "always_allow_when_placed_by_player": false,
    "mining_speed_multiplier": 0.02
  },
  "placing": {
    "allow": true,
    "except": [
      "minecraft:some_unplaceable_block",
      "mod_id:some_other_unplaceable_block"
    ],
    "always_disallow_when_indestructible": true
  },
  "source": {
    "shared": true,
    "shareOperation": "max",
    "power": 10,
    "entries": []
  }
}
```

### Status effect

```json
{
  "type": "status_effect",
  "structure_id": "mod_id:structure",
  "welcome_message": "Example welcome message",
  "leave_message": "Example leave message",
  "entities_affected": {
    "affect_all": false,
    "except": [
      "minecraft:some_affected_mob",
      "mod_id:some_other_affected_mob"
    ]
  },
  "allowed_effects": {
    "allowed": true,
    "except": [
      "minecraft:some_deactivated_status_effect",
      "mod_id:some_other_deactivated_status_effect"
    ]
  },
  "structure_effects": [
    {
      "id": "mod_id:some_status_effect_that_will_be_applied_on_enter",
      "amplifier": 1,
      "duration": -1,
      "forPlayer": true,
      "ambient": false,
      "showParticles": true,
      "showIcon": true
    }
  ],
  "source": {
    "shared": true,
    "shareOperation": "max",
    "power": 10,
    "entries": []
  }
}
```

### Fire

```json
{
  "type": "fire",
  "structure_id": "mod_id:structure",
  "welcome_message": "Example welcome message",
  "leave_message": "Example leave message",
  "non_ticking_fire_remove_chance": 0.20,
  "source": {
    "shared": true,
    "shareOperation": "max",
    "power": 10,
    "entries": []
  }
}
```

### Explosion

```json
{
  "type": "explosion",
  "structure_id": "mod_id:structure",
  "welcome_message": "Example welcome message",
  "leave_message": "Example leave message",
  "source": {
    "shared": true,
    "shareOperation": "max",
    "power": 10,
    "entries": []
  }
}
```

----

# Source entries

### Templates:

```json
      {
        "type": "mobKill",
        "entry_id": "minecraft:sheep",
        "strength": 1
      }
```

```json
      {
        "type": "persistentMobKill",
        "entry_id": "minecraft:piglin_brute",
        "forcePersistent": true,
        "checkForOtherPersistentMobs": true,
        "strength": 1
      }
```

```json
      {
        "type": "blockBreak",
        "entry_id": "minecraft:oak_log",
        "strength": 1
      }
```

```json
      {
        "type": "itemUseFinish",
        "entry_id": "minecraft:cooked_porkchop",
        "strength": 1
      }
```

### Explanations:

#### Common fields:

- `type` - event, that decreases restriction's power by `strength` value
- `entry_id` - id of a mob, item or block, depending on `type`
- `strength` - restriction's power will be decreased by this value after triggering event

#### Type: persistentMobKill:

If there are no more mobs of selected type in structure, restriction will be disabled regardless of remaining power (f.e. if you kill the last piglin brute in bastion, restriction will deactivate despite power being above 0)

- `forcePersistent` - when true, non-persistent mobs of selected type will be ignored (has to be false for passive mobs)
- `checkForOtherPersistentMobs` - if this value is true and there are multiple `persistentMobKill` entries, mobs from all entries must disappear to instantly disable restriction 

----

# Field explanations

### Common fields:

- `type` - type of restriction
- `structure_id` - id of a structure, your restriction is being created for
- `welcome_message` - message that is shown upon entering structure with active restriction
- `leave_message` - message that is shown after leaving structure with active restrictions (or when restrictions deactivate)
- `source` - definition of a way to deactivate restriction:
    * `power` - when reaches 0, restriction deactivates
    * `shared` - when true, your restriction shares power with other restrictions (for the same structure)
    * `shareOperation` - when `shared` is true, defines how shared power is calculated (max, add or mul) (i don't recommend using other than `max` for now because it's poorly implemented)
    * `entries` - events that affect your restriction's power (more info in 'Source entries' section)

### Type: default:

- `mining` - object defining which blocks can be mined
- `placing` - object defining which blocks can be placed
- `allow` - when false, all blocks cannot be mined/placed
- `except` - exceptions from `allow` rule: when `allow` is false, all blocks inside `except` field can be mined/placed
- `always_allow_when_placed_by_player` - player can always mine blocks with `placed_by_player` property (may not work with all blocks due to block property limitations)
- `mining_speed_multiplier` - when above 0, players can always mine instant-break blocks (and they can mine any block, but slower)
- `always_disallow_when_indestructible` - this option prevents players from placing blocks that they won't be able to break (due to block property limitations, not all blocks have `placed_by_player` property)

### Type: status_effect:

- `entities_affected` - object defining entities which become immune to status effects selected in `allowed_effects`
- `allowed_effects` - object defining effects to which entities from `entities_affected` become immune
- `affect_all` | `allowed` - when true, all effects/entities are selected
- `except` - exceptions from `affect_all` and `allowed` rules
- `structure_effects` - list of effects that will be applied when player enters structure, and removed when player leaves structure

### Type: fire:

- `non_ticking_fire_remove_chance` - chance to remove fire every time the world tries to tick it (0.01 = 1%)

