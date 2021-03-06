
package me.openphoto.android.app;

import me.openphoto.android.app.net.IOpenPhotoApi;
import me.openphoto.android.app.net.OpenPhotoApi;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
    public static boolean isAutoUploadActive(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(
                        context.getString(R.string.setting_autoupload_on_key),
                        context.getResources().getBoolean(R.bool.setting_autoupload_on_default));
    }

    public static String getAutoUploadTag(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                        context.getString(R.string.setting_autoupload_tag_key),
                        context.getResources().getString(R.string.setting_autoupload_tag_default));
    }

    public static String getServer(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                        context.getString(R.string.setting_account_server_key),
                        context.getString(R.string.setting_account_server_default));
    }

    public static boolean setServer(Context context, String server) {
        if (!server.startsWith("http")) {
            server = "http://" + server;
        }

        if (server.endsWith("/")) {
            server = server.substring(0,
                    server.length() - 1);
        }

        return PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(R.string.setting_account_server_key), server)
                .commit();
    }

    public static boolean isLoggedIn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.setting_account_loggedin_key), false);
    }

    public static void logout(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.setting_account_loggedin_key), false)
                .commit();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(context.getString(R.string.setting_account_server_key),
                        context.getString(R.string.setting_account_server_default)).commit();

        context.getSharedPreferences("oauth", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    public static void setLoginInformation(Context context, OAuthConsumer consumer) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.setting_account_loggedin_key), true)
                .commit();
        context.getSharedPreferences("oauth", Context.MODE_PRIVATE)
                .edit()
                .putString(context.getString(R.string.setting_oauth_consumer_key),
                        consumer.getConsumerKey())
                .putString(context.getString(R.string.setting_oauth_consumer_secret),
                        consumer.getConsumerSecret())
                .putString(context.getString(R.string.setting_oauth_token),
                        consumer.getToken())
                .putString(context.getString(R.string.setting_oauth_token_secret),
                        consumer.getTokenSecret())
                .commit();
    }

    public static OAuthProvider getOAuthProvider(Context context) {
        String serverUrl = getServer(context);
        OAuthProvider provider = new DefaultOAuthProvider(
                serverUrl + "/v1/oauth/token/request",
                serverUrl + "/v1/oauth/token/access",
                serverUrl + "/v1/oauth/authorize");
        provider.setOAuth10a(true);
        return provider;
    }

    public static OAuthConsumer getOAuthConsumer(Context context) {
        if (!isLoggedIn(context)) {
            return null;
        }

        SharedPreferences prefs = context.getSharedPreferences("oauth", Context.MODE_PRIVATE);
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
                prefs.getString(context.getString(R.string.setting_oauth_consumer_key), null),
                prefs.getString(context.getString(R.string.setting_oauth_consumer_secret),
                        null));
        consumer.setTokenWithSecret(
                prefs.getString(context.getString(R.string.setting_oauth_token), null),
                prefs.getString(context.getString(R.string.setting_oauth_token_secret), null));
        return consumer;
    }

    public static IOpenPhotoApi getApi(Context context) {
        return OpenPhotoApi.createInstance(context);
    }
}
