package cc.baka9.catseedtrade;

import cc.baka9.catseedtrade.exception.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (args.length > 0 && sender instanceof Player) {
            Player player = (Player) sender;
            String pName = player.getName();
            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length > 1) {
                        try {
                            String tradeName = ChatColor.stripColor(args[1]);
                            if (tradeName.length() <= 8) {

                                // 直接执行扣款操作并检测返回结果是否成功 —— 754503921

                                if (CatSeedTrade.getEconomy().getBalance(player) < Config.createTradeMoney) {
                                    player.sendMessage("§e创建公会游戏币不足,需要游戏币" + Config.createTradeMoney);
                                    return true;
                                }

                                Trade trade = TradeHelper.createTrade(tradeName, pName);
                                EconomyResponse er = CatSeedTrade.getEconomy().withdrawPlayer(player, Config.createTradeMoney);
                                if (er.transactionSuccess()) {
                                    player.sendMessage("§e消费了" + Config.createTradeMoney + "已经成功创建工会" + tradeName);
                                    Storage.saveOne(trade);
                                }
                            } else {
                                player.sendMessage("§e工会名字不能过长");
                            }
                        } catch (TradeExistenceException e) {
                            player.sendMessage(e.getMessage());
                        } catch (TradeOwnerException e) {
                            player.sendMessage("§e你已经创建过一个工会了");
                        }
                        return true;
                    }
                    break;
                case "info":
                    if (args.length > 1) {
                        Trade trade = TradeHelper.getTrade(args[1]);
                        if (trade == null) {
                            player.sendMessage("§e查询的工会 " + args[1] + " 不存在");
                        } else {
                            player.spigot().sendMessage(TradeHelper.getTradeTextComponent(trade));
                        }
                    } else {
                        Trade trade = TradeHelper.getPlayerEnterTrade(pName);
                        if (trade == null) {
                            player.sendMessage("§e你没有加入任何工会");
                        } else {
                            player.spigot().sendMessage(TradeHelper.getTradeTextComponent(trade));
                        }
                    }
                    return true;
                case "player":
                    if (args.length > 1) {
                        Trade trade = TradeHelper.getPlayerEnterTrade(args[1]);
                        if (trade == null) {
                            player.sendMessage("§e你查询的指定玩家没有加入任何公会");
                        } else {
                            player.spigot().sendMessage(TradeHelper.getTradeTextComponent(trade));
                        }
                        return true;
                    }
                    break;
                case "join":
                    if (args.length > 1) {
                        Trade enterTrade = TradeHelper.getTrade(args[1]);
                        if (enterTrade == null) {
                            player.sendMessage("§e请求进入的工会 " + args[1] + " 不存在");
                        } else {
                            try {
                                TradeHelper.sendRequestToTrade(enterTrade, pName);
                                player.sendMessage("§e你给工会 " + args[1] + " 发送了进入请求");
                                Notice.sendPlayerNotice(enterTrade.getOwner(), "§e玩家 " + pName + " 请求加入你的工会");
                                Storage.saveOne(enterTrade);
                            } catch (SendRequestedException e) {
                                player.sendMessage("§e你已经给工会 " + args[1] + " 发送过进入请求了");
                            } catch (PlayerJoinedTradeException e) {
                                player.sendMessage("§e你已经加入了工会 " + args[1] + " 了,不能再加入别的工会了");
                            }
                        }
                        return true;
                    }
                    break;
                case "leave":
                    if (args.length > 1) {
                        Trade leaveTrade = TradeHelper.getTrade(args[1]);
                        if (leaveTrade == null) {
                            player.sendMessage("§e请求退出的工会 " + args[1] + " 不存在");
                        } else {
                            try {
                                TradeHelper.removeTradeMember(leaveTrade, pName);
                                player.sendMessage("§e你离开了 " + args[1] + " 工会");
                                leaveTrade.getMember().keySet().forEach(member
                                        -> Notice.sendPlayerNotice(member, "§e玩家 " + pName + " 离开了 " + args[1] + " 工会"));
                                Storage.saveOne(leaveTrade);
                            } catch (TradeNotHaveThisPlayerException e) {
                                player.sendMessage("§e你不在 " + args[1] + " 工会里面");
                            } catch (TradeOwnerException e) {
                                player.sendMessage("§e你是工会的主人,无法退出工会");
                            }
                        }
                        return true;
                    }
                    break;
                case "disable":
                    try {
                        Trade disableTrade = TradeHelper.ownerDisableTrade(pName);
                        player.sendMessage("§e你已经解散了 " + disableTrade.getName());
                        disableTrade.getMember().keySet().forEach(member
                                -> Notice.sendPlayerNotice(member, "§e玩家" + pName + " 解散了 " + disableTrade.getName() + " 工会"));
                        Storage.delOne(disableTrade);
                    } catch (PlayerNotJoinTradeException e) {
                        player.sendMessage("§e你没有加入任何工会");
                    } catch (TradeOwnerNotThisPlayerException e) {
                        player.sendMessage("§e你不是这个工会的主人,不能擅自解散");
                    }
                    return true;
                case "accept":
                    if (args.length > 1) {
                        Trade trade = TradeHelper.getPlayerEnterTrade(pName);
                        if (trade == null) {
                            player.sendMessage("§e你没有加入任何工会");
                        } else if (!trade.getOwner().equalsIgnoreCase(pName)) {
                            player.sendMessage("§e你不是工会的主人,所以无法接受请求");
                        } else {
                            try {
                                TradeHelper.tradeAccept(trade, args[1]);
                                player.sendMessage("§e你同意了 " + args[1] + "的加入请求");
                                Notice.sendPlayerNotice(args[1], "§e玩家 " + pName + " 已经同意你进入 " + trade.getName() + " 工会");
                                Storage.saveOne(trade);
                            } catch (NotRequestException e) {
                                player.sendMessage("§e没有玩家 " + args[1] + " 的加入请求");
                            } catch (TradeMemberFullException e) {
                                player.sendMessage("§e工会已经满员,赶快升级吧");
                                Notice.sendPlayerNotice(args[1], "§e" + trade.getName() + " 工会成员已满");
                            } catch (PlayerJoinedTradeException e) {
                                player.sendMessage("§e玩家 " + args[1] + " 已经加入到了别的工会了");
                            } catch (TradeHaveThisPlayerException e) {
                                player.sendMessage("§e玩家 " + args[1] + " 已经存在于这个工会了");
                            }

                        }
                        return true;
                    }
                    break;
                case "deny":
                    if (args.length > 1) {
                        Trade trade = TradeHelper.getPlayerEnterTrade(pName);
                        if (trade == null) {
                            player.sendMessage("§e你没有加入任何工会");
                        } else if (!trade.getOwner().equalsIgnoreCase(pName)) {
                            player.sendMessage("§e你不是工会的主人,所以无法接受请求");
                        } else {
                            try {
                                TradeHelper.tradeDeny(trade, args[1]);
                                player.sendMessage("§e你拒绝了 " + args[1] + " 的加入请求");
                                Notice.sendPlayerNotice(args[1], "§e玩家 " + pName + " 拒绝你进入 " + trade.getName() + " 工会");
                                Storage.saveOne(trade);
                            } catch (NotRequestException e) {
                                player.sendMessage("§e没有玩家 " + args[1] + " 的加入请求");
                            }

                        }
                        return true;
                    }
                    break;
                case "tops":
                    int page = 1;
                    try {
                        if (args.length > 1) {
                            page = Math.max(1, Math.abs(Integer.parseInt(args[1])));
                        }
                        int eachCount = 6;
                        int count = eachCount * page;
                        try {
                            player.sendMessage("§e§l工会排行§9§l(鼠标放在文字上查看详情)");
                            for (Trade trade : TradeHelper.getTopTrades(count - eachCount, count)) {
                                TextComponent tc = TradeHelper.getTradeTextComponent(trade);
                                player.spigot().sendMessage(tc);
                            }
                            player.sendMessage("§e当前页数 " + page + " / " + (int) Math.ceil(TradeHelper.getList().size() / (double) eachCount));
                        } catch (ArrayIndexOutOfBoundsException arrE) {
                            player.sendMessage("§c你输入的页数过大");
                        }
                    } catch (NumberFormatException e) {
                        player.sendMessage("§c请输入正确的页数");
                    }
                    return true;
                case "kick":
                    if (args.length > 1) {
                        Trade trade = TradeHelper.getPlayerEnterTrade(pName);
                        if (trade == null) {
                            player.sendMessage("§e你没有加入任何工会");
                        } else if (!trade.getOwner().equalsIgnoreCase(pName)) {
                            player.sendMessage("§e你不是工会的主人");
                        } else {
                            try {
                                TradeHelper.removeTradeMember(trade, args[1]);
                                player.sendMessage("§e你从工会里踢出了 " + args[1]);
                                Notice.sendPlayerNotice(args[1], "§e玩家" + pName + "将你从工会" + trade.getName() + "踢出");
                                Storage.saveOne(trade);
                            } catch (TradeNotHaveThisPlayerException e) {
                                player.sendMessage("§e玩家 " + args[1] + " 不在你的工会里");
                            } catch (TradeOwnerException e) {
                                player.sendMessage("§e你无法踢出自己");
                            }
                        }
                        return true;
                    }
                    break;
                case "bio":
                    if (args.length > 1) {
                        Trade trade = TradeHelper.getPlayerEnterTrade(pName);
                        if (trade == null) {
                            player.sendMessage("§e你没有加入任何工会");
                        } else if (!trade.getOwner().equalsIgnoreCase(pName)) {
                            player.sendMessage("§e你不是工会的主人,所以无法修改介绍标语");
                        } else if (args[1].length() <= 30) {

                            TradeHelper.editBio(trade, args[1]);
                            player.sendMessage("§e标语设置成功~");
                            Storage.saveOne(trade);
                        } else {
                            player.sendMessage("§4标语长度过长!");
                        }
                        return true;
                    }
            }

        }
        sender.sendMessage("§e创建工会需要 " + Config.createTradeMoney + " 游戏币");

        if (sender instanceof Player) {
            sendHelp((Player) sender);
            return true;
        }

        return false;
    }

    private void sendHelp(Player player){
        Trade trade = TradeHelper.getPlayerEnterTrade(player);
        if (trade == null) {
            sendNotJoinHelp(player);
        } else if (trade.getOwner().equals(player.getName())) {
            sendOwnerHelp(player);
        } else {
            sendJoinedHelp(player);
        }


    }

    private void sendJoinedHelp(CommandSender sender){
        String sb = "§a/Trade info §b<工会名> §c查看指定公会详情\n" +
                "§a/Trade player §b<玩家名> §c查看这个玩家所在的公会详情\n" +
                "§4/Trade leave <工会名> 离开工会\n" +
                "§a/Trade tops <页数> §c查看工会排行";
        sender.sendMessage(sb);
    }

    private void sendNotJoinHelp(CommandSender sender){
        String sb = "§a/Trade create §b<工会名> §c创建工会\n" +
                "§a/Trade info §b<工会名> §c查看指定公会详情\n" +
                "§a/Trade player §b<玩家名> §c查看这个玩家所在的公会详情\n" +
                "§a/Trade join §b<工会名> §c请求进入工会\n" +
                "§a/Trade tops <页数> §c查看工会排行";
        sender.sendMessage(sb);
    }

    private void sendOwnerHelp(CommandSender sender){
        String sb = "§a/Trade info §b<工会名> §c查看指定公会详情\n" +
                "§a/Trade player §b<玩家名> §c查看这个玩家所在的公会详情\n" +
                "§a/Trade kick §b<玩家名> §c将玩家从工会踢出\n" +
                "§a/Trade accept §b<玩家名> §c同意玩家进入工会\n" +
                "§a/Trade deny §b<玩家名> §c拒绝玩家进入工会\n" +
                "§a/Trade tops <页数> §c查看工会排行\n" +
                "§a/Trade bio <介绍> §c设置工会的介绍标语\n" +
                "§4/Trade disable 解散自己的工会\n";
        sender.sendMessage(sb);
    }

    //"create", "info", "player", "kick", "join", "leave", "disable", "accept", "deny", "tops", "bio"
    public static class Tab implements TabCompleter {
        List<String> empty = new ArrayList<>();
        List<String> arg_1_joined = Arrays.asList("info <工会名> 查看指定公会详情", "player <玩家名> 查看这个玩家所在的公会详情", "leave <工会名> 离开工会", "tops <页数> 查看工会排行");
        List<String> arg_1_noJoin = Arrays.asList("create <工会名> 创建工会", "info <工会名> 查看指定公会详情", "player <玩家名> 查看这个玩家所在的公会详情", "join <工会名> 请求进入工会", "tops <页数> 查看工会排行");
        List<String> arg_1_owner = Arrays.asList("info <工会名> 查看指定公会详情", "player <玩家名> 查看这个玩家所在的公会详情", "kick <玩家名> 将玩家从工会踢出", "accept <玩家名> 同意玩家进入工会", "deny <玩家名> 拒绝玩家进入工会", "tops <页数> 查看工会排行", "bio <介绍> 设置工会的介绍标语");

        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args){
            if (!(commandSender instanceof Player)) return empty;
            Player player = (Player) commandSender;
            Trade trade = TradeHelper.getPlayerEnterTrade(player);
            if (args.length == 1) {
                List<String> list = new ArrayList<>();
                String arg_1 = args[0];
                if (trade == null) {
                    arg_1_noJoin.forEach(info -> {
                        if (info.startsWith(arg_1)) list.add(info);
                    });
                    return list;
                }
                if (trade.getOwner().equals(player.getName())) {
                    arg_1_owner.forEach(info -> {
                        if (info.startsWith(arg_1)) list.add(info);
                    });
                    return list;
                }
                arg_1_joined.forEach(info -> {
                    if (info.startsWith(arg_1)) list.add(info);
                });
                return list;

            }
            List<String> list = new ArrayList<>();
            switch (args[0]) {
                case "create":
                    return Collections.singletonList("<工会名> 创建工会");
                case "info":
                    for (Trade t : TradeHelper.getList()) list.add(t.getName());
                    return list;
                case "player":
                    for (Trade t : TradeHelper.getList()) {
                        list.addAll(t.getMember().keySet());
                    }
                    return list;
                case "kick":
                    if (trade != null && trade.getOwner().equals(player.getName())) {
                        list.addAll(trade.getMember().keySet());
                        return list;
                    }
                    break;
                case "join":
                    if (trade == null) {
                        for (Trade t : TradeHelper.getList()) {
                            list.add(t.getName());
                        }
                        return list;
                    }
                    break;
                case "leave":
                    if (trade != null) {
                        list.add(trade.getName());
                        return list;
                    }
                    break;
                case "accept":
                case "deny":
                    if (trade != null && trade.getOwner().equals(player.getName())) {
                        list.addAll(trade.getRequest());
                        return list;
                    }
                    break;
            }


            return empty;
        }
    }
}
