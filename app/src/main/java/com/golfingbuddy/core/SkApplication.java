package com.golfingbuddy.core;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.golfingbuddy.SkStaticData;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.model.base.classes.SkMenuItem;
import com.golfingbuddy.model.base.classes.SkSite;
import com.golfingbuddy.model.base.classes.SkUser;
import com.golfingbuddy.ui.mailbox.MailboxPingHandler;
import com.golfingbuddy.utils.SKConfig;
import com.golfingbuddy.utils.SKDate;
import com.golfingbuddy.utils.SKLanguage;
import com.golfingbuddy.utils.SKPing;
import com.golfingbuddy.utils.SkApi;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.ApacheClient;

/**
 * Created by sardar on 4/29/14.
 */
public class SkApplication extends MultiDexApplication {

    public enum PLUGINS {
        PHOTO {
            public String toString() {
                return "photo";
            }
        },
        BOOKMARKS {
            public String toString() {
                return "bookmarks";
            }
        },
        GUESTS {
            public String toString() {
                return "ocs_guests";
            }
        },
        CHAT {
            public String toString() {
                return "chat";
            }
        },
        WINK {
            public String toString() {
                return "winks";
            }
        },
        PROFILE_COVER_GALLERY {
            public String toString() {
                return "pcgallery";
            }
        },
        USER_SEARCH {
            public String toString() {
                return "usearch";
            }
        },
        MATCHMAKING {
            public String toString() {
                return "matchmaking";
            }
        },
        MAILBOX {
            public String toString() {
                return "mailbox";
            }
        },
        MEMBERSHIPS {
            public String toString() {
                return "memberships";
            }
        },
        USER_CREDITS {
            public String toString() {
                return "user_credits";
            }
        }
    }

    public static final String PACKAGE = "com.skadatexapp";

    private static final String API_LOCATION = "api/android/";
    private static final String KEY_SITE_URL = "sk_site_url";
    private static final String KEY_SITE_AUTH_TOKEN = "sk_auth_token";
    private static final String KEY_SITE_INFO = "sk_site_info";
    private static final String KEY_USER_INFO = "sk_user_info";
    private static final String KEY_MENU_INFO = "sk_menu_info";
    private static final String PREFIX = "base";

    public final static String EVENT_INTERNET_CONNECTION_STATE = "base.inet_state";

    private static SkSite siteInfo;
    private static SkUser userInfo;
    private static ArrayList<SkMenuItem> menuInfo;
    private static ArrayList<String> activePlugins;

    private static String authToken;
    private static String siteUrl;

    private static SKConfig mConfig;
    private static SKLanguage mLanguage;
    private static SKDate mDate;
    private static SKPing mPing;

    private static final Object objLock = new Object();
    private static HashMap<String, SkRestInterface> adapters = new HashMap<>();

    public static SKConfig getConfig() {
        return mConfig;
    }
    private static int pingDelay = 5000;
    public static SKLanguage getLanguage() {
        return mLanguage;
    }
    public static SKDate getDate() {
        return mDate;
    }
    public static SKPing getPing() {
        return mPing;
    }

    private BaseServiceHelper helper;
    private static SkApplication application;

    private Class foregroundActivityClass;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        mConfig = SKConfig.getInstance(this);
        mLanguage = SKLanguage.getInstance(this);
        helper = BaseServiceHelper.getInstance(this);
        mDate = SKDate.getInstance();
        mPing = SKPing.getInstance(this);

        // Mailbox ping handler
        MailboxPingHandler.getInstance().register(this);

        SkAnalytics.init();
        SkAds.init(this);

