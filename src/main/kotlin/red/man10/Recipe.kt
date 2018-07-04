package red.man10

import org.bukkit.inventory.ItemStack

data class Recipe(
        var inputs: MutableList<ItemStack>,
        var outputs: MutableList<ItemStack>,
        var chanceSets: MutableMap<Skill, ChanceSet>
)