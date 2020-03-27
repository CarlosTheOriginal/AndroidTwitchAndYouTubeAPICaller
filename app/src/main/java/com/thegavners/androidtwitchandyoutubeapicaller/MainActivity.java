package com.thegavners.androidtwitchandyoutubeapicaller;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private YouTube mYouTubeDataApi;
    private final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    private List<FeedRow> listOutputFeedRows = new ArrayList<>();
    private FeedListViewAdapter feedListViewAdapter;

    private final List<FeedRow> listTwitchStreams = new ArrayList<>();
    private final List<FeedRow> listYouTubeStreams = new ArrayList<>();


    String activeContent;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_you_tube:

                    new GetPlayListDataAsyncTask(mYouTubeDataApi) {


                        @Override
                        protected void onPostExecute(SearchListResponse searchListResponse) {
                            super.onPostExecute(searchListResponse);

                            activeContent = "YouTube";


                            if (searchListResponse == null) {
                                Toast.makeText(MainActivity.this, "Can't get YouTube Streams", Toast.LENGTH_SHORT).show();
                            } else {

                                try {


                                    JSONObject object = new JSONObject(searchListResponse.toString());


                                    JSONArray array = object.getJSONArray("items");

                                    for (int i = 0; i < array.length(); i++) {

                                        Log.i("youtubeapi2", "List Items are being called.");

                                        JSONObject jsonPart = array.getJSONObject(i);

                                        listYouTubeStreams.add(new FeedRow(
                                                jsonPart.getJSONObject("snippet").getString("channelTitle"),
                                                "https://img.youtube.com/vi/" + jsonPart.getJSONObject("id").getString("videoId") + "/sddefault.jpg",
                                                jsonPart.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url"),
                                                jsonPart.getJSONObject("id").getString("videoId")


                                        ));

                                    }
                                    listOutputFeedRows.clear();
                                    listOutputFeedRows.addAll(listYouTubeStreams);
                                    listYouTubeStreams.clear();
                                    feedListViewAdapter.notifyDataSetChanged();

                                } catch (Exception e) {

                                }
                            }
                        }
                    }.execute();

                    return true;

                case R.id.navigation_twitch:
                    new GetTwitchStreams().execute("https://api.twitch.tv/kraken/streams");

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView outputListView = findViewById(R.id.contentList);
        feedListViewAdapter = new FeedListViewAdapter(getApplication(), listOutputFeedRows);
        outputListView.setAdapter(feedListViewAdapter);

        outputListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (activeContent.matches("Twitch")){

                    Intent stream = new Intent(getApplicationContext(), TwitchStream.class);
                    stream.putExtra("Channel", listOutputFeedRows.get(position).getId());
                    startActivity(stream);

                }else
                    if(activeContent.matches("YouTube")){
                        Intent youTubeVideo = YouTubeStandalonePlayer.createVideoIntent(MainActivity.this,"Your You Tube Key Here",listOutputFeedRows.get(position).getId());
                        startActivity(youTubeVideo);

                    }
                    else {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
            }
        });

        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mYouTubeDataApi = new YouTube.Builder(mTransport, jsonFactory, null)
                .build();

        new GetPlayListDataAsyncTask(mYouTubeDataApi) {


            @Override
            protected void onPostExecute(SearchListResponse searchListResponse) {
                super.onPostExecute(searchListResponse);

                activeContent = "YouTube";


                if (searchListResponse == null) {
                    Toast.makeText(MainActivity.this, "Can't get YouTube Streams", Toast.LENGTH_SHORT).show();
                } else {

                    try {


                        JSONObject object = new JSONObject(searchListResponse.toString());


                        JSONArray array = object.getJSONArray("items");

                        for (int i = 0; i < array.length(); i++) {

                            Log.i("youtubeapi2", "List Items are being called.");

                            JSONObject jsonPart = array.getJSONObject(i);

                            listYouTubeStreams.add(new FeedRow(
                                    jsonPart.getJSONObject("snippet").getString("channelTitle"),
                                    "https://img.youtube.com/vi/" + jsonPart.getJSONObject("id").getString("videoId") + "/sddefault.jpg",
                                    jsonPart.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").getString("url"),
                                    jsonPart.getJSONObject("id").getString("videoId")


                            ));

                        }
                        listOutputFeedRows.clear();
                        listOutputFeedRows.addAll(listYouTubeStreams);
                        listYouTubeStreams.clear();
                        feedListViewAdapter.notifyDataSetChanged();

                    } catch (Exception e) {

                    }
                }
            }
        }.execute();




    }


    public static class GetPlayListDataAsyncTask extends AsyncTask<String[], Void, SearchListResponse> {

        private static final String youTubePart = "snippet";

        private YouTube mYouTubeDataApi;

        public GetPlayListDataAsyncTask(YouTube api) {
            mYouTubeDataApi = api;
        }

        @Override
        protected SearchListResponse doInBackground(String[]... strings) {


            Log.i("YouTubeCall", "Started");

            SearchListResponse searchListResponse;
            try {

                searchListResponse = mYouTubeDataApi.search()
                        .list(youTubePart)
                        .setEventType("live")
                        .setType("video")
                        .setVideoCategoryId("20")
                        .setKey("Your YouTube Key Here")
                        .execute();

                Log.i("youtubeapi2", "The result is " + searchListResponse);
                return searchListResponse;


            } catch (IOException e) {
                Log.i("youtubeapi2", "Failed");
                e.printStackTrace();
                return null;
            }

        }
    }

    class GetTwitchStreams extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            activeContent = "Twitch";

            URL url;
            HttpURLConnection connection;
            String result;

            try{
                //Establishing a connection with the URL executed in the onCreate Method
                url = new URL(urls[0]);

                connection = (HttpURLConnection) url.openConnection();

                //Setting the client ID for API call
                connection.setRequestProperty("Client-ID:","Your Twitch Client Id Here");
               // connection.setRequestProperty("Client-ID:","Your Client ID ");

                connection.connect();

                //Reads the result from connection
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();

                result = sb.toString();

                Log.i("TwitchAPI", "Success. \n The result is \n" + result);

                return result;
            }

            catch(Exception e){

                Log.i("twitchapi", e.toString());

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            Log.i("TwitchApi", "Post Execution started");

            if(result == null){
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }

            try {

                // Since the result is a JSON object. It is appropriate to work it as a JSON rather than a String.
                JSONObject object = new JSONObject(result);

                //The result contains multiple JSON Object.Thus, a JSON array
                JSONArray array = object.getJSONArray("streams");

                //A loop that goes through the JSON array and adds the objects to the custom ListView Accordingly
                for (int i = 0; i < array.length(); i++) {

                    Log.i("TwitchAPI", "List Items are being called.");

                    JSONObject jsonPart = array.getJSONObject(i);

                    listTwitchStreams.add(new FeedRow(
                            jsonPart.getJSONObject("channel").getString("display_name"),
                            jsonPart.getJSONObject("preview").getString("large"),
                            jsonPart.getJSONObject("channel").getString("logo"),
                            jsonPart.getJSONObject("channel").getString("display_name")

                    ));
                }

                listOutputFeedRows.clear();
                listOutputFeedRows.addAll(listTwitchStreams);
                listTwitchStreams.clear();
                feedListViewAdapter.notifyDataSetChanged();
            }

            catch (Exception e){

                e.printStackTrace();
                Log.i("TwitchAPi", "Failed"+ e.getMessage());

            }
        }
    }
}

