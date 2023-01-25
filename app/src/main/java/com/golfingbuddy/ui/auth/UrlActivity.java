package com.golfingbuddy.ui.auth;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.golfingbuddy.R;
import com.golfingbuddy.core.SkApplication;
import com.golfingbuddy.core.TlsSniSocketFactory;
import com.golfingbuddy.ui.base.email_verification.EmailVerification;
import com.golfingbuddy.ui.join.model.UserData;
import com.golfingbuddy.ui.join.view.JoinForm;
import com.golfingbuddy.ui.search.service.QuestionService;
import com.golfingbuddy.utils.SkApi;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.conn.ssl.SSLSocketFactory;
/**
 * Created by sardar on 6/11/14.
 */
public class UrlActivity extends BaseAuthActivity {

    String urlVal;
    TextView text;
    ImageButton button;
    ProgressBar progress;
    boolean imgButtonState = false, blockSubmit = false;

    public static String ON_CHANGE_SITE_URL = "change_sute_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.auth_url_activity);
        super.onCreate(savedInstanceState, false);
        text = (TextView) findViewById(R.id.text);
        button = (ImageButton) findViewById(R.id.button);
        progress = (ProgressBar)findViewById(R.id.progressBar);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if( blockSubmit ){
                    return;
                }

                if( imgButtonState ){
                    hideText();
                }
                else{
                    showText();
                }
            }
        });
        final EditText url = (EditText)findViewById(R.id.urlField);
        url.setHint(getResources().getString(R.string.url_page_input_invitation));
        url.requestFocus();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        url.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if( blockSubmit ){
                        return true;
                    }
                    // Perform action on key press
                    urlVal = url.getText().toString();

                    if( !Patterns.WEB_URL.matcher(urlVal).matches() ){
                        Toast.makeText(UrlActivity.this, getString(R.string.invalid_url_error_msg), Toast.LENGTH_LONG).show();
                        return true;
                    }

                    new ApiCheckAsyncTask().execute();

                    return true;
                }

                return false;

            }
        });

        String urlVal = getApp().getSiteUrl();
        if( urlVal != null ){
            url.setText(urlVal);
        }
    }

    private void showText(){
        text.setVisibility(View.VISIBLE);
        button.setBackgroundResource(R.drawable.speedmatches_info_on);
        imgButtonState = true;
    }

    private void hideText(){
        text.setVisibility(View.GONE);
        button.setBackgroundResource(R.drawable.speedmatches_info);
        imgButtonState = false;
    }

    class ApiCheckAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideText();
            blockSubmit = true;
            progress.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(String... urls) {
            if (!urlVal.substring(urlVal.length() - 1).equals("/")) {
                urlVal += "/";
            }

            try {
                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, "utf-8");
                params.setBooleanParameter("http.protocol.expect-continue", false);
                HttpProtocolParams.setUserAgent(params, "ANDROID");
                params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 50000);
                params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 50000);

                HostnameVerifier hostnameVerifier = SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;

//                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//                trustStore.load(null, null);

                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", new TlsSniSocketFactory(), 443));
                SingleClientConnManager mgr = new SingleClientConnManager(params, registry);
                DefaultHttpClient httpClient = new DefaultHttpClient(mgr, params);

                HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
                HttpGet httppost = new HttpGet(urlVal+"api/android/base/check-api");

                HttpResponse response = httpClient.execute(httppost);

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);

                    if( data != null ){
                        JsonObject result = new Gson().fromJson(data, JsonObject.class);

                        if(SkApi.propExistsAndNotNull(result, "type") && result.get("type").getAsString().equals("success")){
                            return result.getAsJsonObject("data").getAsJsonObject("data").get("siteUrl").getAsString();
                        }
                    }
                }

            } catch (Exception e) {
                e.toString();
            }
            return null;
        }

//        class MySSLSocketFactory extends SSLSocketFactory {
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//
//            public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
//                super(truststore);
//
//                TrustManager tm = new X509TrustManager() {
//                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    public X509Certificate[] getAcceptedIssuers() {
//                        return null;
//                    }
//                };
//
//                sslContext.init(null, new TrustManager[] { tm }, null);
//            }
//
//            @Override
//            public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
//                return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
//            }
//
//            @Override
//            public Socket createSocket() throws IOException {
//                return sslContext.getSocketFactory().createSocket();
//            }
//        }

        protected void onPostExecute(String result) {
            if( result != null ) {
                String siteUrl = SkApplication.getSiteUrl();

                if ( siteUrl == null || !siteUrl.equals(result) ) {
                    QuestionService.getInstance().removeSharedPreferences(SkApplication.getApplication(), EmailVerification.PREFIX);
                    QuestionService.getInstance().removeSharedPreferences(SkApplication.getApplication(), JoinForm.PREFIX);
                    QuestionService.getInstance().removeSharedPreferences(SkApplication.getApplication(), "search");
                    QuestionService.getInstance().removeSharedPreferences(SkApplication.getApplication(), "edit");
                    UserData.clean(SkApplication.getApplication());
                }

                SkApplication.setUrl(result);

                startActivity(new Intent(UrlActivity.this, AuthActivity.class));
            }
            else {
                Toast.makeText(UrlActivity.this, UrlActivity.this.getString(R.string.invalid_site_error_msg), Toast.LENGTH_LONG).show();
            }

            blockSubmit = false;
            progress.setVisibility(View.GONE);
        }
    }
}
