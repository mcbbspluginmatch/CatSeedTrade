package cc.baka9.catseedtrade.exception;

public class PlayerNotJoinTradeException extends CatSeedTradeException {
    public PlayerNotJoinTradeException(String playerName){
        super(playerName + " 没有加入任何工会");
    }
}
