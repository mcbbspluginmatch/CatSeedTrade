package cc.baka9.catseedtrade;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Trade {

    private String name;
    private String bio;
    private String owner;
    private Map<String, Double> member;
    private int level;
    private Set<String> request;

    public Trade(String name, String bio, String owner, Map<String, Double> member, int level, Set<String> request){
        this.name = name;
        this.bio = bio;
        this.owner = owner;
        this.member = new HashMap<>(member);
        this.level = level;
        this.request = request;
    }

    public Set<String> getRequest(){
        return request;
    }

    public int calcMaxMemberNumber(){
        return 2 + this.level;
    }

    public double calcCurrentExp(){
        double currentExp = 0;
        for (double exp : member.values())
            currentExp = currentExp + exp;
        return currentExp;
    }

    public double calcCurrentMaxExp(){
        return 100 * Math.pow(2, this.level - 1);
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getBio(){
        return bio;
    }

    public void setBio(String bio){
        this.bio = bio;
    }

    public String getOwner(){
        return owner;
    }

    public void setOwner(String owner){
        this.owner = owner;
    }

    public Map<String, Double> getMember(){
        return member;
    }

    public int getLevel(){
        return level;
    }


    public void setLevel(int level){
        this.level = level;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return Objects.equals(name, trade.name);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name);
    }
}
