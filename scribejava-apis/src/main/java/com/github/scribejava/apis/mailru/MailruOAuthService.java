package com.github.scribejava.apis.mailru;

import com.github.scribejava.apis.MailruApi;
import java.net.URLDecoder;
import java.util.Map;
import java.util.TreeMap;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class MailruOAuthService extends OAuth20Service {

    /**
     * @param api api
     * @param apiKey apiKey
     * @param apiSecret apiSecret
     * @param callback callback
     * @param defaultScope defaultScope
     * @param responseType responseType
     * @param userAgent userAgent
     * @param httpClientConfig httpClientConfig
     * @param httpClient httpClient
     * @deprecated use {@link #MailruOAuthService(com.github.scribejava.apis.MailruApi, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.io.OutputStream, java.lang.String,
     * com.github.scribejava.core.httpclient.HttpClientConfig, com.github.scribejava.core.httpclient.HttpClient) }
     */
    @Deprecated
    public MailruOAuthService(MailruApi api, String apiKey, String apiSecret, String callback, String defaultScope,
            String responseType, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        this(api, apiKey, apiSecret, callback, defaultScope, responseType, null, userAgent, httpClientConfig,
                httpClient);
    }

    public MailruOAuthService(MailruApi api, String apiKey, String apiSecret, String callback, String defaultScope,
            String responseType, OutputStream debugStream, String userAgent, HttpClientConfig httpClientConfig,
            HttpClient httpClient) {
        super(api, apiKey, apiSecret, callback, defaultScope, responseType, debugStream, userAgent, httpClientConfig,
                httpClient);
    }

    @Override
    public void signRequest(String accessToken, OAuthRequest request) {
        // sig = md5(params + secret_key)
        request.addQuerystringParameter("session_key", accessToken);
        request.addQuerystringParameter("app_id", getApiKey());
        final String completeUrl = request.getCompleteUrl();

        try {
            final String clientSecret = getApiSecret();
            final int queryIndex = completeUrl.indexOf('?');
            if (queryIndex != -1) {
                final String urlPart = completeUrl.substring(queryIndex + 1);
                final Map<String, String> map = new TreeMap<>();
                for (String param : urlPart.split("&")) {
                    final String[] parts = param.split("=");
                    map.put(parts[0], (parts.length == 1) ? "" : parts[1]);
                }

                final StringBuilder urlNew = new StringBuilder();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    urlNew.append(entry.getKey());
                    urlNew.append('=');
                    urlNew.append(entry.getValue());
                }

                final String sigSource = URLDecoder.decode(urlNew.toString(), "UTF-8") + clientSecret;
                request.addQuerystringParameter("sig", md5(sigSource));
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String md5(String orgString) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] array = md.digest(orgString.getBytes(Charset.forName("UTF-8")));
            final Formatter builder = new Formatter();
            for (byte b : array) {
                builder.format("%02x", b);
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 is unsupported?", e);
        }
    }
}
