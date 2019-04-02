package ceid.katefidis.calchas;
/*Event types:
 * 1 = Patise mia apo tis protaseis mas
 * 2 = Patise to eikonidio gia to call log
 * 3 = Patise to eikonidio gia tis epafes
 */

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StoreStatsSQLlite extends SQLiteOpenHelper
{
    static final String DATABASE_NAME = "calchasnumbers.db";
    private static final int DATABASE_VERSION = 19;

    // Database creation sql statement
    private static final String DATABASE_CREATE1 = 	"create table numbers " +
            "(number TEXT primary key, " +
            "mccmnc integer, " +
            "checked integer);";

    private static final String DATABASE_CREATE2 =	"create table networks " +
            "(mccmnc integer primary key, " +
            "name text);";


    public StoreStatsSQLlite (Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL(DATABASE_CREATE1);
        database.execSQL(DATABASE_CREATE2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS numbers;");
        db.execSQL("DROP TABLE IF EXISTS networks;");
        onCreate(db);
    }

    void insertManyNumbers(ArrayList<String> numbers)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        for (int x=0;x<numbers.size();x++)
        {
            ContentValues values = new ContentValues();
            values.put("number", numbers.get(x));
            values.put("mccmnc", -1);
            values.put("checked", -1);
            db.insertWithOnConflict("numbers", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.endTransaction();
        db.close();
    }

    void insertNumber(String num, int mccmnc, String network, int checked)
    {
        Log.i("SQL","about to insert "+num+", "+mccmnc);
        //db.execSQL("INSERT OR IGNORE INTO numbers (number) VALUES ("+num+");");
        SQLiteDatabase db = this.getWritableDatabase();
        //db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put("number", num);
        values.put("mccmnc", mccmnc);
        values.put("checked", 0);
        Log.i("SQL", "inserted into numbers at row "+db.insertWithOnConflict("numbers", null, values, SQLiteDatabase.CONFLICT_IGNORE));
        values = new ContentValues();
        values.put("mccmnc", mccmnc);
        values.put("name", network);
        Log.i("SQL", "inserted into networks at row "+db.insertWithOnConflict("networks", null, values, SQLiteDatabase.CONFLICT_IGNORE));
        //db.endTransaction();
        db.close();
    }

    //finds out which numbers don't have an entry in our local db
    ArrayList<String> getNumberstoCheck(ArrayList<String> numList)
    {
        String nums="";
        for(String number : numList)
        {
            nums+=number+",";
        }
        nums=nums.substring(0, nums.length()-2); //remove trailing comma

        SQLiteDatabase db = this.getWritableDatabase();
        String cols[] = new String[1];
        cols[0]="number";
        Cursor c = db.query("numbers", cols, "IN ("+nums+")", null, null, null, null);

        do
        {
            int numpos=numList.indexOf(c.getInt(0));
            if(numpos!=-1)//number exists in the checklist
            {
                numList.remove(numpos);
            }
        }
        while(c.moveToNext());

        //the list is clean, now return it.
        db.close();
        return numList;
    }

    public String getNetworkName(String number)
    {
        Log.i("SQL","checking "+number);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("select networks.name, networks.mccmnc from numbers join networks on numbers.mccmnc = networks.mccmnc"+
                " where number LIKE ?", new String[]{"%"+number+"%"});

        if(c.getCount()==0)
        {
            db.close();
            Log.i("SQL","no record found");
            return null;
        }
        else
        {
            c.moveToFirst();
            Log.i("SQL","network record found: "+c.getString(1));
            db.close();
            return c.getString(0);

        }

    }


    public void deletestats()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS numbers;");
        db.execSQL("DROP TABLE IF EXISTS networks;");
        db.execSQL(DATABASE_CREATE1);
        db.execSQL(DATABASE_CREATE2);
        db.close();
    }

}