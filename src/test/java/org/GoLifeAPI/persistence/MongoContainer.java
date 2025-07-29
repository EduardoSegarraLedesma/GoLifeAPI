package org.GoLifeAPI.persistence;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class MongoContainer {

    @Container
    protected static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0.11");

    private static String uid;
    private static String boolMid;
    private static String numMid;

    static {
        mongo.start();
    }

    public static String getMongoURI() {
        return mongo.getReplicaSetUrl();
    }

    public static String getUid() {
        return uid;
    }

    public static String getBoolMid() {
        return boolMid;
    }

    public static String getNumMid() {
        return numMid;
    }

    public static void setUid(String uid) {
        MongoContainer.uid = uid;
    }

    public static void setBoolMid(String boolMid) {
        MongoContainer.boolMid = boolMid;
    }

    public static void setNumMid(String numMid) {
        MongoContainer.numMid = numMid;
    }
}