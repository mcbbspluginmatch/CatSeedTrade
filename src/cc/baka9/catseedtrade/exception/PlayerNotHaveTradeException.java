package cc.baka9.catseedtrade.exception;

public class PlayerNotHaveTradeException extends CatSeedTradeException {
    public PlayerNotHaveTradeException(String playerName){
        super(playerName + " 没有加入任何工会");
    }
}
