package cc.baka9.catseedtrade;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

class Listeners implements Listener {

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        Trade trade = TradeHelper.getPlayerEnterTrade(player);
        if (trade != null) {
            event.setFormat(event.getFormat().replace("%1$s", (trade.getOwner().equalsIgnoreCase(player.getName()) ? "§3[§6" + trade.getName() + "§3]§r" : "§7[§a" + trade.getName() + "§7]§r") + "%1$s"));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        LivingEntity entity = event.getEntity();
        Player player = entity.getKiller();
        if (player != null) {
            Trade trade = TradeHelper.getPlayerEnterTrade(player);
            if (trade != null) {
                double maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double exp = maxHealth / 20;
                if (exp > 0.1) {
                    TradeHelper.addMemberDedicate(trade, player, exp);
                    trade.setExp(trade.getExp() + exp);
                    while (trade.getExp() >= trade.calcCurrentMaxExp()) {
                        TradeHelper.tradeUpgrade(trade);
                        Notice.sendTradeMemberNotice(trade, "§e工会 " + trade.getName() + " 等级已提升,现在等级 " + trade.getLevel());
                    }
                }

            }
        }

    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event){
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Trade trade = TradeHelper.getPlayerEnterTrade(player);
        if (trade != null) {
            TradeHelper.addMemberDedicate(trade, player, 1);
            trade.setExp(trade.getExp() + 1);
            while (trade.getExp() >= trade.calcCurrentMaxExp()) {
                TradeHelper.tradeUpgrade(trade);
                Notice.sendTradeMemberNotice(trade, "§e工会 " + trade.getName() + " 等级已提升,现在等级 " + trade.getLevel());
            }
        }

    }


}
