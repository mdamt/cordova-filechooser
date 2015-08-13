package com.megster.cordova;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.ContentUris;
import android.net.Uri;
import android.util.Log;
import android.database.Cursor;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

public class FileChooser extends CordovaPlugin {

    private static boolean resolvePath = false;
    private static String mimeType = "*/*";
    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final String ACTION_RESOLVE = "resolve";
    private static final int PICK_FILE_REQUEST = 1;
    CallbackContext callback;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_OPEN)) {
            JSONObject obj = null;
            try {
                obj = args.getJSONObject(0);
                resolvePath = obj.getBoolean("resolve-path");
            } catch (Exception e) {
                // silent
            }

            try {
                if (obj != null) {
                    mimeType = obj.getString("mime-type");
                }
            } catch (Exception e) {
                // silent
            }
            chooseFile(callbackContext);
            return true;
        } else if (action.equals(ACTION_RESOLVE)) {
            final String uri = args.getString(0);
            resolve(uri, callbackContext);
            return true;
        }

        return false;
    }

    public void chooseFile(CallbackContext callbackContext) {

        // type and title should be configurable

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooser = Intent.createChooser(intent, "Select File");
        cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    public void resolve(final String uri, CallbackContext callbackContext) {
        final String resolved = resolveUri(Uri.parse(uri));
        callbackContext.success(resolved);
    }

    String resolveUri(final Uri uri) {
        Context context = cordova.getActivity();
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = null;
            Uri lookedUpUri = uri;
            final String[] projection = {
                "_data"
            };
            if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                final long id = ContentUris.parseId(uri);
                lookedUpUri = ContentUris.withAppendedId(Uri.parse("content://downloads/all_downloads"), id);
            }

            try {
                cursor = context.getContentResolver().query(lookedUpUri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int index = cursor.getColumnIndexOrThrow("_data");
                    return cursor.getString(index); 
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        } else if ("file".equals(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_FILE_REQUEST && callback != null) {

            if (resultCode == Activity.RESULT_OK) {

                Uri uri = data.getData();

                if (uri != null) {

                    Log.w(TAG, uri.toString());
                    if (resolvePath) {
                        callback.success(resolveUri(uri));
                    } else {
                        callback.success(uri.toString());
                    }

                } else {

                    callback.error("File uri was null");

                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

                // TODO NO_RESULT or error callback?
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                callback.sendPluginResult(pluginResult);

            } else {

                callback.error(resultCode);
            }
        }
    }
}
