package com.findfakes;

import org.bson.types.ObjectId;

/**
 * Created by aghasighazaryan on 7/31/15.
 */
public class Main {
    public static void main(String[] args) {
//
//        Chgitem chgitem = new Chgitem();
//        chgitem.connect();
//
//
//        chgitem.doing();
//        chgitem.result();
//
//        chgitem.disconnect();

        long t = System.currentTimeMillis();
        Other other = new Other("quorum1", "quorum2", "PICSART");
        other.forContest(new ObjectId("54aff9be550c736c3b000041"), Long.parseLong("158526432000201"));
        other.forContest("wapwhite", Long.parseLong("158526432000201"));
        other.disconnect();
        System.out.println(System.currentTimeMillis() - t);
    }
}
