package cc.baka9.catseedtrade;

import cc.baka9.catseedtrade.exception.*;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.*;

public class TradeHelper {

    private static final List<Trade> list = new ArrayList<>();

    public static List<Trade> getList(){
        return list;
    }

    /**
     * 工会升级
     */
    public static void tradeUpgrade(Trade trade){
        trade.setLevel(trade.getLevel() + 1);
        trade.setExp(0);
    }

    /**
     * 添加工会成员
     */
    public static void addTradeMember(Trade trade, Player player)
            throws TradeMemberFullException, PlayerJoinedTradeException, TradeHaveThisPlayerException{
        addTradeMember(trade, player.getName());

    }

    /**
     * 添加工会成员
     */
    public static void addTradeMember(Trade trade, String playerName)
            throws TradeHaveThisPlayerException, PlayerJoinedTradeException, TradeMemberFullException{
        Trade playerEnterTrade = getPlayerEnterTrade(playerName);
        if (playerEnterTrade != null) {
            if (playerEnterTrade == trade)
                throw new TradeHaveThisPlayerException(trade.getName() + " 工会已经存在 " + playerName + " 了");
            throw new PlayerJoinedTradeException(playerName, playerEnterTrade);
        }
        if (memberNumberIsFull(trade)) throw new TradeMemberFullException(trade.getName() + " 工会成员已满人");
        trade.getMember().put(playerName, 0.0);
    }

    /**
     * 移除工会成员
     */
    public static void removeTradeMember(Trade trade, Player player) throws TradeNotHaveThisPlayerException, TradeOwnerException{
        removeTradeMember(trade, player.getName());
    }

    /**
     * 移除工会成员
     */
    public static void removeTradeMember(Trade trade, String playerName) throws TradeNotHaveThisPlayerException, TradeOwnerException{
        if (trade.getOwner().equalsIgnoreCase(playerName)) {
            throw new TradeOwnerException(playerName + " 是 " + trade.getName() + " 工会的主人,无法移除");
        }
        Map<String, Double> member = trade.getMember();
        if (member.containsKey(playerName)) {
            trade.getMember().remove(playerName);
        } else {
            throw new TradeNotHaveThisPlayerException(playerName + " 不在 " + trade.getName() + " 工会里");
        }

    }

    /**
     * 获取玩家已经进入的工会
     */
    public static Trade getPlayerEnterTrade(Player player){
        return getPlayerEnterTrade(player.getName());
    }

    /**
     * 获取玩家已经进入的工会
     *
     * @param playerName 玩家名
     * @return 玩家已经进入的工会 没有为null
     */
    public static Trade getPlayerEnterTrade(String playerName){
        for (Trade trade : TradeHelper.list) {
            for (String name : trade.getMember().keySet()) {
                if (name.equalsIgnoreCase(playerName.toLowerCase()))
                    return trade;
            }
        }
        return null;
    }

    /**
     * 工会成员数量是否满人
     *
     * @param trade 工会
     * @return true = 满
     */
    public static boolean memberNumberIsFull(Trade trade){
        return trade.getMember().size() >= trade.calcMaxMemberNumber();
    }

    /**
     * 同意加入工会请求
     */
    public static void tradeAccept(Trade trade, String playerName)
            throws NotRequestException, TradeMemberFullException, PlayerJoinedTradeException, TradeHaveThisPlayerException{
        if (!trade.getRequest().contains(playerName)) {
            throw new NotRequestException(trade.getName() + " 工会没有 " + playerName + " 的加入请求");
        }
        list.forEach(t -> t.getRequest().remove(playerName));
        TradeHelper.addTradeMember(trade, playerName);
    }

    /**
     * 拒绝加入工会请求
     */
    public static void tradeDeny(Trade trade, String playerName) throws NotRequestException{
        System.out.println(trade.getRequest());
        if (!trade.getRequest().contains(playerName)) {
            throw new NotRequestException(trade.getName() + " 工会没有 " + playerName + " 的加入请求");
        }
        trade.getRequest().remove(playerName);
    }

    /**
     * 给工会发送加入请求
     */
    public static void sendRequestToTrade(Trade trade, String playerName) throws SendRequestedException, PlayerJoinedTradeException{
        if (trade.getRequest().contains(playerName)) {
            throw new SendRequestedException(trade.getName() + " 工会已经被 " + playerName + " 申请加入过了，需要等待工会创建者同意");
        }
        Trade playerEnterTrade = getPlayerEnterTrade(playerName);
        if (playerEnterTrade != null) {
            throw new PlayerJoinedTradeException(playerName, playerEnterTrade);
        }
        trade.getRequest().add(playerName);

    }

