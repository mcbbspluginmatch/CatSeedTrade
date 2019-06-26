package cc.baka9.catseedtrade;

import cc.baka9.catseedtrade.exception.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                        Player targetPlayer = Bukkit.getPlayer(args[1]);
                        if (targetPlayer == null || !targetPlayer.isOnline()) {
                            player.sendMessage("§e你查询的指定玩家不存在");
                        } else {
                            Trade trade = TradeHelper.getPlayerEnterTrade(targetPlayer);
                            if (trade == null) {
                                player.sendMessage("§e你查询的指定玩家没有加入任何公会");
                            } else {
                                player.spigot().sendMessage(TradeHelper.getTradeTextComponent(trade));
                            }

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
                            } catch (PlayerNotHaveTradeException e) {
                                player.sendMessage("§e你没有加入任何工会");
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
                    } catch (PlayerNotHaveTradeException e) {
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
                            } catch (TradeNotHaveThisPlayerException | PlayerNotHaveTradeException e) {
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
        return false;
    }


}
