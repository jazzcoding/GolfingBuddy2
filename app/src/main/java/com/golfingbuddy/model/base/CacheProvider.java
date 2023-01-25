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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;

import com.golfingbuddy.core.sqlite.SQLiteTableProvider;


public class CacheProvider extends SQLiteTableProvider {

    public static final String TABLE_NAME = "cache";

    public static final Uri URI = Uri.parse("content://com.skadatexapp.sqlite/" + TABLE_NAME);

    public CacheProvider() {
        super(TABLE_NAME);
    }

    public static long getId(Cursor c) {
        return c.getLong(c.getColumnIndex(Columns._ID));
    }

    public static String getUri(Cursor c) {
        return c.getString(c.getColumnIndex(Columns.URI));
    }

    public static String getData(Cursor c) {
        return c.getString(c.getColumnIndex(Columns.DATA));
    }

    public static String getExpireTs(Cursor c) {
        return c.getString(c.getColumnIndex(Columns.EXPIRE_TS));
    }


    @Override
    public Uri getBaseUri() {
        return URI;
    }

    @Override
    public void onContentChanged(Context context, int operation, Bundle extras) {
        if (operation == INSERT) {
//            extras.keySet();
//            final Bundle syncExtras = new Bundle();
//            syncExtras.putLong(SyncAdapter.KEY_FEED_ID, extras.getLong(KEY_LAST_ID, -1));
//            ContentResolver.requestSync(AppDelegate.sAccount, AppDelegate.AUTHORITY, syncExtras);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_NAME +
                "(" + Columns._ID + " integer primary key on conflict replace, "
                + Columns.URI + " text unique on conflict replace, "
                + Columns.DATA + " text, "
                + Columns.EXPIRE_TS + " integer)");
    }

    public interface Columns extends BaseColumns {
        String URI = "uri";
        String DATA = "data";
        String EXPIRE_TS = "expireTs";
    }

}
