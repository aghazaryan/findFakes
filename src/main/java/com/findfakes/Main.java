package com.findfakes;


/**
 * Created by aghasighazaryan on 7/31/15.
 */
public class Main {
    public static void main(String[] args) {

        Chgitem chgitem = new Chgitem();
        chgitem.connect();


        chgitem.doing();
        chgitem.result();

        chgitem.disconnect();

    }
}
