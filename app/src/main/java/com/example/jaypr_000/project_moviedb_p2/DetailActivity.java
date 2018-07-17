package com.example.jaypr_000.project_moviedb_p2;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static android.support.constraint.Constraints.TAG;


public class DetailActivity extends AppCompatActivity {

    private TextView overViewText;
   private ImageView thumbnailImage;
   private TextView vote;
   private TextView release;
   private TextView originalTitleTextView;
   private ProgressBar progressBar;

   String api_key = "ENTER_API_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        //Action Bar sets the return to Home Button in Movie Detail Activity.
        Toolbar detailToolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        setSupportActionBar(detailToolbar);
         ActionBar actionBar = getSupportActionBar();

         if(actionBar != null) {
             actionBar.setDisplayHomeAsUpEnabled(true);
             }


        String overViewSummary = getIntent().getStringExtra("overview");
        String image = getIntent().getStringExtra("backdrop_path");
        String relD = getIntent().getStringExtra("release_date");
        String voteAverage = getIntent().getStringExtra("vote_average");
        String originalTitle = getIntent().getStringExtra("title");
        String movieId = getIntent().getStringExtra("id");

        final String trailerLink = "https://api.themoviedb.org/3/movie/"+ movieId +"/videos?language=en-US&api_key=" + api_key;

        originalTitleTextView = (TextView) findViewById(R.id.detail_TitleTextView);
        overViewText = (TextView) findViewById(R.id.detail_textView);
        thumbnailImage = (ImageView) findViewById(R.id.detailphoto);
        vote = (TextView) findViewById(R.id.votingAverage);
        release = (TextView) findViewById(R.id.releaseDate);


        originalTitleTextView.setText(Html.fromHtml(originalTitle));
        overViewText.setText(Html.fromHtml(overViewSummary));
        vote.setText(Html.fromHtml(voteAverage));
        release.setText(Html.fromHtml(relD));
        Picasso.get().load(image).into(thumbnailImage);


        ImageButton imgButtonOne = (ImageButton) findViewById(R.id.imageButtonOne);
        imgButtonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DetailDownloadTask().execute(trailerLink);


            }
        });



        }

    public class DetailDownloadTask extends AsyncTask <String, Void, String>{
        @Override
        protected void onPreExecute() {
            //progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String keyReturnValue) {

                String trailerLinkOne = "https://www.youtube.com/watch?v=" + keyReturnValue;
                Uri webpage = Uri.parse(trailerLinkOne);
                Intent videoIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(videoIntent);
                //Toast.makeText(getApplicationContext(),"Your video will start here" , Toast.LENGTH_LONG).show();

                //Verify it resolves
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(videoIntent, packageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size()>0;

                if(isIntentSafe){
                    startActivity(videoIntent);
                }

        }


       @Override
        protected String doInBackground(String... params) {



            HttpURLConnection urlConnection;
            String keyReturnValue=null;

            try{

                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int statusCode = urlConnection.getResponseCode();

                if(statusCode == 200){

                    BufferedReader k = new BufferedReader(new InputStreamReader
                            (urlConnection.getInputStream()));

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = k.readLine()) != null){
                        response.append(line);
                    }

                    System.out.println("The Link for parsing is ");
                    keyReturnValue=parseVideoResult(response.toString());

                    System.out.println(parseVideoResult(response.toString()));
                }
                else{

                    Toast.makeText(getApplicationContext(),"Failed to Fetch Data" , Toast.LENGTH_LONG).show();
                }

            }catch (Exception e){

                Log.d(TAG,e.getLocalizedMessage() );
            }
            return keyReturnValue;
        }
    }

    private  String parseVideoResult(String videoTrailerLink){

        String idValue;
        String keyValue;
        String[] idStrArray = new String[3];
        String[] keyStrArray = new String[3];


        try{

            //iterate loop
            JSONObject object = new JSONObject(videoTrailerLink);
            JSONArray jsonArray = object.getJSONArray("results");
            System.out.println(jsonArray.length());
            for(int i =0; i < jsonArray.length(); i++){

                JSONObject childJSONObject = jsonArray.getJSONObject(i);
                idValue = childJSONObject.getString("id");
                keyValue = childJSONObject.getString("key");
                idStrArray = new String[]{idValue, idValue, idValue};
                keyStrArray = new String[] {keyValue, keyValue, keyValue};



            }
            //System.out.println(idStrArray[0]);
            //System.out.println(idStrArray[1]);
           // System.out.println(idStrArray[2]);


        }catch (JSONException e){
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }

        return keyStrArray[0];

    }


}
