package cc.baka9.catseedtrade.exception;

public class CatSeedTradeException extends Exception {

    public CatSeedTradeException(String message){
        super("§e" + message);
    }
}
