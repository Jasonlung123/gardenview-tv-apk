package com.zengarden.tv;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String HOST = "appassets.local";
    private static final String BASE = "https://" + HOST + "/";
    private static final String GAME_DIR = "PVZGARDENVIEW";
    private WebView web;
    private long lastBack = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        File ext = getExternalFilesDir(null);
        File gameDir = ext == null ? null : new File(ext, GAME_DIR + "/reanim");
        if (gameDir != null && !gameDir.exists()) gameDir.mkdirs();

        web = new WebView(this);
        web.setBackgroundColor(0xFF173009);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return serve(r.getUrl());
            }
        });
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        web.requestFocus();
        hideBars();
        web.loadUrl(BASE + "index.html");

        String[] have = gameDir == null ? null : gameDir.list();
        boolean ext_ok = have != null && have.length > 0;
        if (!hasAssetFolder() && !ext_ok) {
            Toast.makeText(this, "Game images missing. Copy the folder " + GAME_DIR + " to:\n"
                    + (ext == null ? "(no storage)" : ext.getAbsolutePath()), Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasAssetFolder() {
        try {
            String[] l = getAssets().list(GAME_DIR + "/reanim");
            if (l == null) return false;
            for (String n : l) if (n.toLowerCase().endsWith(".png")) return true;
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private WebResourceResponse serve(Uri u) {
        if (!HOST.equals(u.getHost())) return null;
        String p = u.getPath();
        if (p == null || p.length() <= 1) p = "/index.html";
        p = p.substring(1);
        if (p.contains("..")) return notFound();
        InputStream in = null;
        try {
            if (p.startsWith(GAME_DIR + "/")) {
                File ext = getExternalFilesDir(null);
                if (ext != null) {
                    File f = new File(ext, p);
                    if (f.isFile()) in = new FileInputStream(f);
                }
            }
            if (in == null) in = getAssets().open(p);
        } catch (IOException e) {
            return notFound();
        }
        Map<String, String> h = new HashMap<String, String>();
        h.put("Access-Control-Allow-Origin", "*");
        return new WebResourceResponse(mime(p), null, 200, "OK", h, in);
    }

    private WebResourceResponse notFound() {
        return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found",
                new HashMap<String, String>(), new ByteArrayInputStream(new byte[0]));
    }

    private static String mime(String p) {
        String l = p.toLowerCase();
        if (l.endsWith(".html")) return "text/html";
        if (l.endsWith(".js")) return "application/javascript";
        if (l.endsWith(".png")) return "image/png";
        if (l.endsWith(".jpg") || l.endsWith(".jpeg")) return "image/jpeg";
        if (l.endsWith(".json")) return "application/json";
        if (l.endsWith(".css")) return "text/css";
        return "application/octet-stream";
    }

    private void hideBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) hideBars();
    }

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - lastBack < 1500) { finish(); return; }
        lastBack = now;
        web.evaluateJavascript("window.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape'}))", null);
        Toast.makeText(this, "Press Back again to exit", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onPause() { super.onPause(); web.onPause(); }
    @Override protected void onResume() { super.onResume(); web.onResume(); }
    @Override protected void onDestroy() { web.destroy(); super.onDestroy(); }
}
