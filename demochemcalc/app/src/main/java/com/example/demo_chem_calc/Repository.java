package com.example.demo_chem_calc;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Repository extends SQLiteOpenHelper {

    private static final String DB_NAME = "chemistry.db";
//    private static final int DB_VERSION = 1;
    private static final int DB_VERSION = 2;

    private final Context context;
    private final String dbPath;

    public Repository(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;

        this.dbPath = context.getDatabasePath(DB_NAME).getPath();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // не используется — база уже создана заранее
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    // Проверяем, есть ли база, если нет — копируем
//    public void checkAndCopyDatabase() {
//        File dbFile = new File(dbPath);
//        if (!dbFile.exists()) {
//            copyDatabaseFromAssets();
//        }
//    }
    public void checkAndCopyDatabase() {
        copyDatabaseFromAssets(); // ВСЕГДА копируем
    }


    private void copyDatabaseFromAssets() {
        try {
            InputStream input = context.getAssets().open(DB_NAME);
            OutputStream output = new FileOutputStream(dbPath);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            input.close();

        } catch (IOException e) {
            throw new RuntimeException("Ошибка копирования базы: " + e);
        }
    }

    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

//    public Double getMolarMassByName(String name) {
//        SQLiteDatabase db = openDatabase();
//
//        Cursor cursor = db.rawQuery(
//                "SELECT molar_mass FROM substances WHERE LOWER(name) = LOWER(?) LIMIT 1",
//                new String[]{name}
//        );
//
//        Double result = null;
//
//        if (cursor != null && cursor.moveToFirst()) {
//            result = cursor.getDouble(0);
//        }
//
//        if (cursor != null) cursor.close();
//        return result;
//    }

    public String getFormulaByName(String name) {
        SQLiteDatabase db = openDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT formula FROM substances WHERE LOWER(name) = LOWER(?) LIMIT 1",
                new String[]{name}
        );

        String result = null;

        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(0);
        }

        if (cursor != null) cursor.close();
        return result;
    }

}

