package cc.baka9.catseedtrade;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static double createTradeMoney;

    public static void load(){
        CatSeedTrade catSeedTrade = CatSeedTrade.getInstance();
        catSeedTrade.saveDefaultConfig();
        FileConfiguration config = catSeedTrade.getConfig();
        createTradeMoney = config.getDouble("CreateTradeMoney");

    }
}