        final Handler handler = new Handler();
        Runnable runable = new Runnable() {
            @Override
            public void run() {
                new InetConnectionCheckTask().execute();

                try{
                    HashMap<String, String> commands = mPing.start();

                    helper.getSiteInfo(commands, new SkServiceCallbackListener() {
                        @Override
                        public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
                            Intent intent = new Intent(SKPing.ACTION_RESULT);
                            intent.putExtra(SKPing.RESULT, data);
                            LocalBroadcastManager.getInstance(SkApplication.this).sendBroadcast(intent);

                            if( SkApi.checkResult(data) == SkApi.API_RESULT.SUCCESS ){
                                saveOrUpdateSiteInfo(data, SkApplication.this);
                            }

                            if ( mDate.getCalendar() == null && SkApplication.getSiteInfo() != null ) {
                                SkSite skSite = SkApplication.getSiteInfo();
                                mDate.initServerTime(skSite.getServerTimestamp(), skSite.getServerTimezone());
                            }
                        }
                    });
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally{
                    //also call the same runnable
                    handler.postDelayed(this, pingDelay);
                }
            }
        };
        handler.postDelayed(runable, pingDelay);
    }

    public static SkApplication getApplication(){
        return application;
    }

    public static SkRestInterface getRestAdapter() {
        synchronized (objLock) {
            String key = getSiteUrl();
            if (getAuthToken() != null) {
                key += "token";
            }

            final String tag = application.getLocalityLanguageTag();

            if (adapters.get(key) == null) {
                if (authToken == null) {
                    RestAdapter restAdapter = getRestBuilder().build();
                    adapters.put(key, restAdapter.create(SkRestInterface.class));
                } else {

                    RestAdapter.Builder builder = getRestBuilder();
                    builder.setRequestInterceptor( new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            request.addHeader("api-auth-token", getAuthToken());
                            request.addHeader("api-language", tag);
                        }
                    } );

                    RestAdapter restAdapter = builder.build();
                    adapters.put(key, restAdapter.create(SkRestInterface.class));
                }
            }

            return adapters.get(key);
        }
    }

    private static RestAdapter.Builder getRestBuilder() {
        String apiUrl = getSiteUrl();

        if (apiUrl == null) {
            return null;
        }

        if (!apiUrl.substring(apiUrl.length() - 1).equals("/")) {
            apiUrl += "/";
        }

        //SETS UP PARAMETERS
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "utf-8");
        HttpProtocolParams.setUseExpectContinue(params, true);
        params.setBooleanParameter("http.protocol.expect-continue", false);
        HttpProtocolParams.setUserAgent(params, "ANDROID");
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 50000);
        params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 50000);

        //REGISTERS SCHEMES FOR BOTH HTTP AND HTTPS
        HostnameVerifier hostnameVerifier = SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
        SchemeRegistry registry = new SchemeRegistry();

        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", new TlsSniSocketFactory(), 443));
        SingleClientConnManager mgr = new SingleClientConnManager(params, registry);

        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setEndpoint(apiUrl + API_LOCATION)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog("OKHTTP_CLIENT"))
                .setClient(new ApacheClient(getThreadSafeClient(new DefaultHttpClient(mgr, params))));

        return builder;
    }

    private static DefaultHttpClient getThreadSafeClient( DefaultHttpClient client)  {
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
        return client;
    }

    public static String getAuthToken() {
        if (authToken == null) {
            authToken = mConfig.getStringValue(PREFIX, KEY_SITE_AUTH_TOKEN, null);
        }

        return authToken;
    }

    public static void setAuthToken(String pAuthToken) {
        authToken = pAuthToken;
        if (pAuthToken == null) {
            mConfig.removeConfig(PREFIX, KEY_SITE_AUTH_TOKEN);
        } else {
            mConfig.saveConfig(PREFIX, KEY_SITE_AUTH_TOKEN, pAuthToken);
        }
    }

    public static void setUrl(String url) {
        siteUrl = url;
        if (url == null) {
            mConfig.removeConfig(PREFIX, KEY_SITE_URL);
        } else {
            mConfig.saveConfig(PREFIX, KEY_SITE_URL, url);
        }
    }

    public static String getSiteUrl() {

        if (SkStaticData.SITE_URL != null) {
            return SkStaticData.SITE_URL;
        }

        if (siteUrl == null) {
            siteUrl = mConfig.getStringValue(PREFIX, KEY_SITE_URL, null);
        }

        return siteUrl;
    }

    public static void saveOrUpdateSiteInfo(Bundle bundle, SkApplication app) {
        JsonObject result = SkApi.processResult(bundle);

        if( result == null ){
            return;
        }

        if( SkApi.propExistsAndNotNull(result, "token") ){
            setAuthToken(result.get("token").getAsString());
        }

        if( SkApi.propExistsAndNotNull(result, "activePluginList") ){
            JsonArray list = result.getAsJsonArray("activePluginList");
            activePlugins = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                activePlugins.add(list.get(i).getAsString());
            }
        }

        if (SkApi.propExistsAndNotNull(result, "siteInfo")) {
            siteInfo = new Gson().fromJson(result.getAsJsonObject("siteInfo"), SkSite.class);
            mConfig.removeConfig(PREFIX, KEY_SITE_INFO);
            mConfig.saveConfig(PREFIX, KEY_SITE_INFO, result.getAsJsonObject("siteInfo").toString());
        }

        if (SkApi.propExistsAndNotNull(result, "userInfo")) {
            userInfo = new Gson().fromJson(result.getAsJsonObject("userInfo"), SkUser.class);
            mConfig.removeConfig(PREFIX, KEY_USER_INFO);
            mConfig.saveConfig(PREFIX, KEY_USER_INFO, result.getAsJsonObject("userInfo").toString());
        }

        if (SkApi.propExistsAndNotNull(result, "menuInfo")) {
            menuInfo = new Gson().fromJson(result.getAsJsonArray("menuInfo"), new TypeToken<ArrayList<SkMenuItem>>(){}.getType());
            mConfig.removeConfig(PREFIX, KEY_MENU_INFO);
            mConfig.saveConfig(PREFIX, KEY_MENU_INFO, result.getAsJsonArray("menuInfo").toString());
        }

        Intent intent = new Intent("base.site_info_updated");
        LocalBroadcastManager.getInstance(app).sendBroadcast(intent);
    }

    public static SkSite getSiteInfo() {

        if( siteInfo == null ){
            String data = mConfig.getStringValue(PREFIX, KEY_SITE_INFO, null);

            if( data != null ){
                siteInfo = new Gson().fromJson(data, SkSite.class);
            }
        }

        return siteInfo;
    }

    public static void setSiteInfo( SkSite siteInfo ) {
        if( siteInfo == null ){
            mConfig.removeConfig(PREFIX, KEY_SITE_INFO);
        }
        SkApplication.siteInfo = siteInfo;
        //TODO need to update data in configs
    }

    public static SkUser getUserInfo() {

        if( userInfo == null ){
            String data = mConfig.getStringValue(PREFIX, KEY_USER_INFO, null);

            if( data != null ){
                userInfo = new Gson().fromJson(data, SkUser.class);
            }
        }

        return userInfo;
    }

    public static void setUserInfo(SkUser userInfo) {
        if( userInfo == null ){
            mConfig.removeConfig(PREFIX, KEY_USER_INFO);
        }

        SkApplication.userInfo = userInfo;
        //TODO need to update data in configs
    }

    public static ArrayList<SkMenuItem> getMenuInfo() {
        if( menuInfo == null ){
            String data = mConfig.getStringValue(PREFIX, KEY_MENU_INFO);

            if( data != null ){
                menuInfo = new Gson().fromJson(data, new TypeToken<ArrayList<SkMenuItem>>(){}.getType());
            }
        }

        return menuInfo;
    }

    public static boolean isPluginActive(PLUGINS plugin) {
        if( activePlugins == null ){
            return true;
        }

        return activePlugins.contains(plugin.toString());
    }

    public Class getForegroundActivityClass() {
        return foregroundActivityClass;
    }

    public void setForegroundActivityClass(Class foregroundActivityClass) {
        this.foregroundActivityClass = foregroundActivityClass;
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

    }

    public String getLocalityLanguageTag() {
        Locale current = getResources().getConfiguration().locale;
        return current.toString();
    }

    private class InetConnectionCheckTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void...arg0) {
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com");

                if (ipAddr.equals("")) {
                    return false;
                } else {
                    return true;
                }

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Intent intent = new Intent(EVENT_INTERNET_CONNECTION_STATE);
            intent.putExtra("data", result);
            LocalBroadcastManager.getInstance(SkApplication.this).sendBroadcast(intent);
        }
    }
}