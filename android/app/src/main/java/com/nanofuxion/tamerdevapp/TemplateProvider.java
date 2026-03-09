// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.nanofuxion.tamerdevapp;
import com.nanofuxion.tamerdevapp.BuildConfig;
import com.nanofuxion.tamerdevapp.DevServerPrefs;

import com.lynx.tasm.provider.AbsTemplateProvider;
public class TemplateProvider extends AbsTemplateProvider {
    private final android.content.Context context;

    public TemplateProvider(android.content.Context context) {
        this.context = context.getApplicationContext();
    }

      private static final String DEV_CLIENT_BUNDLE = "dev-client.lynx.bundle";

    @Override
    public void loadTemplate(String url, final Callback callback) {
        new Thread(() -> {
            if (url != null && (url.equals(DEV_CLIENT_BUNDLE) || url.endsWith("/" + DEV_CLIENT_BUNDLE) || url.contains(DEV_CLIENT_BUNDLE))) {
                try {
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    try (java.io.InputStream is = context.getAssets().open(DEV_CLIENT_BUNDLE)) {
                        byte[] buf = new byte[1024];
                        int n;
                        while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
                    }
                    callback.onSuccess(baos.toByteArray());
                } catch (java.io.IOException e) {
                    callback.onFailed(e.getMessage());
                }
                return;
            }
            if (BuildConfig.DEBUG) {
                String devUrl = DevServerPrefs.INSTANCE.getUrl(context);
                if (devUrl != null && !devUrl.isEmpty()) {
                    try {
                        java.net.URL u = new java.net.URL(devUrl);
                        String base = u.getProtocol() + "://" + u.getHost() + (u.getPort() > 0 ? ":" + u.getPort() : ":3000") + (u.getPath() != null && !u.getPath().isEmpty() ? u.getPath() : "");
                        String fetchUrl = base.endsWith("/") ? base + url : base + "/" + url;
                        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                            .build();
                        okhttp3.Request request = new okhttp3.Request.Builder().url(fetchUrl).build();
                        try (okhttp3.Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful() && response.body() != null) {
                                callback.onSuccess(response.body().bytes());
                                return;
                            }
                            callback.onFailed("HTTP " + response.code() + " for " + fetchUrl);
                            return;
                        }
                    } catch (Exception e) {
                        callback.onFailed("Fetch failed: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
                        return;
                    }
                }
            }
            try {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                try (java.io.InputStream is = context.getAssets().open(url)) {
                    byte[] buf = new byte[1024];
                    int n;
                    while ((n = is.read(buf)) != -1) {
                        baos.write(buf, 0, n);
                    }
                }
                callback.onSuccess(baos.toByteArray());
            } catch (java.io.IOException e) {
                callback.onFailed(e.getMessage());
            }
        }).start();
    }
}
