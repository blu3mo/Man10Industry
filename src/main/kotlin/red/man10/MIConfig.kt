package red.man10

import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

class MIConfig {

    var util: MIUtility? = null

    companion object {
        var pl: MIPlugin? = null
    }

    fun initialize(plugin: MIPlugin): MIConfig {
        pl = plugin

        util = pl!!.util

        return MIConfig()
    }

    fun loadAll(cs: CommandSender) {
        cs.sendMessage(pl!!.prefix + "§bLoading all configurations...")
        loadChanceSets(cs)
        loadSkills(cs)
        loadRecipes(cs)
    }

    fun loadChanceSets(cs: CommandSender) {
        val file = loadFile("chancesets", cs)

        pl!!.chanceSets.clear()
        val ymlFile = YamlConfiguration.loadConfiguration(file)
        val chanceSetKeys = ymlFile.getKeys(false)
        cs.sendMessage(pl!!.prefix + "§eChanceSets:")
        for (chanceSetKey in chanceSetKeys) {
            val isCorrect = (
                    ymlFile.getKeys(true).contains(chanceSetKey + ".req") &&
                    ymlFile.getKeys(true).contains(chanceSetKey + ".map")
                    )
            if (isCorrect) {
//                val chanceSetMapKeys = ymlFile.getKeys(true).filter { it.startsWith(chanceSetKey + ".map.") }
//                val chanceSetMap = mutableMapOf<Int, Double>()
//                for (chanceSetMapKey in chanceSetMapKeys) {
//                    chanceSetMap.put(
//                            chanceSetMapKey.replace(chanceSetKey + ".map.", "").toInt(), //レベル
//                            ymlFile.getDouble(chanceSetMapKey) //確率
//                    )
//                }
                val map = getItemsUnderPath(ymlFile, chanceSetKey + ".map.") as MutableMap<Int, Double>

                val newChanceSet = ChanceSet(
                        ymlFile.getInt(chanceSetKey + "req"),
                        map
                )
                pl!!.chanceSets.put(chanceSetKey, newChanceSet)
                cs.sendMessage(pl!!.prefix + "§a" + chanceSetKey + " ○")
            } else {
                cs.sendMessage(pl!!.prefix + "§c" + chanceSetKey + " ×")
            }
        }
        print(pl!!.chanceSets)
    }

    fun loadSkills(cs: CommandSender) {
        val file = loadFile("skills", cs)
        val ymlFile = YamlConfiguration.loadConfiguration(file)

        pl!!.skills.clear()

        cs.sendMessage(pl!!.prefix + "§eSkills:")
        var newSkills = mutableListOf<Skill>()
        var skillGenre = SkillGenre.Craft
        for (i in 1..12) {
            val skillName = ymlFile.getString(i.toString())
            if (skillName != null) {
                val newSkill = Skill(skillName, skillGenre)
                newSkills.add(newSkill)
                cs.sendMessage(pl!!.prefix + "§a" + i + " ○")
            } else {
                cs.sendMessage(pl!!.prefix + "§a" + i + " ×")
            }
            when {
                i <= 0 -> skillGenre = SkillGenre.Craft
                i <= 4 -> skillGenre = SkillGenre.Magic
                i <= 8 -> skillGenre = SkillGenre.Study
            }
        }
        pl!!.skills = newSkills
    }

