package com.myapplication.objects;

public class MessageObject {

    String msg;
    int from_id;
    int incoming;


    public MessageObject(String msg, int  from_id, int incoming) {
        this.msg = msg;
        this.from_id = from_id;
        this.incoming = incoming;
    }

    public MessageObject(String msg, int incoming) {
        this.msg = msg;
        this.incoming = incoming;
    }

    public int getFrom_id() {
        return from_id;
    }

    public int getIncoming() {
        return incoming;
    }

    public String getMsg() {
        return msg;
    }
}
