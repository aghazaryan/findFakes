package com.findfakes;

import com.mongodb.*;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Created by aghasighazaryan on 8/17/15.
 */
public class Other {

    private MongoClient client1 = null;
    private MongoClient client2 = null;

    private DB db1;     //  contests and tags collections are in db1.
    private DB db2;

    /**
     * @param quorum1 quorum for server instance that contain contests and tags collections
     * @param quorum2 quorum for second part of db
     * @param dbName  DB name
     */
    public Other(String quorum1, String quorum2, String dbName) {

        client1 = new MongoClient(quorum1);
        client2 = new MongoClient(quorum2);

        db1 = client1.getDB(dbName);
        db2 = client2.getDB(dbName);
    }

    public void disconnect() {

        if (client1 != null) {
            client1.close();
        }
        if (client2 != null) {
            client2.close();
        }
    }

    /**
     * counting fake percent of contest
     *
     * @param tag     contest tag id
     * @param photoId photo id
     */
    public void forContest(ObjectId tag, Long photoId) {

        DBCollection contests = db1.getCollection("contests");

        BasicDBObject query = new BasicDBObject("tag", tag)
                .append("photo_id", photoId);
        BasicDBObject projection = new BasicDBObject("votes", 1)
                .append("created", 1)
                .append("_id", 0);
        DBObject contest = contests.findOne(query, projection);

        if (contest == null) {
            System.err.println("there is no such contest");
            return;
        }

        BasicDBList votes = (BasicDBList) contest.get("votes");
        double metadata = metadata(votes);
        double created = created(votes);

        System.out.println("metadata: " + metadata + "% , created: " + created + "%");
        System.out.println("total percent: " + (metadata * 2 + created) / 3 + "%");

    }

    /**
     * counting fake percent of contest
     *
     * @param tagName tag name
     * @param photoId photo id
     */
    public void forContest(String tagName, Long photoId) {

        DBObject tagId = db1.getCollection("tags")
                .findOne(new BasicDBObject("name", tagName)
                        , new BasicDBObject("_id", 1));

        ObjectId id = (ObjectId) tagId.get("_id");

        forContest(id, photoId);
    }

    private <T> List<T> getList(BasicDBList query, String projection) {

        List<T> ret = new ArrayList<T>();

        DBCollection cl1 = db1.getCollection("users");
        DBCollection cl2 = db2.getCollection("users");

        BasicDBObject proj = new BasicDBObject(projection, 1).append("_id", 0);

        ObjectId[] inThisArr = query.toArray(new ObjectId[query.size()]);

        DBCursor result1 = cl1.find(new BasicDBObject("_id", new BasicDBObject("$in", inThisArr)), proj);
        DBCursor result2 = cl2.find(new BasicDBObject("_id", new BasicDBObject("$in", inThisArr)), proj);

        while (result1.hasNext()) {
            ret.add((T) result1.next().get(projection));
        }
        while (result2.hasNext()) {
            ret.add((T) result2.next().get(projection));
        }

//        DBObject value;
//        int i = 0;
//        for (ObjectId obj : query.toArray(new ObjectId[query.size()])) {
//            i++;
//            if ((value = cl1.findOne(obj, proj)) != null) {
//                System.out.println(i);
//                ret.add((T) value.get(projection));
//            } else if ((value = cl2.findOne(obj, proj)) != null) {
//                System.out.println(i);
//                ret.add((T) value.get(projection));
//            }
//        }
//        System.out.println(i);
        return ret;
    }

    private double created(BasicDBList votes) {

        List<Date> created = getList(votes, "created");

        if (created.size() != 0) {
            Collections.sort(created);

            int group = created.size();
            Date begin = created.get(0);
            int i = 0;
            for (Date d : created) {

                //    System.out.println(d);
                if (d.getTime() - begin.getTime() < 12 * 3600 * 1000) {  //  different less than 12 hour
                    i++;
                } else {
                    begin = d;
                    if (i > 1) {
                        group--;
                    }
                    i = 1;
                }
            }
            if (i > 1) {    //  checking last group
                group--;
            }

            return group * 100.0 / created.size();
        }
        return 100.0;
    }

    private double metadata(BasicDBList votes) {

        List<DBObject> metadata = getList(votes, "metadata");
        Map<String, Integer> android = new HashMap<String, Integer>();
        Map<String, Integer> apple = new HashMap<String, Integer>();

        if (metadata.size() == 0) {
            return 100.0;
        }

        for (DBObject obj : metadata) {

            Object ip = obj.get("ip");
            String platform = (String) obj.get("platform");

            if (ip != null && platform != null) {

                if (platform.equals("android")) {
                    android.put((String) ip, 1);
                }

                if (platform.equals("apple")) {
                    apple.put((String) ip, 1);
                }
            }
        }
        return (apple.size() + android.size()) * 100.0 / metadata.size();
    }
}
