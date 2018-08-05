package com.example.jaypr_000.project_moviedb_p2;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
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
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.support.constraint.Constraints.TAG;


public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "MovieDBData";
    private TextView overViewText;
    private ImageView thumbnailImage;
    private TextView vote;
    private TextView release;
    private TextView originalTitleTextView;
    private ProgressBar progressBar;
    private TextView authornameOne;
    private TextView contentOne;
    private TextView authornameTwo;
    private TextView contentTwo;
    private TextView noReviews;

    String api_key = "ENTER_YOUR_KEY_HERE";


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

        //ArrayList reviewLink = new ArrayList<>();
        String newReviewLink = "https://api.themoviedb.org/3/movie/"+movieId+"/reviews?api_key="+api_key+"&language=en-US&page=1";
        //reviewLink.add(newReviewLink);

        //String s = Arrays.toString(reviewLink.toArray());

        System.out.println("The Review Link is : " + newReviewLink);


        originalTitleTextView = (TextView) findViewById(R.id.detail_TitleTextView);
        overViewText = (TextView) findViewById(R.id.detail_textView);
        thumbnailImage = (ImageView) findViewById(R.id.detailphoto);
        vote = (TextView) findViewById(R.id.votingAverage);
        release = (TextView) findViewById(R.id.releaseDate);
        //authorname = (TextView) findViewById(R.id.author_one_name);


        originalTitleTextView.setText(Html.fromHtml(originalTitle));
        overViewText.setText(Html.fromHtml(overViewSummary));
        vote.setText(Html.fromHtml(voteAverage));
        release.setText(Html.fromHtml(relD));
        Picasso.get().load(image).into(thumbnailImage);

        ArrayList<String> reviewResult = new ArrayList<String>();

        new ReviewDownLoadTask().execute(newReviewLink);


        ImageButton imgButtonOne = (ImageButton) findViewById(R.id.imageButtonOne);
        imgButtonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DetailDownloadTask().execute(trailerLink);



            }
        });



        // Create a Cursor.

        SQLiteOpenHelper movieDBDatabaseHelper = new MovieDBDatabaseHelper(this);
        try{

            SQLiteDatabase db = movieDBDatabaseHelper.getReadableDatabase();

            Cursor cursor = db.query("MOVIEDB", new String[]{ "MOVIEID","TITLE","FAVORITE"}, "MOVIEID='"+movieId+"'", null, null, null, null);

            System.out.println("The Cursor count is " + cursor.getCount());

            // Move to first record in the cursor.
            if(cursor.moveToFirst()){
                final CheckBox favorite = (CheckBox) findViewById(R.id.favoriteCheckBox);
                favorite.setChecked(true);

            }

            cursor.close();
            db.close();
        }
        catch(SQLiteException e){
            Toast toast = Toast.makeText(this,"Database Unavailable in onCreate" ,Toast.LENGTH_SHORT );
            toast.show();

        }

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
                    Toast.makeText(getApplicationContext(),"DetailActivity: Failed to Fetch Data" , Toast.LENGTH_LONG).show();
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
        }catch (JSONException e){
            e.printStackTrace();
        }

        return keyStrArray[0];

    }

    public  class ReviewDownLoadTask extends AsyncTask <String, Void, ArrayList<String>> {





        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }



        @Override
        protected void onPostExecute(ArrayList<String> reviewReturnValue) {

            System.out.println("The size of an ArrayList is : " + reviewReturnValue.size());

            if( reviewReturnValue.size() == 0){

                System.out.println("Do Nothing");

            }
            else if(reviewReturnValue.size() < 3) {
                authornameOne = (TextView) findViewById(R.id.author_one_name);
                authornameOne.setText(reviewReturnValue.get(0));
                contentOne = (TextView) findViewById(R.id.content_one_review);
                contentOne.setText(reviewReturnValue.get(1));
            }else if(reviewReturnValue.size() < 5)  {

                authornameOne = (TextView) findViewById(R.id.author_one_name);
                authornameOne.setText(reviewReturnValue.get(0));
                contentOne = (TextView) findViewById(R.id.content_one_review);
                contentOne.setText(reviewReturnValue.get(1));

                authornameTwo = (TextView) findViewById(R.id.author_two_name);
                authornameTwo.setText(reviewReturnValue.get(2));
                contentTwo = (TextView) findViewById(R.id.content_two_review);
                contentTwo.setText(reviewReturnValue.get(3));
            }else {
                authornameOne = (TextView) findViewById(R.id.author_one_name);
                authornameOne.setText(reviewReturnValue.get(0));
                contentOne = (TextView) findViewById(R.id.content_one_review);
                contentOne.setText(reviewReturnValue.get(1));

                authornameTwo = (TextView) findViewById(R.id.author_two_name);
                authornameTwo.setText(reviewReturnValue.get(2));
                contentTwo = (TextView) findViewById(R.id.content_two_review);
                contentTwo.setText(reviewReturnValue.get(3));

                authornameOne = (TextView) findViewById(R.id.author_one_name);
                authornameOne.setText(reviewReturnValue.get(4));
                contentOne = (TextView) findViewById(R.id.content_one_review);
                contentOne.setText(reviewReturnValue.get(5));
            }



            super.onPostExecute(reviewReturnValue);
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            HttpURLConnection urlConnection;
            ArrayList<String> reviewReturnValue = new ArrayList<>();
            //Integer reviewResult = 0;

            try{
                //ArrayList<String> reviewPassedList = params[0];
                //URL url = new URL(reviewPassedList.toString());
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int reviewstatuscode = urlConnection.getResponseCode();

                if(reviewstatuscode == 200){
                    BufferedReader k = new BufferedReader(new InputStreamReader
                            (urlConnection.getInputStream()));

                    StringBuilder reviewResponse = new StringBuilder();
                    String line;
                    while ((line = k.readLine()) != null){
                        reviewResponse.append(line);
                    }

                    System.out.println();
                    reviewReturnValue=  parseReviewResult(reviewResponse.toString());
                    //reviewResult = 1;

                    System.out.println("The List of Values are :" + reviewReturnValue);


                }else{

                    // Toast.makeText(DetailActivity.this,"Review: Failed to Fetch Data" , Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Review Connection Failed");

                    //reviewResult = 0;

                }

            }catch(Exception e){

                Log.d(TAG,e.getLocalizedMessage() );
            }

            return reviewReturnValue;

        }


    }

    private static ArrayList<String> parseReviewResult (String reviewResult){

        String author;
        String content;
        ArrayList<String> userReviews = new ArrayList<>();

        try{
            JSONObject reviewObject = new JSONObject(reviewResult);
            JSONArray reviewArray = reviewObject.getJSONArray("results");

            for (int j = 0; j < reviewArray.length(); j++){

                JSONObject reviewChildObject = reviewArray.getJSONObject(j);
                author = reviewChildObject.getString("author");
                userReviews.add(author);
                content = reviewChildObject.getString("content");
                userReviews.add(content);
            }

        }catch(JSONException e){

            e.printStackTrace();
        }

        return userReviews;
    }

    //update the database when checkbox is clicked.
    public void onFavoriteClicked (View view){

        String movieIdTwo = getIntent().getStringExtra("id");
        long rows;

        // Get the Value of the Checkbox.
        CheckBox favorite = (CheckBox) findViewById(R.id.favoriteCheckBox);
        //Ankur Code Start


        //Ankur Code End
        ContentValues movieCheckboxValue = new ContentValues();
        // Adding the value of favorite checkbox to movieCheckboxValue ContentValues object.
        movieCheckboxValue.put("MOVIEID",movieIdTwo );
        movieCheckboxValue.put("TITLE","Title" );
        movieCheckboxValue.put("FAVORITE",favorite.isChecked());

        //Get a Reference to the database and update the FAVORITE column.
        SQLiteOpenHelper movieDBDatabaseHelper = new MovieDBDatabaseHelper(this );


        try{
            SQLiteDatabase db = movieDBDatabaseHelper.getWritableDatabase();
            if(favorite.isChecked()){
                rows =  db.insert("MOVIEDB",null,movieCheckboxValue);

            }else{

                //long rows = db.delete("MOVIEDB",null,movieCheckboxValue);
                rows =  db.delete("MOVIEDB", "MOVIEID" + "=" + movieIdTwo, null);

            }

            //int rows = db.update("MOVIEDB", movieCheckboxValue, "_id = ?", new String[] {movieIdTwo});
            System.out.println(movieIdTwo);
            Log.d(TAG,"Rows updated:" + rows + " Movie ID"+ movieIdTwo);
            if (rows >= 1){
                Toast passtoast = Toast.makeText(this,"Database updated" ,Toast.LENGTH_SHORT );
                passtoast.show();
            }
            else{
                Toast passtoast = Toast.makeText(this,"Something wrong with updation" ,Toast.LENGTH_SHORT );
                passtoast.show();
            }

        }catch (SQLiteException e){

            Toast toast = Toast.makeText(this,"Database Unavailable in OnFavoriteClicked Method" ,Toast.LENGTH_SHORT );
            toast.show();

        }


    }


    // AsyncTask and JSON parsing for Reviews.




}
