package com.lorentzos.swipecards;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper {

    private static final String DATABASE_NAME = "YesOrNo";
    private static final String DATABASE_TABLE = "cards";

    public static final String KEY_ROWID = "_id";
    public static final String KEY_URL = "url";
    public static final String KEY_TAG = "tag";
    public static final String KEY_QCFLAG = "qcfilag";
    public static final String KEY_KEYSPACE = "keyspace";
    public static final String KEY_TRUTH = "truth";
    public static final String KEY_ANSWERED_OR_NOT = "isAnswered";
    public static final String KEY_IMAGE = "image_path";


    private static final int DATABASE_VERSION = 3;

    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;

    private static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE_NAME,null,DATABASE_VERSION);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub _id INTEGER PRIMARY KEY
            db.execSQL("CREATE TABLE " + DATABASE_TABLE + " ( " + KEY_ROWID + " INTEGER " +
                            " PRIMARY KEY , " + KEY_URL + " TEXT NOT NULL UNIQUE , "
                            + KEY_TAG + " TEXT NOT NULL , " + KEY_QCFLAG +
                            " INTEGER NOT NULL, " + KEY_KEYSPACE + " TEXT NOT NULL, " + KEY_TRUTH +
                            " INTEGER , " +  KEY_ANSWERED_OR_NOT +
                            " INTEGER NOT NULL , " + KEY_IMAGE + " TEXT NOT NULL " +
                            " );"
            );



        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL(" DROP TABLE IF EXISTS " + DATABASE_TABLE  );
            onCreate(db);
            // Version 2 - added a column isFav - dropped table readlater - Notificationtable changed
           /* if (oldVersion ==  1) {
                db.execSQL("ALTER TABLE "+ DATABASE_BLOGGER_TABLE + " ADD COLUMN " + "isFav" + " INTEGER DEFAULT 0");
                //db.execSQL("ALTER TABLE "+ DATABASE_TABLE + " ADD COLUMN " + KEY_CATEGORY + " INTEGER DEFAULT 0");
                db.execSQL(" DROP TABLE IF EXISTS " + DATABASE_READ_LATER_TABLE  );
                db.execSQL(" DROP TABLE IF EXISTS " + DATABASE_NOTIFICATIONS_TABLE  );
                db.execSQL("CREATE TABLE " + DATABASE_NOTIFICATIONS_TABLE + " ( " + KEY_ROWID + " INTEGER NOT NULL UNIQUE" +
                                "  , " + KEY_TITLE + " TEXT NOT NULL  , "
                                + KEY_BLOGGER_ID + " INTEGER NOT NULL , " + KEY_CATEGORY +
                                " TEXT NOT NULL, " + KEY_DATE + " TEXT NOT NULL, " + KEY_READ_OR_NOT +
                                " INTEGER NOT NULL ,"+  "blogger_name" +
                                " TEXT NOT NULL ," + KEY_BLOG_NAME +
                                " TEXT NOT NULL "+" );"
                );
            }
         */
        }

    }

    public DatabaseHelper(Context c){
        ourContext = c;
    }

    public DatabaseHelper open(){
        ourHelper = new DbHelper (ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        ourHelper.close();
    }

    public long addCardObj(ObjCard obj){
        ContentValues cv = new ContentValues();
        cv.put(KEY_URL,  obj.url);
        cv.put(KEY_TAG,  obj.tag);
        cv.put(KEY_QCFLAG,  obj.qcflag);
        cv.put(KEY_KEYSPACE,  obj.keyspace);
        cv.put(KEY_ANSWERED_OR_NOT, 0);
        cv.put(KEY_IMAGE, obj.getImage());
        long id = ourDatabase.insert(DATABASE_TABLE, null, cv);
        Log.d("id of the added", Long.toString(id));
        return id;
    }

    public Cursor getAllCards () {
        String[] columns = new String[] {KEY_ROWID, KEY_URL, KEY_TAG, KEY_QCFLAG, KEY_KEYSPACE, KEY_TRUTH, KEY_ANSWERED_OR_NOT, KEY_IMAGE};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ANSWERED_OR_NOT + " = " + 0, null, null, null,  KEY_ROWID + " ASC");
        return c;
    }

    public Cursor getCardById (int id) {
        String[] columns = new String[] {KEY_ROWID, KEY_URL, KEY_TAG, KEY_QCFLAG, KEY_KEYSPACE, KEY_TRUTH, KEY_ANSWERED_OR_NOT, KEY_IMAGE};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ROWID + " = " + id, null, null, null,  KEY_ROWID + " ASC");
        return c;
    }

    public Void deleteCardFromQue(int id){
        int count = ourDatabase.delete(DATABASE_TABLE, KEY_ROWID + " = " + id, null);
        Log.d("Number Deleted", Integer.toString(count));
        return null;
    }

    public int getCountOfCards(){
        String[] columns = new String[] {KEY_ROWID, KEY_URL, KEY_TAG, KEY_QCFLAG, KEY_KEYSPACE, KEY_TRUTH, KEY_ANSWERED_OR_NOT, KEY_IMAGE};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_ANSWERED_OR_NOT + " = " + 0, null, null, null,  KEY_ROWID + " ASC");
        int count = c.getCount();
        return count;
    }


}