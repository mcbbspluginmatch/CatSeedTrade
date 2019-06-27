package cc.baka9.catseedtrade;


import cc.baka9.catseedtrade.jfep.Parser;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Test {
    //保留两位
    private static DecimalFormat doubleDf = new DecimalFormat("0.00");
    static Map<String, Double> member = new HashMap<>();

    public static void main(String[] args){
       String s = "((level - 1) ^ 3 + 60) / 5 * ((level - 1) * 2 + 60)";
        Parser parser = new Parser(s);
        for (int i = 0; i <= 3; i++) {
            parser.setVariable("level", i);
            System.out.println(doubleDf.format(parser.getValue()));

        }
    }

}