package com.example.syncmaster;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class Adaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NewsModel> newsModels;
    private Context context;

    public Adaptor(Context context, List<NewsModel> newsModels) {
        this.context = context;
        this.newsModels = newsModels;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_news, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ViewHolder holder = (ViewHolder) viewHolder;
        holder.source.setText(newsModels.get(i).getNewsSource());
        holder.time.setText(newsModels.get(i).getTime());
        holder.desc.setText(newsModels.get(i).getDescription());
        holder.title.setText(newsModels.get(i).getTitle());

        String image = newsModels.get(i).getImage();

        Glide.with(context)
                .load(image)
                .placeholder(R.drawable.load)
                .into(holder.image);

    }

    @Override
    public int getItemCount() {
        return newsModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView source, time, desc, title;

        private ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.Image);
            source = itemView.findViewById(R.id.sourceName);
            time = itemView.findViewById(R.id.time);
            desc = itemView.findViewById(R.id.desc);
            title = itemView.findViewById(R.id.title);
        }
    }
}
