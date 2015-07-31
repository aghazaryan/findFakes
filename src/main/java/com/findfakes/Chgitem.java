package com.findfakes;

import com.mongodb.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aghasighazaryan on 7/31/15.
 */
public class Chgitem {


    private BasicDBList votes = new BasicDBList();
    private BasicDBObject contests = new BasicDBObject();

    private Map<BasicDBObject,Integer> fakePercent = new HashMap<BasicDBObject, Integer>();

    private DB db;
    private DBCollection col;
    private MongoClient mongoClient = null;


    public void connect() {

        if (mongoClient == null) {
            mongoClient = new MongoClient("localhost");
            db = mongoClient.getDB("task");//picsart
            col = db.getCollection("user");//contests
        }

    }

    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    /**
     * user - fake percent, for example UserXXX - 67%
     */
    public void result(){
        calculating();
        for(BasicDBObject user : fakePercent.keySet()){

            System.out.println(user.get("name") + " - "+ fakePercent.get(user)+"%");
        }

    }


    /**
     * user - fake percent and category , for example UserXXX - hly chgitem
     */
    public void advancedResult(){
        calculating();

    }

    private void calculating(){

       DBCursor cursor = col.find(new BasicDBObject("$elemMatch",),new BasicDBObject("votes",1));

       //cursor.toArray();

        while (cursor.hasNext()){
            votes.add(cursor.next());
        }

    }

}
