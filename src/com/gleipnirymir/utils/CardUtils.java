package com.gleipnirymir.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class CardUtils {

    public enum Rarity {
        COMMON {
            @Override
            public int getTradeQuantity() {
                return 250;
            }
        },
        RARE {
            @Override
            public int getTradeQuantity() {
                return 50;
            }
        },
        EPIC {
            @Override
            public int getTradeQuantity() {
                return 10;
            }
        },
        LEGENDARY {
            @Override
            public int getTradeQuantity() {
                return 1;
            }
        };

        @Override
        public String toString() {
            return StringUtils.capitalize(name().toLowerCase());
        }

        public abstract int getTradeQuantity();

        public static String[] getRaritiesAsString(){
            return Arrays.stream(values()).map(Enum::toString).toArray(String[]::new);
        }
    }

    private static final Set<String> ARCHERS_ALIASES = initializeSet("archers", "arch");
    private static final Set<String> ARROWS_ALIASES = initializeSet("arrows", "arrow", "arr");
    private static final Set<String> BABY_DRAGON_ALIASES = initializeSet("baby-dragon", "bbd", "babyd", "bd", "babydragon");
    private static final Set<String> BALLOON_ALIASES = initializeSet("balloon", "loon", "balloon");
    private static final Set<String> BANDIT_ALIASES = initializeSet("bandit", "band");
    private static final Set<String> BARBARIAN_BARREL_ALIASES = initializeSet("barbarian-barrel", "bb", "barb-barrel");
    private static final Set<String> BARBARIAN_HUT_ALIASES = initializeSet("barbarian-hut", "barb-hut", "bh", "barbarianhut");
    private static final Set<String> BARBARIANS_ALIASES = initializeSet("barbarians", "barb", "barbs");
    private static final Set<String> BATS_ALIASES = initializeSet("bats", "bat");
    private static final Set<String> BATTLE_HEALER_ALIASES = initializeSet("battle-healer", "healer");
    private static final Set<String> BATTLE_RAM_ALIASES = initializeSet("battle-ram", "br", "ram", "battleram");
    private static final Set<String> BOMB_TOWER_ALIASES = initializeSet("bomb-tower", "bt", "bombtower");
    private static final Set<String> BOMBER_ALIASES = initializeSet("bomber");
    private static final Set<String> BOWLER_ALIASES = initializeSet("bowler");
    private static final Set<String> CANNON_ALIASES = initializeSet("cannon");
    private static final Set<String> CANNON_CART_ALIASES = initializeSet("cannon-cart", "cc", "cart", "cannoncart");
    private static final Set<String> CLONE_ALIASES = initializeSet("clone");
    private static final Set<String> DARK_PRINCE_ALIASES = initializeSet("dark-prince", "dp", "dank-prince", "darkprince");
    private static final Set<String> DART_GOBLIN_ALIASES = initializeSet("dart-goblin", "dg", "dart-gob", "dart-gobs", "dartgoblin");
    private static final Set<String> EARTHQUAKE_ALIASES = initializeSet("earthquake", "eq", "quake");
    private static final Set<String> ELECTRO_DRAGON_ALIASES = initializeSet("electro-dragon", "ed", "edrag", "electrodragon");
    private static final Set<String> ELECTRO_WIZARD_ALIASES = initializeSet("electro-wizard", "ew", "ewiz", "ewizard", "electrowizard");
    private static final Set<String> ELITE_BARBARIANS_ALIASES = initializeSet("elite-barbarians", "eb", "ebarb", "ebarbs", "elitebarbarians");
    private static final Set<String> ELIXIR_COLLECTOR_ALIASES = initializeSet("elixir-collector", "ec", "pump", "collector", "elixircollector");
    private static final Set<String> ELIXIR_GOLEM_ALIASES = initializeSet("elixir-golem", "eg", "egolem");
    private static final Set<String> EXECUTIONER_ALIASES = initializeSet("executioner", "ex", "exe", "exec");
    private static final Set<String> FIRE_SPIRITS_ALIASES = initializeSet("fire-spirits", "fs", "firespirits");
    private static final Set<String> FIREBALL_ALIASES = initializeSet("fireball", "fb");
    private static final Set<String> FIRECRACKER_ALIASES = initializeSet("firecracker", "fc");
    private static final Set<String> FISHERMAN_ALIASES = initializeSet("fisherman", "fish", "fishman", "fisher", "fman");
    private static final Set<String> FLYING_MACHINE_ALIASES = initializeSet("flying-machine", "fm", "fly", "machine", "flyingmachine");
    private static final Set<String> FREEZE_ALIASES = initializeSet("freeze");
    private static final Set<String> FURNACE_ALIASES = initializeSet("furnace");
    private static final Set<String> GIANT_ALIASES = initializeSet("giant");
    private static final Set<String> GIANT_SKELETON_ALIASES = initializeSet("giant-skeleton", "gs", "giantskeleton");
    private static final Set<String> GIANT_SNOWBALL_ALIASES = initializeSet("giant-snowball", "snow", "gsb", "snowball", "giantsnowball");
    private static final Set<String> GOBLIN_BARREL_ALIASES = initializeSet("goblin-barrel", "gb", "gob-barrel", "barrel", "goblinbarrel");
    private static final Set<String> GOBLIN_CAGE_ALIASES = initializeSet("goblin-cage", "gc", "gob-cage", "cage", "goblincage");
    private static final Set<String> GOBLIN_GANG_ALIASES = initializeSet("goblin-gang", "gg", "gob-gang", "goblingang");
    private static final Set<String> GOBLIN_GIANT_ALIASES = initializeSet("goblin-giant", "ggiant", "ggi", "goblingiant");
    private static final Set<String> GOBLIN_HUT_ALIASES = initializeSet("goblin-hut", "gob-hut", "gobhut", "gh", "goblinhut");
    private static final Set<String> GOBLINS_ALIASES = initializeSet("goblins", "gobs", "gob", "stab-gobs", "stab-gob");
    private static final Set<String> GOLEM_ALIASES = initializeSet("golem");
    private static final Set<String> GRAVEYARD_ALIASES = initializeSet("graveyard", "gy", "skillyard");
    private static final Set<String> GUARDS_ALIASES = initializeSet("guards");
    private static final Set<String> HEAL_ALIASES = initializeSet("heal");
    private static final Set<String> HOG_RIDER_ALIASES = initializeSet("hog-rider", "hog", "hogrider");
    private static final Set<String> HUNTER_ALIASES = initializeSet("hunter", "htr", "hu");
    private static final Set<String> ICE_GOLEM_ALIASES = initializeSet("ice-golem", "ig", "icegolem");
    private static final Set<String> ICE_SPIRIT_ALIASES = initializeSet("ice-spirit", "is", "icespirit");
    private static final Set<String> ICE_WIZARD_ALIASES = initializeSet("ice-wizard", "iw", "ice-wiz", "iwiz", "icewizard");
    private static final Set<String> INFERNO_DRAGON_ALIASES = initializeSet("inferno-dragon", "id", "infernodragon");
    private static final Set<String> INFERNO_TOWER_ALIASES = initializeSet("inferno-tower", "inferno", "it", "infernotower");
    private static final Set<String> KNIGHT_ALIASES = initializeSet("knight");
    private static final Set<String> LAVA_HOUND_ALIASES = initializeSet("lava-hound", "lava", "lh", "hound", "lavahound");
    private static final Set<String> LIGHTNING_ALIASES = initializeSet("lightning");
    private static final Set<String> LUMBERJACK_ALIASES = initializeSet("lumberjack", "lj");
    private static final Set<String> MAGIC_ARCHER_ALIASES = initializeSet("magic-archer", "ma", "magicarcher");
    private static final Set<String> MEGA_KNIGHT_ALIASES = initializeSet("mega-knight", "mk", "mknight", "megaknight");
    private static final Set<String> MEGA_MINION_ALIASES = initializeSet("mega-minion", "mm", "meta-minion", "mega", "megaminion");
    private static final Set<String> MINER_ALIASES = initializeSet("miner");
    private static final Set<String> MINI_PEKKA_ALIASES = initializeSet("mini-pekka", "minip", "mini-p", "mp", "minipekka");
    private static final Set<String> MINION_HORDE_ALIASES = initializeSet("minion-horde", "mh", "horde", "minionhorde");
    private static final Set<String> MINIONS_ALIASES = initializeSet("minions");
    private static final Set<String> MIRROR_ALIASES = initializeSet("mirror");
    private static final Set<String> MORTAR_ALIASES = initializeSet("mortar");
    private static final Set<String> MUSKETEER_ALIASES = initializeSet("musketeer", "1m", "musk", "musky");
    private static final Set<String> NIGHT_WITCH_ALIASES = initializeSet("night-witch", "nwitch", "nw", "nightwitch");
    private static final Set<String> PEKKA_ALIASES = initializeSet("pekka");
    private static final Set<String> POISON_ALIASES = initializeSet("poison");
    private static final Set<String> PRINCE_ALIASES = initializeSet("prince");
    private static final Set<String> PRINCESS_ALIASES = initializeSet("princess");
    private static final Set<String> RAGE_ALIASES = initializeSet("rage");
    private static final Set<String> RAM_RIDER_ALIASES = initializeSet("ram-rider", "ramrider");
    private static final Set<String> RASCALS_ALIASES = initializeSet("rascals", "ras", "r");
    private static final Set<String> ROCKET_ALIASES = initializeSet("rocket");
    private static final Set<String> ROYAL_GHOST_ALIASES = initializeSet("royal-ghost", "ghost", "royalghost");
    private static final Set<String> ROYAL_GIANT_ALIASES = initializeSet("royal-giant", "rg", "rgg", "royalgiant");
    private static final Set<String> ROYAL_HOGS_ALIASES = initializeSet("royal-hogs", "rh", "royal-hog", "royalhog", "royalhogs");
    private static final Set<String> ROYAL_RECRUITS_ALIASES = initializeSet("royal-recruits", "rr", "royalrecruits");
    private static final Set<String> SKELETON_ARMY_ALIASES = initializeSet("skeleton-army", "skarmy", "sa", "skeletonarmy");
    private static final Set<String> SKELETON_BARREL_ALIASES = initializeSet("skeleton-barrel", "sb", "sbarrel", "skeletonbarrel");
    private static final Set<String> SKELETONS_ALIASES = initializeSet("skeletons", "skele", "sk");
    private static final Set<String> SPARKY_ALIASES = initializeSet("sparky");
    private static final Set<String> SPEAR_GOBLINS_ALIASES = initializeSet("spear-goblins", "spear-gobs", "spear-gob", "sgobs", "sgob", "sg", "spear", "speargobs", "speargoblins");
    private static final Set<String> TESLA_ALIASES = initializeSet("tesla");
    private static final Set<String> THE_LOG_ALIASES = initializeSet("the-log", "log", "thelog");
    private static final Set<String> THREE_MUSKETEERS_ALIASES = initializeSet("three-musketeers", "3m", "3musk", "3musks", "muskies", "threemusketeers");
    private static final Set<String> TOMBSTONE_ALIASES = initializeSet("tombstone", "ts");
    private static final Set<String> TORNADO_ALIASES = initializeSet("tornado", "nado");
    private static final Set<String> VALKYRIE_ALIASES = initializeSet("valkyrie", "valk");
    private static final Set<String> WALL_BREAKERS_ALIASES = initializeSet("wall-breakers", "wall-breaker", "wb", "wallbreakers");
    private static final Set<String> WITCH_ALIASES = initializeSet("witch");
    private static final Set<String> WIZARD_ALIASES = initializeSet("wizard", "wiz");
    private static final Set<String> X_BOW_ALIASES = initializeSet("x-bow", "xbow");
    private static final Set<String> ZAP_ALIASES = initializeSet("zap");
    private static final Set<String> ZAPPIES_ALIASES = initializeSet("zappies", "zp");

    public static final Map<Set<String>, String> ALIASES_MAP = initializeMap();

    public static String getCardKey(String cardNameParam) {
        String cardName = getDefaultCardKey(cardNameParam);
        Set<String> cardAlias = ALIASES_MAP.keySet().stream()
                .filter(aliasSet -> aliasSet.contains(cardName))
                .findAny()
                .orElse(Collections.singleton(""));

        return ALIASES_MAP.get(cardAlias) == null ? cardName : ALIASES_MAP.get(cardAlias);
    }

    private static String getDefaultCardKey(String cardName) {
        return cardName.replace(" ", "-").replace(".", "").toLowerCase();
    }

    private static Set<String> initializeSet(String... aliases) {
        return new HashSet<>(Arrays.asList(aliases));
    }

    private static Map<Set<String>, String> initializeMap() {
        Map<Set<String>, String> map = new HashMap<>();
        map.put(ARCHERS_ALIASES, "archers");
        map.put(ARROWS_ALIASES, "arrows");
        map.put(BABY_DRAGON_ALIASES, "baby-dragon");
        map.put(BALLOON_ALIASES, "balloon");
        map.put(BANDIT_ALIASES, "bandit");
        map.put(BARBARIAN_BARREL_ALIASES, "barbarian-barrel");
        map.put(BARBARIAN_HUT_ALIASES, "barbarian-hut");
        map.put(BARBARIANS_ALIASES, "barbarians");
        map.put(BATS_ALIASES, "bats");
        map.put(BATTLE_HEALER_ALIASES, "battle-healer");
        map.put(BATTLE_RAM_ALIASES, "battle-ram");
        map.put(BOMB_TOWER_ALIASES, "bomb-tower");
        map.put(BOMBER_ALIASES, "bomber");
        map.put(BOWLER_ALIASES, "bowler");
        map.put(CANNON_ALIASES, "cannon");
        map.put(CANNON_CART_ALIASES, "cannon-cart");
        map.put(CLONE_ALIASES, "clone");
        map.put(DARK_PRINCE_ALIASES, "dark-prince");
        map.put(DART_GOBLIN_ALIASES, "dart-goblin");
        map.put(EARTHQUAKE_ALIASES, "earthquake");
        map.put(ELECTRO_DRAGON_ALIASES, "electro-dragon");
        map.put(ELECTRO_WIZARD_ALIASES, "electro-wizard");
        map.put(ELITE_BARBARIANS_ALIASES, "elite-barbarians");
        map.put(ELIXIR_COLLECTOR_ALIASES, "elixir-collector");
        map.put(ELIXIR_GOLEM_ALIASES, "elixir-golem");
        map.put(EXECUTIONER_ALIASES, "executioner");
        map.put(FIRE_SPIRITS_ALIASES, "fire-spirits");
        map.put(FIREBALL_ALIASES, "fireball");
        map.put(FIRECRACKER_ALIASES, "firecracker");
        map.put(FISHERMAN_ALIASES, "fisherman");
        map.put(FLYING_MACHINE_ALIASES, "flying-machine");
        map.put(FREEZE_ALIASES, "freeze");
        map.put(FURNACE_ALIASES, "furnace");
        map.put(GIANT_ALIASES, "giant");
        map.put(GIANT_SKELETON_ALIASES, "giant-skeleton");
        map.put(GIANT_SNOWBALL_ALIASES, "giant-snowball");
        map.put(GOBLIN_BARREL_ALIASES, "goblin-barrel");
        map.put(GOBLIN_CAGE_ALIASES, "goblin-cage");
        map.put(GOBLIN_GANG_ALIASES, "goblin-gang");
        map.put(GOBLIN_GIANT_ALIASES, "goblin-giant");
        map.put(GOBLIN_HUT_ALIASES, "goblin-hut");
        map.put(GOBLINS_ALIASES, "goblins");
        map.put(GOLEM_ALIASES, "golem");
        map.put(GRAVEYARD_ALIASES, "graveyard");
        map.put(GUARDS_ALIASES, "guards");
        map.put(HEAL_ALIASES, "heal");
        map.put(HOG_RIDER_ALIASES, "hog-rider");
        map.put(HUNTER_ALIASES, "hunter");
        map.put(ICE_GOLEM_ALIASES, "ice-golem");
        map.put(ICE_SPIRIT_ALIASES, "ice-spirit");
        map.put(ICE_WIZARD_ALIASES, "ice-wizard");
        map.put(INFERNO_DRAGON_ALIASES, "inferno-dragon");
        map.put(INFERNO_TOWER_ALIASES, "inferno-tower");
        map.put(KNIGHT_ALIASES, "knight");
        map.put(LAVA_HOUND_ALIASES, "lava-hound");
        map.put(LIGHTNING_ALIASES, "lightning");
        map.put(LUMBERJACK_ALIASES, "lumberjack");
        map.put(MAGIC_ARCHER_ALIASES, "magic-archer");
        map.put(MEGA_KNIGHT_ALIASES, "mega-knight");
        map.put(MEGA_MINION_ALIASES, "mega-minion");
        map.put(MINER_ALIASES, "miner");
        map.put(MINI_PEKKA_ALIASES, "mini-pekka");
        map.put(MINION_HORDE_ALIASES, "minion-horde");
        map.put(MINIONS_ALIASES, "minions");
        map.put(MIRROR_ALIASES, "mirror");
        map.put(MORTAR_ALIASES, "mortar");
        map.put(MUSKETEER_ALIASES, "musketeer");
        map.put(NIGHT_WITCH_ALIASES, "night-witch");
        map.put(PEKKA_ALIASES, "pekka");
        map.put(POISON_ALIASES, "poison");
        map.put(PRINCE_ALIASES, "prince");
        map.put(PRINCESS_ALIASES, "princess");
        map.put(RAGE_ALIASES, "rage");
        map.put(RAM_RIDER_ALIASES, "ram-rider");
        map.put(RASCALS_ALIASES, "rascals");
        map.put(ROCKET_ALIASES, "rocket");
        map.put(ROYAL_GHOST_ALIASES, "royal-ghost");
        map.put(ROYAL_GIANT_ALIASES, "royal-giant");
        map.put(ROYAL_HOGS_ALIASES, "royal-hogs");
        map.put(ROYAL_RECRUITS_ALIASES, "royal-recruits");
        map.put(SKELETON_ARMY_ALIASES, "skeleton-army");
        map.put(SKELETON_BARREL_ALIASES, "skeleton-barrel");
        map.put(SKELETONS_ALIASES, "skeletons");
        map.put(SPARKY_ALIASES, "sparky");
        map.put(SPEAR_GOBLINS_ALIASES, "spear-goblins");
        map.put(TESLA_ALIASES, "tesla");
        map.put(THE_LOG_ALIASES, "the-log");
        map.put(THREE_MUSKETEERS_ALIASES, "three-musketeers");
        map.put(TOMBSTONE_ALIASES, "tombstone");
        map.put(TORNADO_ALIASES, "tornado");
        map.put(VALKYRIE_ALIASES, "valkyrie");
        map.put(WALL_BREAKERS_ALIASES, "wall-breakers");
        map.put(WITCH_ALIASES, "witch");
        map.put(WIZARD_ALIASES, "wizard");
        map.put(X_BOW_ALIASES, "x-bow");
        map.put(ZAP_ALIASES, "zap");
        map.put(ZAPPIES_ALIASES, "zappies");
        return map;
    }

}
