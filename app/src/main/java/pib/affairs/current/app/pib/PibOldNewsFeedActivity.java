package pib.affairs.current.app.pib;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appnext.core.AppnextError;
import com.appnext.nativeads.MediaView;
import com.appnext.nativeads.NativeAdRequest;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import utils.AdsSubscriptionManager;
import utils.AppController;
import utils.News;
import utils.NightModeManager;
import utils.SettingManager;

import static com.android.volley.VolleyLog.TAG;

public class PibOldNewsFeedActivity extends AppCompatActivity {

    WebView webView;
    private News news;
    private String tableDataString;

    ProgressDialog pDialog;
    private NativeAd nativeAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (NightModeManager.getNightMode(this)) {
            setTheme(R.style.ActivityTheme_Primary_Base_Dark);
        }
        setContentView(R.layout.activity_pib_old_news_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pDialog = new ProgressDialog(this);

        news = (News) getIntent().getSerializableExtra("news");
        try {
            getSupportActionBar().setTitle(news.getNewsID());
        } catch (Exception e) {
            e.printStackTrace();
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        webView = (WebView) findViewById(R.id.pibOld_webView);

        initializeWebView();

        getWebsite(news.getLink());

        showLoadingDialog("Loading...");

        try {
            Answers.getInstance().logCustom(new CustomEvent("old news search").putCustomAttribute("date", news.getNewsID()).putCustomAttribute("title", news.getTitle()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setSubtitle(news.getPubDate());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ddnews_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_open_browser) {
            onOpenInBrowser();
            return true;
        } else if (id == R.id.action_share) {
            openShareDialog(news.getLink());
        }

        return super.onOptionsItemSelected(item);
    }

    private void onOpenInBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getLink()));
        startActivity(browserIntent);
    }


    private void openShareDialog(String shortUrl) {


        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        //sharingIntent.putExtra(Intent.EXTRA_STREAM, newsMetaInfo.getNewsImageLocalPath());

        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shortUrl
                + "\n\n PIB Reader & DD News App -\n https://play.google.com/store/apps/details?id=app.crafty.studio.current.affairs.pib");
        startActivity(Intent.createChooser(sharingIntent, "share link via"));


        try {
            Answers.getInstance().logCustom(new CustomEvent("Share Link Created").putCustomAttribute("shared dd news link", news.getTitle()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void initializeWebView() {

        if (NightModeManager.getNightMode(this)) {
            webView.setBackgroundColor(Color.parseColor("#5a666b"));
        }

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                return shouldOverrideUrlLoading(url);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                Uri uri = request.getUrl();
                return shouldOverrideUrlLoading(uri.toString());
            }

            private boolean shouldOverrideUrlLoading(final String url) {
                // Log.i(TAG, "shouldOverrideUrlLoading() URL : " + url);

                // Here put your code
                webView.loadUrl(url);

                return true; // Returning True means that application wants to leave the current WebView and handle the url itself, otherwise return false.
            }
        });
    }

