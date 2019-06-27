package cc.baka9.catseedtrade;

import java.text.DecimalFormat;

public class Utils {
    //保留两位
    private static DecimalFormat doubleDf = new DecimalFormat("0.00");
    public static double twoDecimal(double number){
        return Double.parseDouble(doubleDf.format(number));

    }
}
