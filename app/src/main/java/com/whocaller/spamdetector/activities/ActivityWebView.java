/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.snackbar.Snackbar;
import com.whocaller.spamdetector.databinding.ActivityWebviewBinding;
import com.whocaller.spamdetector.utils.Utils;

public class ActivityWebView extends AppCompatActivity {

    String url;

    ActivityWebviewBinding binding;
    OnBackPressedCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebviewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        url = intent.getStringExtra("link");

        loadData();

        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);


        binding.backBtn.setOnClickListener(v -> callback.handleOnBackPressed());
    }


    @SuppressLint("SetJavaScriptEnabled")
    public void loadData() {

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setBuiltInZoomControls(false);
        binding.webView.getSettings().setSupportZoom(true);
        binding.webView.setWebViewClient(new CustomWebViewClient());

        if (url.startsWith("http://") || url.startsWith("https://")) {
            binding.webView.loadUrl(url);
        } else {
            Utils.loadHtml(this, binding.webView, url, true);
        }

        binding.webView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                WebView webView = (WebView) v;
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                        binding.lytProgress.setVisibility(View.GONE);
                        return true;
                    }
                }
            }
            return false;
        });


        binding.webView.setWebChromeClient(new WebChromeClient() {
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                binding.webView.setVisibility(View.INVISIBLE);
            }

            public void onProgressChanged(WebView view, int newProgress) {
                binding.progressBar.setProgress(newProgress, true);
                if (newProgress == 100) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.progressBar.setProgress(0);
                    }, 1000);
                } else {
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
            }

            public void onHideCustomView() {
                super.onHideCustomView();
                binding.webView.setVisibility(View.VISIBLE);
            }
        });

    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            binding.lytProgress.setVisibility(View.VISIBLE);
            String url = request.getUrl().toString();

            if (url.startsWith("http://") || url.startsWith("https://")) {
                if (url.contains("?target=external")) {
                    String newUrl = url.replace("?target=external", "");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newUrl));
                    startActivity(intent);
                } else if (url.contains("?package=")) {
                    Utils.startExternalApplication(ActivityWebView.this, url);
                } else {
                    view.loadUrl(url);
                }
            }

            actionHandler("mailto:", Intent.ACTION_SENDTO, url);
            actionHandler("sms:", Intent.ACTION_SENDTO, url);
            actionHandler("tel:", Intent.ACTION_DIAL, url);

            socialHandler(url, "intent://instagram", "com.instagram.android");
            socialHandler(url, "instagram://", "com.instagram.android");
            socialHandler(url, "twitter://", "com.twitter.android");
            socialHandler(url, "https://maps.google.com", "com.google.android.apps.maps");
            socialHandler(url, "https://api.whatsapp.com", "com.whatsapp");
            socialHandler(url, "https://play.google.com/store/apps/details?id=", null);

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            binding.progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            binding.progressBar.setVisibility(View.GONE);
            Snackbar.make(findViewById(android.R.id.content), "Try again later", Snackbar.LENGTH_SHORT).show();
            view.loadUrl("about:blank");
        }

    }


    public void actionHandler(String type, String action, String url) {
        if (url != null && url.startsWith(type)) {
            Intent intent = new Intent(action, Uri.parse(url));
            startActivity(intent);
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void socialHandler(String url, String socialUrl, String packageName) {
        PackageManager packageManager = getPackageManager();
        if (url != null && url.startsWith(socialUrl)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            try {
                intent.setPackage(packageName);
                intent.setData(Uri.parse(url));
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            callback.handleOnBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.webView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.webView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.webView.destroy();
    }

}

