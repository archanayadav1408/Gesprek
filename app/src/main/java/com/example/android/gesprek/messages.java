package com.example.android.gesprek;

public class messages {


    String message;

    Boolean seen;
    String type;
    String from;

    public  messages()
    {


    }
    public messages(String message,  Boolean seen, String type, String from) {
        this.message = message;

        this.seen = seen;
        this.type = type;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }



    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}