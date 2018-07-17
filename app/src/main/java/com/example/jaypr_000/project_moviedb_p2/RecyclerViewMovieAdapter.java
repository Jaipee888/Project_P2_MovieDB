package com.example.jaypr_000.project_moviedb_p2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewMovieAdapter extends RecyclerView.Adapter<RecyclerViewMovieAdapter.CustomViewHolder>{


        private List<MovieData> MovieDataList;
        private Context mcontext;

        public RecyclerViewMovieAdapter(Context context, List<MovieData> MovieDataList){

            this.MovieDataList = MovieDataList;
            this.mcontext = context;
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder{

            protected ImageView imageView;
            protected TextView textView;
           // protected TextView detailTextView;

            public CustomViewHolder(View view) {

                super(view);
                this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
                this.textView = (TextView) view.findViewById(R.id.title);
               // this.detailTextView = (TextView) view.findViewById(R.id.detail_textView);

            }

        }





    @Override
    public CustomViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_view,null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return  viewHolder;
    }


    @Override
    public void onBindViewHolder( CustomViewHolder customViewHolder, int i) {

        final MovieData movieItem = MovieDataList.get(i);

        //Load all the images using below if statement.

        if(!TextUtils.isEmpty(movieItem.getImagePoster())){

            Picasso.get().load(movieItem.getImagePoster())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.imageView);
        }



        View.OnClickListener listener = new View.OnClickListener(){

            @Override
            public void onClick(View v){
                onItemClickListener.onItemClick(movieItem);

            }
        };

        customViewHolder.imageView.setOnClickListener(listener);
        customViewHolder.textView.setOnClickListener(listener);

        // Set Text view title.
        //customViewHolder.textView.setText(Html.fromHtml(movieItem.getOriginalTitle()));

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return (null != MovieDataList? MovieDataList.size():0);
    }

    private OnItemClickListener onItemClickListener;

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
