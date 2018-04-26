package me.aitbaali.aiacgo;

import android.support.annotation.NonNull;

public class Card implements Comparable<Card>{
    private String mdate;
    private String mplace;
    private String mprice;
    private String mtimeHeGo;
    private User user;
    private long timeUnix;

    public Card(){

    }


    public Card(String mdate, String mplace, String mprice, String mtimeHeGo, User user, long timeUnix) {
        this.mdate = mdate;
        this.mplace = mplace;
        this.mprice = mprice;
        this.mtimeHeGo = mtimeHeGo;
        this.user = user;
        this.timeUnix = timeUnix;
    }
    //date
    public String getMdate() {
        return mdate;
    }

    public void setMdate(String mdate) {
        this.mdate = mdate;
    }

    //notecolor

    public String getMplace() {
        return mplace;
    }

    public void setMplace(String mplace) {
        this.mplace = mplace;
    }


    //price
    public String getMprice() {
        return mprice;
    }

    public void setMprice(String mprice) {
        this.mprice = mprice;
    }

    //timehego
    public String getMtimeHeGo() {
        return mtimeHeGo;
    }

    public void setMtimeHeGo(String mtimeHeGo) {
        this.mtimeHeGo = mtimeHeGo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getTimeUnix() {
        return timeUnix;
    }

    public void setTimeUnix(long timeUnix) {
        this.timeUnix = timeUnix;
    }



    @Override
    public int compareTo(@NonNull Card o) {
        if(this.getTimeUnix() > o.getTimeUnix()){
            return 1;
        }else{
            return -1;
        }
    }
}
