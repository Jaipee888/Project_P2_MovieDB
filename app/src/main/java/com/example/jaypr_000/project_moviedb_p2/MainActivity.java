package com.example.jaypr_000.project_moviedb_p2;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MovieDBData";
    private List<MovieData> MovieList;
    private RecyclerView mRecyclerView;
    private RecyclerViewMovieAdapter adapter;
    private ProgressBar progressBar;
    private String api_key ="ENTER_API_KEY";

    Spinner spinner;
    private String originalLink = "http://api.themoviedb.org/3/movie/popular?api_key="+api_key;
    private String popularityLink = "https://api.themoviedb.org/3/discover/movie?api_key="+api_key+"&language=en-US&sort_by=popularity.desc&include_adult=false&include_video=false&page=1";
    private String voteLink="https://api.themoviedb.org/3/movie/top_rated?api_key="+api_key+"&language=en-US&page=1";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        int numberOfColumns = 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,numberOfColumns));
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);


        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.default_sort, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(listener);


    }

    AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            switch (position){

                case 1:
                    new DownloadTask().execute(popularityLink);
                    break;
                case 2:
                    new DownloadTask().execute(voteLink);
                    break;
                case 3:
                    new DownloadTask().execute();

                default:
                    new DownloadTask().execute(originalLink);
                    break;

            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };






    public class DownloadTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    parseResult(response.toString());
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressBar.setVisibility(View.GONE);

            if (result == 1) {
                adapter = new RecyclerViewMovieAdapter(MainActivity.this, MovieList);
                mRecyclerView.setAdapter(adapter);

                /**
                 Below ClickListener will launch a detailActivity window.
                 */
                adapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(MovieData item) {

                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        String detailUrl = item.getMovieId();
                        intent.putExtra("backdrop_path",item.getPosterThumbnail());
                        intent.putExtra("overview",item.getOverView());
                        intent.putExtra("release_date",item.getReleaseDate());
                        intent.putExtra("vote_average",item.getUserRating());
                        intent.putExtra("title",item.getOriginalTitle());
                        intent.putExtra("id",item.getMovieId() );

                        System.out.println("The Movie ID is" + item.getMovieId());

                        startActivity(intent);

                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray posts = response.optJSONArray("results");
            MovieList = new ArrayList<>();

            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);
                MovieData item = new MovieData();
                item.setImagePoster("http://image.tmdb.org/t/p/w500/" + post.getString("poster_path"));
                item.setOriginalTitle(post.getString("title"));
                //item.setMovieId("https://api.themoviedb.org/3/movie/" + post.getString("id") + "?api_key="+api_key);
                item.setMovieId(post.getString("id"));
                item.setPosterThumbnail("http://image.tmdb.org/t/p/w500/" + post.getString("backdrop_path"));
                item.setUserRating(post.getString("vote_average"));
                item.setReleaseDate(post.getString("release_date"));
                item.setOverView(post.getString("overview"));


                //String trailerArray = "https://api.themoviedb.org/3/movie/" +post.getString("id")+
                        //"/videos?language=en-US&api_key="+api_key;



                MovieList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
