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

    private DB db1;
    private DB db2;

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

    public void forContest(ObjectId tag, Long photoId) {

        DBCollection contests = db1.getCollection("contests");

        BasicDBObject query = new BasicDBObject("tag", tag)
                .append("photo_id", photoId);
        BasicDBObject projection = new BasicDBObject("votes", 1)
                .append("created", 1);
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

    private <T> List<T> getList(BasicDBList query, String projection) {

        List<T> ret = new ArrayList<T>();

        DBCollection cl1 = db1.getCollection("users");
        DBCollection cl2 = db2.getCollection("users");

        BasicDBObject proj = new BasicDBObject(projection, 1).append("_id", 0);

        DBObject value;
        int i = 0;
        for (ObjectId obj : query.toArray(new ObjectId[query.size()])) {
            i++;
            if ((value = cl1.findOne(obj, proj)) != null) {
                System.out.println(i);
                ret.add((T) value.get(projection));
            } else if ((value = cl2.findOne(obj, proj)) != null) {
                System.out.println(i);
                ret.add((T) value.get(projection));
            }
        }
        System.out.println(i);
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

                System.out.println(d);
                if (d.getTime() - begin.getTime() < 12 * 3600 * 1000) {
                    i++;
                } else {
                    begin = d;
                    if (i > 1) {
                        group--;
                    }
                    i = 1;
                }
            }
            return group * 100.0 / created.size();
        }
        return 100.0;
    }

    private double metadata(BasicDBList votes) {

        List<DBObject> metadata = getList(votes, "metadata");
        Map<String, Integer> android = new HashMap<String, Integer>();
        Map<String, Integer> apple = new HashMap<String, Integer>();

        if(metadata.size()==0){
            return 100.0;
        }

        for (DBObject obj : metadata) {

            Object ip = obj.get("ip");
            String platform = (String) obj.get("platform");

            if (ip != null && platform != null) {

                if (platform.equals("android")) {
                    if (android.get(ip) == null) {
                        android.put((String) ip, 1);
                    } else {
                        int newValue = android.get(ip) + 1;
                        android.put((String) ip, newValue);
                    }

                }
                if (platform.equals("apple")) {
                    if (apple.get(ip) == null) {
                        apple.put((String) ip, 1);
                    } else {
                        int newValue = apple.get(ip) + 1;
                        apple.put((String) ip, newValue);
                    }
                }
            }
        }
        return (apple.size() + android.size()) * 100.0 / metadata.size();
    }

    public void forContest(String tagName, Long photoId){
        DBObject tagId = db1.getCollection("tags")
                .findOne(new BasicDBObject("name",tagName)
                        ,new BasicDBObject("_id",1));


        ObjectId id = (ObjectId)tagId.get("_id");

        forContest(id,photoId);
    }


}