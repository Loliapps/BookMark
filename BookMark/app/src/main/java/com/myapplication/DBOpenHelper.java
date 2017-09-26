package com.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;



public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "myData.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "all_chatters";
    public static final String COLUMN_ID = "chat_id";
    public static final String COLUMN_CHATTER_NAME = "chatter_name";
    public static final String COLUMN_CHATTER_ID = "chatter_id";
    public static final String COLUMN_CHATTER_IMAGE = "chatter_image";
    public static final String COLUMN_CHATTER_TABLE = "chatter_table";


    private static final String CREATE_TABLE_CHAT = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_CHATTER_ID + " INTEGER," +
            COLUMN_CHATTER_NAME + " VARCHAR(20), " +
            COLUMN_CHATTER_TABLE + " VARCHAR(20), " +
            COLUMN_CHATTER_IMAGE + " VARCHAR(10) DEFAULT 'default.jpg');";

    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CHAT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void createNewTable(String userName){

        SQLiteDatabase db = getWritableDatabase();

        String sql = "CREATE TABLE IF NOT EXISTS " + userName + " (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " message TEXT NOT NULL, cur_time VARCHAR(10) NOT NULL DEFAULT '00:00', " +
                "cur_date VARCHAR(10) NOT NULL DEFAULT '00/00/00', incomming INTEGER DEFAULT 0);";

        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.execute();
        db.close();

    }

    public void insertIntoTable(String userName, String message, String time, String date, int incomming){

        SQLiteDatabase db = getWritableDatabase();

        String sql = "INSERT INTO " + userName + " (message,cur_time,cur_date, incomming) VALUES (?,?,?,?);";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1,message);
        stmt.bindString(2,time);
        stmt.bindString(3,date);
        stmt.bindString(4,String.valueOf(incomming));
        stmt.execute();

        db.close();
    }

    public void insertIntoChatters(int borrowerId, String borrowerName, String borrowerImg, String tableName){

        SQLiteDatabase db = getWritableDatabase();

        String sql = "INSERT INTO " + TABLE_NAME + " (" + COLUMN_CHATTER_ID + "," + COLUMN_CHATTER_NAME + ","+ COLUMN_CHATTER_IMAGE +"," + COLUMN_CHATTER_TABLE +") VALUES (?,?,?,?);";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1,String.valueOf(borrowerId));
        stmt.bindString(2,borrowerName);
        stmt.bindString(3,borrowerImg);
        stmt.bindString(4,tableName);
        stmt.execute();

        db.close();
    }

    public String readFromTable(String tableName){

        String sql = "SELECT message,cur_time,cur_date,incomming FROM " + tableName + ";";
        return sql;
    }

    public String readFromChatters(){

        String sql = "SELECT * FROM " +TABLE_NAME+ ";";
        return sql;
    }

    public void updateChatters(int borrowerId, String borrowerName, String borrowerImg){

        SQLiteDatabase db = getWritableDatabase();

        String sql = "UPDATE " + TABLE_NAME + " SET " + COLUMN_CHATTER_NAME + "=?, " + COLUMN_CHATTER_IMAGE + "=? WHERE " + COLUMN_CHATTER_ID + "="+ borrowerId +";";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1,borrowerName);
        stmt.bindString(2,borrowerImg);
        stmt.executeUpdateDelete();

        db.close();

    }

    public String getChatterData(String borrowerName){
        String sql = "SELECT " + COLUMN_CHATTER_ID + "," + COLUMN_CHATTER_IMAGE + " FROM " + TABLE_NAME + " WHERE " + COLUMN_CHATTER_NAME + " = "+"'" +borrowerName+"';";
        return sql;
    }

}