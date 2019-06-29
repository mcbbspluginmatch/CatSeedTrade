package cc.baka9.catseedtrade;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CatSeedTrade extends JavaPlugin {
    private static CatSeedTrade instance;
    private static Economy economy;

    @Override
    public void onEnable(){
        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        instance = this;
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Listeners(), this);
        getServer().getPluginCommand("trade").setExecutor(new Commands());
        getServer().getPluginCommand("trade").setTabCompleter(new Commands.Tab());
        Config.load();
        Storage.load();
        Notice.start();

    }

    @Override
    public void onDisable(){
        Storage.saveAll();
    }

    public static CatSeedTrade getInstance(){
        return instance;
    }

    public static Economy getEconomy(){
        return economy;
    }

}
