package cc.baka9.catseedtrade;


import java.util.Arrays;
import java.util.List;

public class Test {
    static String s = "sf";
    static {
        System.out.println("staticBlock");
        System.out.println(s);
    }
    public static void main(String[] args){
        test2();
    }

    static void test2(){
        System.out.println(getList().subList(0, 1));
        System.out.println(getList().subList(5, getList().size()));
    }

    static List<Object> getList(){
        return Arrays.asList("1", "2", "3", "4", "5", "6");
    }

    static void test(){
        int lv = 0;
        System.out.println(100 * Math.pow(2, lv - 1));
    }
}