    private void getWebsite(final String url) {


        String tag_string_req = "string_req";

        loadCache(url);

        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                initializeActivityData(response);
                //webView.loadDataWithBaseURL("", response, "text/html", "UTF-8", "");


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();


            }
        });


        strReq.setShouldCache(true);
        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

        strReq.setTag(TextUtils.isEmpty(tag_string_req) ? TAG : tag_string_req);
        Volley.newRequestQueue(getApplicationContext()).add(strReq);


    }


    private void loadCache(String url) {

        Cache cache = AppController.getInstance().getRequestQueue().getCache();


        Cache.Entry entry = cache.get(url);
        if (entry != null) {
            //Cache data available.
            try {
                String response = new String(entry.data, "UTF-8");

                initializeActivityData(response);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // Cache data not exist.
        }

    }


    public void initializeActivityData(final String data) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {

                    Document doc = Jsoup.parse(data);


                    tableDataString = doc.select("#condiv").toString();


                } catch (Exception e) {
                    builder.append("Error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        tableDataString = "<html ><style>span{line-height: 120%;font-size:" + SettingManager.getTextSize(PibOldNewsFeedActivity.this) + "px}</style>" + tableDataString + "</html>";

                        webView.loadDataWithBaseURL("", tableDataString, "text/html", "UTF-8", "");

                        hideLoadingDialog();
                        initializeBottomNativeAds();

                    }
                });


            }
        }).start();


    }

    public void showLoadingDialog(String message) {
        pDialog.setMessage(message);
        pDialog.show();
    }

    public void hideLoadingDialog() {
        try {
            pDialog.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeBottomNativeAds() {

        if (AdsSubscriptionManager.getSubscription(this)) {
            return;
        }

        if (nativeAd == null) {

            nativeAd = new NativeAd(this, "1963281763960722_1972656879689877");
            nativeAd.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.d(TAG, "onError: " + adError);

                    try {
                        Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "RSTV FEED").putCustomAttribute("error", adError.getErrorMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    initializeAppnext();
                }

                @Override
                public void onAdLoaded(Ad ad) {


                    View adView = NativeAdView.render(PibOldNewsFeedActivity.this, nativeAd, NativeAdView.Type.HEIGHT_400);
                    CardView nativeAdContainer = (CardView) findViewById(R.id.ddnews_adContainer_LinearLayout);
                    // Add the Native Ad View to your ad container
                    nativeAdContainer.addView(adView);
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });

            // Initiate a request to load an ad.
            nativeAd.loadAd();
        }


    }

    private void initializeAppnext() {

        try {
            com.appnext.nativeads.NativeAd appNextNative = new com.appnext.nativeads.NativeAd(this, "ac73473d-6ca6-4e38-baa8-5a81ae7b908c");
            appNextNative.setAdListener(new com.appnext.nativeads.NativeAdListener() {
                @Override
                public void onAdLoaded(com.appnext.nativeads.NativeAd nativeAd) {
                    super.onAdLoaded(nativeAd);
                    Log.d(TAG, "onAdLoaded: ");

                    showAppnextNative(nativeAd);
                }

                @Override
                public void onAdClicked(com.appnext.nativeads.NativeAd nativeAd) {
                    super.onAdClicked(nativeAd);
                }

                @Override
                public void onError(com.appnext.nativeads.NativeAd nativeAd, AppnextError appnextError) {
                    super.onError(nativeAd, appnextError);
                    Log.d(TAG, "onError: ");
                    try {
                        Answers.getInstance().logCustom(new CustomEvent("Ad failed").putCustomAttribute("Placement", "App Next").putCustomAttribute("error", appnextError.getErrorMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void adImpression(com.appnext.nativeads.NativeAd nativeAd) {
                    super.adImpression(nativeAd);
                }
            });

            appNextNative.loadAd(new NativeAdRequest()
                    .setCachingPolicy(NativeAdRequest.CachingPolicy.ALL)
                    .setCreativeType(NativeAdRequest.CreativeType.ALL)
                    .setVideoLength(NativeAdRequest.VideoLength.SHORT)
                    .setVideoQuality(NativeAdRequest.VideoQuality.LOW)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showAppnextNative(com.appnext.nativeads.NativeAd appNextNative) {

        try {
            CardView nativeAdContainer = (CardView) findViewById(R.id.ddnews_adContainer_LinearLayout);
            nativeAdContainer.removeAllViews();

            View appNextNativeLayout = getLayoutInflater().inflate(R.layout.native_appnext_container, null);


            ImageView imageView = appNextNativeLayout.findViewById(R.id.appnextNative_na_icon);
            //The ad Icon
            appNextNative.downloadAndDisplayImage(imageView, appNextNative.getIconURL());


            TextView textView = appNextNativeLayout.findViewById(R.id.appnextNative_na_title);
            //The ad title
            textView.setText(appNextNative.getAdTitle());

            MediaView mediaView = appNextNativeLayout.findViewById(R.id.appnextNative_na_media);
            //Setting up the Appnext MediaView

            mediaView.setMute(true);
            mediaView.setAutoPLay(false);
            mediaView.setClickEnabled(true);
            appNextNative.setMediaView(mediaView);

            TextView description = appNextNativeLayout.findViewById(R.id.appnextNative_description);
            //The ad description
            String str = appNextNative.getAdDescription() + "\n" + appNextNative.getStoreDownloads() + " peoples have used the app";

            description.setText(str);

            Button ctaButton = appNextNativeLayout.findViewById(R.id.appnextNative_install);
            //ctaButton.setText(appNextNative.getCTAText());


            //Registering the clickable areas - see the array object in `setViews()` function
            ArrayList<View> clickableView = new ArrayList<>();
            clickableView.add(mediaView);
            clickableView.add(textView);
            clickableView.add(imageView);
            clickableView.add(ctaButton);
            appNextNative.registerClickableViews(clickableView);

            com.appnext.nativeads.NativeAdView nativeAdView = appNextNativeLayout.findViewById(R.id.appnextNative_na_view);
            //Setting up the entire native ad view
            appNextNative.setNativeAdView(nativeAdView);


            nativeAdContainer.addView(appNextNativeLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
