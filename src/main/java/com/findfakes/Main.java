package com.findfakes;

import org.bson.types.ObjectId;

import java.util.Arrays;

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


        int n = 10;
        long[] arr = new long[n];
        long t;
        Other other = new Other("173.192.94.174:2717", "173.192.94.174:2717", "PICSART");
        for (int i = 0; i < n; i++) {
            t = System.currentTimeMillis();
            other.forContest(new ObjectId("54aff9be550c736c3b000041"), Long.parseLong("158526432000201"));
            other.forContest("wapwhite", Long.parseLong("158526432000201"));
            arr[i] = System.currentTimeMillis() - t;
        }
        other.disconnect();
        Arrays.sort(arr);
        long sum = 0;
        for (int j = 0; j < n; j++) {
            sum += arr[j];
            System.out.println(arr[j]);
        }

        System.out.println(sum / 10.0);
    }
}
