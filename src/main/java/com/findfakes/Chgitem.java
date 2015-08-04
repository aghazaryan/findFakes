package com.findfakes;

import com.mongodb.*;

import java.util.*;

/**
 * Created by aghasighazaryan on 7/31/15.
 */
public class Chgitem {


    private Map<BasicDBObject, List> fakePercent = new HashMap<BasicDBObject, List>();

    private DB db;
    private MongoClient mongoClient = null;

    public void connect() {

        if (mongoClient == null) {
            mongoClient = new MongoClient("173.192.94.174:2717");
            db = mongoClient.getDB("PICSART");
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
    public void result() {
        System.out.println("result is :");

        for (BasicDBObject user : fakePercent.keySet()) {

            //entadrum em, vor bolor usernery metadata unen
            List result = fakePercent.get(user);

            double percent = (Integer)result.get(0);
            percent=(percent-1)*3;
            percent = percent<15 ? percent:15;//metadata max 15

            percent+=(Integer)result.get(1);//created max 10
            percent+=(Integer)result.get(2);//photo max 3
            percent+=(Integer)result.get(3);//follower max 3

            percent*=(100/31);


            System.out.println(user.get("name") + " - " + (int)percent + "%");
        }

    }


//    /**
//     * user - fake percent and category , for example UserXXX - hly chgitem
//     */
//    public void advancedResult() {
//        for (BasicDBObject user : fakePercent.keySet()) {
//
//            System.out.println(user.get("name") + " - " + fakePercent.get(user).get(3) + "%");
//        }
//
//    }

    private List<DBObject> tagsContests() {

        System.out.println("tagsContests");
        DBCollection tags = db.getCollection("tags");
//        DBCursor cursor = tags.find(new BasicDBObject("ended"
//                , new BasicDBObject("$gte", System.currentTimeMillis()))
//                , new BasicDBObject("contests", 1).append("name", 1));
        DBCursor cursor = tags.find(new BasicDBObject(), new BasicDBObject("contests", 1).append("name", 1));
        return cursor.toArray();

    }

    private List<DBObject> votes(BasicDBObject contest) {

        DBCollection users = db.getCollection("users");
        BasicDBList list = (BasicDBList) contest.get("votes");
        DBCursor cursor = users.find(new BasicDBObject("$or", list), new BasicDBObject("name", 1)
                .append("metadata", 1)
                .append("followers_count", 1)
                .append("photos_count", 1)
                .append("created", 1));
        return cursor.toArray();
    }


    private void calculating(BasicDBObject contestId) {

        DBCollection contestsColl = db.getCollection("contests");
        BasicDBObject contests = (BasicDBObject) contestsColl.findOne(contestId);
        if (contests == null) {

            System.err.println("tenc contests id chkar");
            return;
        }
        System.out.println("for " + contests.get("photo") + " voted users fake percent:");

        metadata(contests);

        for (DBObject user : votes(contests)) {

            List status = new ArrayList();

            status.add(created(contests, user));
            status.add(photoCount(user));
            status.add(followersCount(user));

            BasicDBObject basicDBObjectUser = (BasicDBObject) user;

            List result = fakePercent.get(user);

            if (result != null) {
                result.addAll(status);
                fakePercent.put(basicDBObjectUser, result);
            } else {

                fakePercent.put(basicDBObjectUser, status);
            }
        }

    }

    public void doing() {
        for (DBObject tag : tagsContests()) {
            System.out.println("for tag " + tag.get("name"));
            BasicDBList contestIDs = (BasicDBList) (tag.get("contests"));

            if (contestIDs != null) {
                System.err.println(contestIDs.toString());

                //BasicDBObject[] arr = contestIDs.toArray(new BasicDBObject[0]);
                BasicDBObject[] arr = toBasicDBObjArr(contestIDs.toString());

                for (BasicDBObject contestID : arr) {
                    calculating(contestID);
                }
            }
        }

    }

    private void metadata(BasicDBObject contests) {


        Map<String, Integer> android = new HashMap<String, Integer>();
        Map<String, Integer> apple = new HashMap<String, Integer>();


        for (DBObject user : votes(contests)) {
            DBObject metadata = (DBObject) user.get("metadata");
            if (metadata == null) {
                continue;
            }
            Object ip = metadata.get("ip");
            String platform = (String) metadata.get("platform");

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

        DBCollection users = db.getCollection("users");
        BasicDBObject projection = new BasicDBObject("name", 1)
                .append("metadata", 1)
                .append("followers_count", 1)
                .append("photos_count", 1)
                .append("created", 1);


        for (String key : android.keySet()) {

            List status = new ArrayList();
            status.add(android.get(key));
            BasicDBObject user = (BasicDBObject) users.findOne(new BasicDBObject("metadata",
                    new BasicDBObject("platform", "android").append("ip", key))
                    , projection);
            fakePercent.put(user, status);

        }
        for (String key : apple.keySet()) {

            List status = new ArrayList();
            status.add(apple.get(key));
            BasicDBObject user = (BasicDBObject) users.findOne(new BasicDBObject("metadata",
                    new BasicDBObject("platform", "apple").append("ip", key))
                    , projection);
            fakePercent.put(user, status);

        }

    }

    private int created(BasicDBObject contests, DBObject user) {

        return (Integer) user.get("created") > (Integer) contests.get("created") ? 10 : 0;

    }

    private int photoCount(DBObject user) {
        int ret;
        return (ret = (Integer) user.get("photos_count")) < 4 ? 3 - ret : 0;
    }

    private int followersCount(DBObject user) {
        int ret;
        return (ret = (Integer) user.get("followers_count")) < 4 ? 3 - ret : 0;
    }


    private BasicDBObject[] toBasicDBObjArr(String str) {
        int value = 3;
        String[] arr = str.split("\"");
        int n = (arr.length - 1) / 4;

        BasicDBObject[] ret = new BasicDBObject[n];

        for (int i = 0; i < n; i++) {
            ret[i] = new BasicDBObject("_id", arr[value]);
            value += 2;
        }
        return ret;
    }

}
