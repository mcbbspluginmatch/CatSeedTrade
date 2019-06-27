package cc.baka9.catseedtrade;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Storage {
    private static CatSeedTrade catSeedTrade = CatSeedTrade.getInstance();

    static{
        catSeedTrade.getServer().getScheduler().runTaskTimerAsynchronously(catSeedTrade, Storage::saveAll, 20L * 60, 20L * 60);
    }

    public static void saveOne(Trade trade){
        try {
            File file = getTradeFile(trade);
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            configuration.set("name", trade.getName());
            configuration.set("bio", trade.getBio());
            configuration.set("owner", trade.getOwner());
            Map<String, Double> member = trade.getMember();
            configuration.set("member", null);
            for (String playerName : member.keySet()) {
                configuration.set("member." + playerName, member.get(playerName));
            }
            configuration.set("member", trade.getMember());
            configuration.set("level", trade.getLevel());
            configuration.set("request", new ArrayList<>(trade.getRequest()));
            configuration.set("exp",trade.getExp());
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void delOne(Trade trade){
        try {
            File file = getTradeFile(trade);
            if (file.delete()) catSeedTrade.getLogger().info("已删除文件 " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(){
        List<Trade> list = TradeHelper.getList();
        File dataFolder = new File(catSeedTrade.getDataFolder(), "data");
        if (!dataFolder.exists()) return;
        File[] files = dataFolder.listFiles();
        if (files == null) return;
        for (File file : files) {
            FileConfiguration conf = YamlConfiguration.loadConfiguration(file);
            Map<String, Double> member = new HashMap<>();
            conf.getConfigurationSection("member").getKeys(false).forEach(playerName -> member.put(playerName, conf.getDouble("member." + playerName)));
            Trade trade = new Trade(conf.getString("name"), conf.getString("bio"), conf.getString("owner"),
                    member, conf.getInt("level"), new HashSet<>(conf.getStringList("request")),conf.getDouble("exp"));
            list.add(trade);

        }

    }

    public static void saveAll(){
        TradeHelper.getList().forEach(Storage::saveOne);

    }

    private static File getTradeFile(Trade trade) throws IOException{
        File dataFolder = new File(catSeedTrade.getDataFolder(), "data");
        if (!dataFolder.exists() && dataFolder.mkdirs()) {
            catSeedTrade.getLogger().info("已新建目录 " + dataFolder.getAbsolutePath());
        }
        File file = new File(dataFolder, trade.getName() + ".yml");

        if (!file.exists() && file.createNewFile()) {
            catSeedTrade.getLogger().info("已新建文件 " + file.getAbsolutePath());
        }
        return file;

    }

}
