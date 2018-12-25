package com.example.android.gesprek;

public class Conv {

    public boolean seen;
    public long timestamp;
    String message, from,type;

    public Conv(){

    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Conv(Boolean seen, Long timestamp, String message, String from, String type) {
        this.seen = seen;
        this.timestamp = timestamp;
        this.message = message;
        this.from = from;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
