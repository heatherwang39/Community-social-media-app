package com.example.ece1778;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    List<Post> postList;
    LayoutInflater inflater;
    Context ctx;

    public PostAdapter(Context ctx, List<Post> postList){
        this.ctx = ctx;
        this.postList = postList;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout_post,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load(postList.get(position).getStorageRef()).into(holder.postView);
//        Glide.with(this).load(postList.get(position).getStorageRef()).into(holder.postView);
        holder.postView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, Comment.class);
                intent.putExtra("postURL", postList.get(position).getStorageRef());
                intent.putExtra("uID", postList.get(position).getUID());
                intent.putExtra("caption", postList.get(position).getCaption());
                ctx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView postView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postView = itemView.findViewById(R.id.postView);
        }
    }
}