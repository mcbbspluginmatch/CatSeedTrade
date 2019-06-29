package cc.baka9.catseedtrade;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class Notice {

    private static CatSeedTrade catSeedTrade = CatSeedTrade.getInstance();
    private static BukkitScheduler scheduler = catSeedTrade.getServer().getScheduler();
    private static File file;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void start(){
        requestStart();
        otherStart();

    }

    public static void sendPlayerNotice(String playerName, String noticeMessage){
        noticeMessage = noticeMessage.replace("&", "§");
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline() && player.getName().equals(playerName)) {
            player.sendMessage(noticeMessage);
        } else {
            try {
                FileConfiguration conf = YamlConfiguration.loadConfiguration(getNoticeFile());
                List<String> noticeMessages = conf.getStringList(playerName);
                noticeMessages = noticeMessages == null ? new ArrayList<>() : noticeMessages;
                noticeMessages.add(sdf.format(new Date()) + " " + noticeMessage);
                conf.set(playerName, noticeMessages);
                conf.save(getNoticeFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendTradeMemberNotice(Trade trade, String noticeMessage){
        trade.getMember().keySet().forEach(name -> Notice.sendPlayerNotice(name, noticeMessage));
    }

    private static void requestStart(){
        scheduler.runTaskTimerAsynchronously(catSeedTrade, () -> TradeHelper.getList().forEach(trade -> {
            Set<String> request = trade.getRequest();
            if (request.size() > 0) {
                Player player = Bukkit.getPlayer(trade.getOwner());
                if (player != null && player.isOnline() && player.getName().equalsIgnoreCase(trade.getOwner())) {
                    request.forEach(name -> player.sendMessage("§e玩家" + name + "请求加入你的工会"));
                    player.sendMessage("§3输入 /trade accept 同意 或 /trade deny 拒绝");

                }
            }
        }), 0L, 20L * 60);
    }

    private static void otherStart(){
        catSeedTrade.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event){
                Player player = event.getPlayer();
                scheduler.runTaskLaterAsynchronously(catSeedTrade, () -> TradeHelper.getList().forEach(trade -> {
                    try {
                        FileConfiguration conf = YamlConfiguration.loadConfiguration(getNoticeFile());
                        Set<String> names = conf.getKeys(false);
                        String playerName = player.getName();
                        if (names.size() > 0 && names.contains(playerName)) {
                            Player taskPlayer = Bukkit.getPlayer(playerName);
                            if (taskPlayer != null && taskPlayer.isOnline() && taskPlayer.getName().equalsIgnoreCase(playerName)) {
                                conf.getStringList(playerName).forEach(player::sendMessage);
                                conf.set(playerName, null);
                            }
                            conf.save(file);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }), 60L);
            }
        }, catSeedTrade);
    }


    private static File getNoticeFile() throws IOException{
        if (Notice.file != null) return Notice.file;
        File dataFolder = catSeedTrade.getDataFolder();
        if (!dataFolder.exists() && dataFolder.mkdirs()) {
            catSeedTrade.getLogger().info("已新建目录 " + dataFolder.getAbsolutePath());
        }
        File file = new File(dataFolder, "Notice.yml");
        if (!file.exists() && file.createNewFile()) {
            catSeedTrade.getLogger().info("已新建文件 " + file.getAbsolutePath());
        }
        Notice.file = file;
        return file;

    }
}