    /**
     * 根据工会名获取工会对象
     *
     * @param tradeName 公会名
     * @return 工会 没有则为 null
     */
    public static Trade getTrade(String tradeName){
        for (Trade trade : list) {
            if (trade.getName().equalsIgnoreCase(tradeName)) {
                return trade;
            }
        }
        return null;
    }

    public static Trade createTrade(String tradeName, String playerName) throws TradeExistenceException, TradeOwnerException{
        for (Trade trade : list) {
            if (trade.getName().equalsIgnoreCase(tradeName)) {
                throw new TradeExistenceException(tradeName + "工会已经存在了");
            }
            if (trade.getOwner().equalsIgnoreCase(playerName)) {
                throw new TradeOwnerException(playerName + " 已经创建过工会了");

            }
        }
        Trade trade = new Trade(tradeName, "还没有任何介绍~", playerName, Collections.singletonMap(playerName, 0.0), 0, new HashSet<>(), 0);
        list.add(trade);
        return trade;
    }

    public static Trade ownerDisableTrade(String playerName)
            throws PlayerNotJoinTradeException, TradeOwnerNotThisPlayerException{
        Trade trade = getPlayerEnterTrade(playerName);
        if (trade == null) throw new PlayerNotJoinTradeException(playerName);
        if (!trade.getOwner().equalsIgnoreCase(playerName))
            throw new TradeOwnerNotThisPlayerException(playerName + " 不是 " + trade.getName() + " 工会的主人");
        list.remove(trade);
        return trade;
    }

    public static List<Trade> getTopTrades(int start, int last) throws ArrayIndexOutOfBoundsException{
        int tradeCount = TradeHelper.getList().size();
        if (start >= tradeCount) {
            throw new ArrayIndexOutOfBoundsException();
        }
        last = last > tradeCount ? tradeCount : last;
        List<Trade> tradeList = getTopTrades();
        return tradeList.subList(start, last);
    }

    public static List<Trade> getTopTrades() throws ArrayIndexOutOfBoundsException{
        List<Trade> tradeList = new ArrayList<>(TradeHelper.getList());
        tradeList.sort(Comparator.comparing(Trade::getLevel).reversed().thenComparing(Comparator.comparing(Trade::getExp).reversed()));
        return tradeList;
    }

    static Comparator<Map.Entry<String, Double>> comparator = Comparator.comparing(Map.Entry::getValue);

    public static TextComponent getTradeTextComponent(Trade trade){
        StringBuilder showText = new StringBuilder();
        showText.append("§9§l").append(trade.getName()).append(" §r(").append(trade.getBio()).append("§r)\n");
        showText.append("§8成员- ").append(trade.getMember().size()).append(" / ").append(trade.calcMaxMemberNumber()).append("\n");
        showText.append("§d会长- ").append(trade.getOwner()).append(" §a贡献- ").append(trade.getMember().get(trade.getOwner())).append("\n");
        List<Map.Entry<String, Double>> member = new ArrayList<>(trade.getMember().entrySet());
        member.sort(comparator.reversed());
        for (Map.Entry<String, Double> entry : member) {
            if (entry.getKey().equalsIgnoreCase(trade.getOwner())) continue;
            showText.append("§b").append(entry.getKey()).append(" §a贡献- ").append(entry.getValue()).append("\n");
        }
        TextComponent tc = new TextComponent("§l#" + (TradeHelper.getTopTrades().indexOf(trade) + 1) + " §6" + trade.getName() + "§9(§c等级 " + trade.getLevel() + " §e经验 " + trade.getExp() + " / " + trade.calcCurrentMaxExp() + "§9)");
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7" + showText.toString().replace("&", "§")).create()));
        return tc;
    }

    public static void editBio(Trade trade, String bio){
        trade.setBio(bio);
    }

    public static void addMemberDedicate(Trade trade, Player p, double val){
        Map<String, Double> member = trade.getMember();
        String playerName = p.getName();
        member.computeIfPresent(playerName, (k, v) -> Utils.twoDecimal(v + val));
    }


}
