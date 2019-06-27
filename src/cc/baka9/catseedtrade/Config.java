package cc.baka9.catseedtrade;

import cc.baka9.catseedtrade.jfep.Parser;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class Config {
    private static final CatSeedTrade plugin = CatSeedTrade.getInstance();
    private static final File cacheFile = new File(plugin.getDataFolder(), "Cache");
    public static double createTradeMoney;
    public static String maxLevelEquation;
    public static Parser maxLevelEquationParser;

    public static void load(){
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        createTradeMoney = config.getDouble("CreateTradeMoney");
        try {
            maxLevelEquation = config.getString("MaxLevelEquation");
            maxLevelEquationParser = new Parser(maxLevelEquation);
            maxLevelEquationParser.setVariable("level", 0);
            maxLevelEquationParser.getValue();
        } catch (Exception e) {
            CatSeedTrade.getInstance().getLogger().warning("配置文件载入 MaxLevelEquation 出错, 无效的计算公式!");
            maxLevelEquationParser = new Parser("((level - 1) ^ 3 + 60) / 5 * ((level - 1) * 2 + 60)");
        }

    }

    public static void reload(){
        plugin.reloadConfig();
        load();
    }
}
