package com.example.ece1778;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class GlobalPostAdapter extends RecyclerView.Adapter<GlobalPostAdapter.ViewHolder>{
    List<Post> globalPostList;
    LayoutInflater inflater;
    Context ctx;

    public GlobalPostAdapter(Context ctx, List<Post> globalPostList){
        this.ctx = ctx;
        this.globalPostList = globalPostList;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout_global_post,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load(globalPostList.get(position).getStorageRef()).into(holder.globalPostView);
        holder.globalPostView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, CommentActivity.class);
                intent.putExtra("postURL", globalPostList.get(position).getStorageRef());
                intent.putExtra("uID", globalPostList.get(position).getUID());
                intent.putExtra("caption", globalPostList.get(position).getCaption());
                ctx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return globalPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView globalPostView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            globalPostView = itemView.findViewById(R.id.globalPostView);
        }
    }
}
