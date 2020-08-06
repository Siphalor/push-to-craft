# Push To Craft

[![curseforge downloads](http://cf.way2muchnoise.eu/full_push-to-craft_downloads.svg)](https://minecraft.curseforge.com/projects/push-to-craft)
[![curseforge mc versions](http://cf.way2muchnoise.eu/versions/push-to-craft.svg)](https://minecraft.curseforge.com/projects/push-to-craft)

![logo](images/icon_big.png?raw=true)

## Usage
This mod adds a new data type called `push to craft`.

A valid json file is built like this:
- `additions`: _Array_ - Defines all items or tags that you want to add to the targets
    - Entries are item ids as _Strings_. You can use tags by prepending a `#`.
- `targets`: _Array_ or _String_ - Defines which items and tags to target in the recipes
    - Entries are item ids or tags as _Strings_.
- `recipes`: _Object_ - Gives conditions for which items to choose
    - `types`: _Array_ - __optional__ - Defines which recipe serializers to target
        - Entries are recipe serializer ids as _Strings_
    - `ids`: _Array_ - __optional__ - Defines which recipes to target
        - Entries are _Strings_ that are either of these:
            - Exact recipe ids à la `minecraft:anvil`
            - Namespace specific regular expression that only mark the last part of the id as regex à la `minecraft:.*`
            - A complete regular expression beginning and ending with a forward slash à la `/.*craft:.*/`
            
## Example
This allows you to use blackstone in favor of cobblestone everywhere:
```json
{
    "additions": [
        "minecraft:blackstone"
    ],
    "targets": [
        "minecraft:cobblestone"
    ],
    "recipes": {}
}
```

This allows you to use emeralds and all kinds of planks as alternatives to diamonds and iron ores in crafting and smelting recipes:
```json
{
	"additions": [
		"minecraft:emerald",
		"#minecraft:planks"
	],
	"targets": [
		"minecraft:diamond",
		"minecraft:iron_ore"
	],
	"recipes": {
		"types": [
			"crafting_shaped",
			"crafting_shapeless",
			"blasting"
		],
		"ids": [
			"minecraft:/.*/"
		]
	}
}
```

## Modders
If you're a modder and like to use this in your projects you can do so using my maven:

```groovy
repositories {
    maven {
        name "Siphalor's Maven"
        url "https://maven.siphalor.de"
    }
}

dependencies {
    modCompile "de.siphalor:pushtocraft-1.15:+"
    // Optionally you can include this mod into your jar
    include "de.siphalor:pushtocraft-1.15:+"
}
```