class FeedRow {


    private String title;
    private String thumbnailURL;
    private String profileURL;
    private String id;


    FeedRow(String rowTitle,String rowThumbnail,String rowProfile, String rowId ) {


       title = rowTitle;
       thumbnailURL = rowThumbnail;
       profileURL = rowProfile;
       id = rowId;




    }


    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    String getThumbnailURL(){
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL){
        this.profileURL = thumbnailURL;
    }

    String getProfile(){
        return profileURL;
    }

    public void setProfileURL(String profileURL){
        this.profileURL = profileURL;
    }

    String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

}

class FeedListViewAdapter extends BaseAdapter {

    private final Context context;
    private final List<FeedRow> feedRowList;

    FeedListViewAdapter(Context context, List<FeedRow> feedRowList) {

        this.context = context;
        this.feedRowList = feedRowList;
    }

    @Override
    public int getCount() {
        return feedRowList.size();
    }

    @Override
    public Object getItem(int position) {
        return feedRowList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        FeedListViewAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(context).inflate(R.layout.feed_item, null);

            viewHolder = new FeedListViewAdapter.ViewHolder();

            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.profile = convertView.findViewById(R.id.profilePhoto);
            viewHolder.thumbnail = convertView.findViewById(R.id.thumbnail);


            convertView.setTag(viewHolder);

        } else {
            viewHolder = (FeedListViewAdapter.ViewHolder) convertView.getTag();

        }
        FeedRow feedRow = feedRowList.get(position);


        viewHolder.title.setText(feedRow.getTitle());

        Picasso.get()
                .load(feedRow.getThumbnailURL())
                .placeholder(R.color.colorAccent)
                .into(viewHolder.thumbnail);

        Picasso.get()
                .load(feedRow.getProfile())
                .placeholder(R.color.colorAccent)
                .into(viewHolder.profile);



        return convertView;
    }

    class ViewHolder {

        TextView title;
        ImageView profile;
        ImageView thumbnail;



    }
}