package com.example.ece1778;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{
    List<Comment> commentList;
    LayoutInflater inflater;
    Context ctx;

    public CommentAdapter(Context ctx, List<Comment> commentList){
        this.ctx = ctx;
        this.commentList = commentList;
        this.inflater = LayoutInflater.from(ctx);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_layout_comment,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Show Commenter Avatar
        Picasso.get().load(commentList.get(position).getDisplayPicPath()).into(holder.commenterAvatar);
        //Show Commenter Username
        holder.commenterUsername.setText(commentList.get(position).getUsername());
        //Show Comment Content
        holder.commentContent.setText(commentList.get(position).getCommentContent());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView commenterAvatar;
        TextView commenterUsername;
        TextView commentContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            commenterAvatar = itemView.findViewById(R.id.commenterAvatar);
            commenterUsername = itemView.findViewById(R.id.commenterUsername);
            commentContent = itemView.findViewById(R.id.commentContent);
        }
    }
}
