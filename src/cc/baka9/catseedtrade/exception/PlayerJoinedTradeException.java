package cc.baka9.catseedtrade.exception;

import cc.baka9.catseedtrade.Trade;

public class PlayerJoinedTradeException extends CatSeedTradeException {
    public PlayerJoinedTradeException(String playerName , Trade trade){
        super(playerName + " 已经加入了 " + trade.getName() + " 工会了");
    }
}
