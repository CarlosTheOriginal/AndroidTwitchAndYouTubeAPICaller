package com.thegavners.androidtwitchandyoutubeapicaller;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TwitchStream extends AppCompatActivity {


    String channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitch_stream);


        Intent intent = getIntent();
        channel = intent.getStringExtra("Channel");

        Toast.makeText(this, "Playing "+ channel, Toast.LENGTH_SHORT).show();

        WebView streamWebView = findViewById(R.id.streamWebView);

        streamWebView.setWebViewClient(new WebViewClient());

        //The Twitch Embed uses JavaScript. Hence, we need to enable it inside the Web View.
        streamWebView.getSettings().setJavaScriptEnabled(true);

        channel.replaceAll("\\s+","");

        String twitchVideo = "<html><body><iframe src=\"https://player.twitch.tv/?channel="+channel+ "\" height=\"300\" width=\"400\"  frameborder=\"0\"  scrolling=\"no\"    </iframe></body></html>";

        streamWebView.loadData( twitchVideo, "text/html"  ,"UTF-8"        );


    }
}
