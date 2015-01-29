package com.lorentzos.swipecards;

/**
 * Created by aqel on 15/1/15.
 */
public class ObjCard {
    String  url;
    String tag;
    int qcflag;
    int truth;
    String keyspace;
    int id;
    String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTruth() {
        return truth;
    }

    public void setTruth(int truth) {
        this.truth = truth;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public int getQcflag() {
        return qcflag;
    }

    public void setQcflag(int qcflag) {
        this.qcflag = qcflag;
    }

    String answer;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }


}
