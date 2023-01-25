/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.golfingbuddy.model.base;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.golfingbuddy.core.sqlite.SQLiteTableProvider;


public class MainMenuProvider extends SQLiteTableProvider {

    public static final String TABLE_NAME = "main_menu";

    public static final Uri URI = Uri.parse("content://com.skadatexapp/" + TABLE_NAME);

    public MainMenuProvider() {
        super(TABLE_NAME);
    }

    public static long getId(Cursor c) {
        return c.getLong(c.getColumnIndex(Columns._ID));
    }

    public static String getLabel(Cursor c) {
        return c.getString(c.getColumnIndex(Columns.LABEL));
    }

    public static String getType(Cursor c) {
        return c.getString(c.getColumnIndex(Columns.TYPE));
    }

    public static String getEventCount(Cursor c) {
        return c.getString(c.getColumnIndex(Columns.EVENT_COUNT));
    }


    @Override
    public Uri getBaseUri() {
        return URI;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Boolean tableExists;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", TABLE_NAME});

        if (!cursor.moveToFirst()){
            tableExists = false;
        }
        else{
            int count = cursor.getInt(0);
            cursor.close();
            tableExists = count > 0;
        }
        if( !tableExists ){


            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    "(" + Columns._ID + " integer primary key on conflict replace autoincrement, "
                    + Columns.KEY + " text unique on conflict replace, "
                    + Columns.LABEL + " text unique on conflict replace, "
                    + Columns.TYPE + " text, "
                    + Columns.FRAGMENT + " text, "
                    + Columns.EVENT_COUNT + " integer default 0)");

            db.execSQL("INSERT INTO " + TABLE_NAME + " SELECT NULL AS " + Columns._ID + ", '' AS " + Columns.KEY +  ", 'Jude Law' AS " + Columns.LABEL + ", 1 AS " + Columns.TYPE + ", '' AS " + Columns.FRAGMENT + ", 0 AS " + Columns.EVENT_COUNT
                    + " UNION SELECT NULL, 'search', 'Search', 2, '', 0"
            );
        }
    }
    public interface Columns extends BaseColumns {
        String LABEL = "label";
        String TYPE = "type";
        String EVENT_COUNT = "eventCount";
        String KEY = "key";
        String FRAGMENT = "fragment";
    }

}