    fun loadRecipes(cs: CommandSender) {
        val file = loadFile("recipes", cs)

        pl!!.recipies.clear()
        val ymlFile = YamlConfiguration.loadConfiguration(file)
        val recipeKeys = ymlFile.getKeys(false)
        cs.sendMessage(pl!!.prefix + "§eRecipes:")
        for (recipeKey in recipeKeys) {
            //try {
            print(ymlFile.getKeys(true))
            val isCorrect: Boolean = (
                    ymlFile.getKeys(true).contains(recipeKey + ".inputs") &&
                            ymlFile.getKeys(true).contains(recipeKey + ".outputs") &&
                            ymlFile.getKeys(true).contains(recipeKey + ".chancesets")
                    )
            if (isCorrect) {
                var newInputs = mutableListOf<ItemStack>()
                if (ymlFile.getString(recipeKey + ".inputs") != "") {
                    newInputs = util!!.itemStackArrayFromBase64(ymlFile.getString(recipeKey + ".inputs"))
                }
                var newOutputs = mutableListOf<ItemStack>()
                if (ymlFile.getString(recipeKey + ".outputs") != "") {
                    newOutputs = util!!.itemStackArrayFromBase64(ymlFile.getString(recipeKey + ".outputs"))
                }
                var newChanceSets = mutableMapOf<Skill, ChanceSet>()
                val stringChanceSets = getItemsUnderPath(ymlFile, ".chancesets.")//ymlFile.getKeys(true).filter { it.startsWith(recipeKey + ".chancesets.") }
                for (stringChanceSet in stringChanceSets) {
                    newChanceSets.put(pl!!.skills[stringChanceSet.key.toInt()], pl!!.chanceSets[stringChanceSet.value]!!)
                }
                val newRecipe = Recipe(
                        newInputs,
                        newOutputs,
                        newChanceSets
                )
                pl!!.recipies.put(recipeKey, newRecipe)
                cs.sendMessage(pl!!.prefix + "§a" + recipeKey + " ○")
            } else {
                cs.sendMessage(pl!!.prefix + "§c" + recipeKey + " ×")
            }
        }
        print(pl!!.chanceSets)


    }

    fun loadMachines(cs: CommandSender) {

    }

    fun loadFile(name: String, cs: CommandSender): File {
        val file = File(pl!!.dataFolder.path + "/" + name + ".yml")
        if (!file.exists()) {
            file.createNewFile()
            cs.sendMessage(pl!!.prefix + "Generated " + name + ".yml")
        }
        return file
    }

    fun getItemsUnderPath(yaml: YamlConfiguration, path: String): MutableMap<String, Any> {
        val allKeys = yaml.getKeys(true)
        val keysUnderPath = allKeys.filter { it.startsWith(path) } as MutableList
        val items = mutableMapOf<String, Any>()
        for (keyUnderPath in keysUnderPath) {
            items.put(
                    keyUnderPath.replace(path, ""),
                    yaml.get(keyUnderPath)
            )
        }
        return items
    }

//    fun loadRecipes(cs: CommandSender) {
//        val dir = File(pl!!.dataFolder.path + "/recipes")
//        if (!dir.exists()) {
//            dir.mkdir()
//        }
//
//        val recipeFiles= dir.listFiles()
//        for (recipeFile in recipeFiles) {
//
//            val ymlFile = YamlConfiguration.loadConfiguration(recipeFile)
//            val isCorrect = (ymlFile.getString("chance") != null &&
//                            ymlFile.getKeys(false).contains("chance"))
//            val recipeKey = recipeFile.nameWithoutExtension
//
//            if (isCorrect) {
//                var newRecipe = Recipe()
//                var patternKeys = ymlFile.getKeys(false)
//                for (patternKey in patternKeys) {
//                    //newRecipe.patterns.put()
//                }
//                cs.sendMessage(pl!!.prefix + "レシピ" + recipeKey + "をロードしました")
//            } else {
//                cs.sendMessage(pl!!.prefix + "ERROR: レシピ" + recipeKey + "はロードできません")
//            }
//
//        }
//    }
//
//    fun loadMachines(cs: CommandSender) {
//        val dir = File(pl!!.dataFolder.path + "/machine")
//        if (!dir.exists()) {
//            dir.mkdir()
//        }
//
//        val machineFiles= dir.listFiles()
//        for (machineFile in machineFiles) {
//            val ymlFile = YamlConfiguration.loadConfiguration(machineFile)
//            val isCorrect = (
//                    ymlFile.getString("name") != null &&
//                            ymlFile.getStringList("recipes") != null
//                    )
//            val machineKey = machineFile.nameWithoutExtension
//            if (isCorrect) {
//                var newMachine = Machine()
//                newMachine.name = ymlFile.getString("name")
//                cs.sendMessage(pl!!.prefix + "マシン" + machineKey + "をロードしました")
//            } else {
//                cs.sendMessage(pl!!.prefix + "ERROR: マシン" + machineKey + "は読み込めません")
//            }
//        }
//    }
//
//    fun loadSkills() {
//        var dir = File(pl!!.dataFolder.toString() + "/skill")
//    }
}