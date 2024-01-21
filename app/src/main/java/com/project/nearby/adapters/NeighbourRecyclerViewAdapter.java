package com.project.nearby.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.nearby.R;
import com.project.nearby.models.History;
import com.project.nearby.models.User;
import com.project.nearby.utils.Utils;

import java.util.List;

public class NeighbourRecyclerViewAdapter extends RecyclerView.Adapter<NeighbourRecyclerViewAdapter.ViewHolder> {
    List<User> users;
    Context context;
    public NeighbourRecyclerViewAdapter(List<User> users, Context context) {
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=  LayoutInflater.from(parent.getContext()).inflate(R.layout.home_screen_neighbour_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getName().setText(users.get(position).getName());
        holder.getStatus().setText(users.get(position).isStatus()?"Positive":"Negative");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (users.get(position).isStatus()) {
                holder.getStatus()
                        .setCompoundDrawables(context.getDrawable(R.drawable.ic_baseline_sentiment_very_satisfied_24), null, null, null);
            } else {
                holder.getStatus()
                        .setCompoundDrawables(context.getDrawable(R.drawable.ic_baseline_social_distance_24), null, null, null);
            }
            DrawableCompat.setTint(context.getDrawable(R.drawable.ic_baseline_social_distance_24), Color.RED);
            holder.getStatus().setCompoundDrawablePadding(5);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView status;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            status = (TextView) itemView.findViewById(R.id.status);
        }

        public TextView getName() {
            return name;
        }

        public TextView getStatus() {
            return status;
        }
    }
}
