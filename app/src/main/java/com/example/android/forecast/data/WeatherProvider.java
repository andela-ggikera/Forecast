package com.example.android.forecast.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by nerd on 25/09/2016.
 */

public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static UriMatcher buildUriMatcher () {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ForecastContract.CONTENT_AUTHORITY;

        // for each uri type, create a corresponding code
        matcher.addURI(authority, ForecastContract.WEATHER_PATH, WEATHER);
        // Use * since the location and date are stored as Strings
        // Technically the date will always be numeric
        matcher.addURI(authority, ForecastContract.WEATHER_PATH + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, ForecastContract.WEATHER_PATH + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);

        matcher.addURI(authority, ForecastContract.LOCATION_PATH, LOCATION);
        // Use # since the id is a   long type
        matcher.addURI(authority, ForecastContract.LOCATION_PATH + "/#", LOCATION_ID);
        return matcher;
    }

    private ForecastDbHelper openHelper;

    @Override
    public boolean onCreate() {
        openHelper = new ForecastDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Define a switch that determines the kind of request to be made given a URI
        Cursor retCursor;
        switch (uriMatcher.match(uri)) {
            case WEATHER_WITH_LOCATION_AND_DATE:
            {
                retCursor = null;
                break;
            }

            // Weather
            case WEATHER_WITH_LOCATION: {
                retCursor = null;
                break;
            }

            case WEATHER: {
                retCursor = openHelper.getReadableDatabase().query(
                        ForecastContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case LOCATION_ID: {
                retCursor = openHelper.getReadableDatabase().query(
                        ForecastContract.LocationEntry.TABLE_NAME,
                        projection,
                        ForecastContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri)+ "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case LOCATION: {
                retCursor = openHelper.getReadableDatabase().query(
                        ForecastContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);

        }
        // set notification uri to be the one passed into this function
        // this causes the cursor to register a content observer to watch for changes
        // that happen to the URI
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    /**
     * This function is used to return the mime type
     * associated with the data at the given URI
     * returns: Mime type in String format
     */
    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case WEATHER_WITH_LOCATION_AND_DATE:
                // "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + WEATHER_PATH
                return ForecastContract.WeatherEntry.CONTENT_TYPE_ITEM;

            case WEATHER_WITH_LOCATION:
                // "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + WEATHER_PATH
                return ForecastContract.WeatherEntry.CONTENT_TYPE_DIR;

            case WEATHER:
                // "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + WEATHER_PATH
                return ForecastContract.WeatherEntry.CONTENT_TYPE_DIR;

            case LOCATION:
                return ForecastContract.LocationEntry.CONTENT_TYPE_DIR;

            case LOCATION_ID:
                // "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + WEATHER_PATH
                return ForecastContract.LocationEntry.CONTENT_TYPE_ITEM;

            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
