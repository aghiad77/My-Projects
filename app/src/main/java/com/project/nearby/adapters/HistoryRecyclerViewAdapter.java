package com.project.nearby.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.nearby.R;
import com.project.nearby.models.History;
import com.project.nearby.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {
    private List<History> histories;
    private Context context;
    public static SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yyyy HH:mm:ss", Locale.ENGLISH);
    public HistoryRecyclerViewAdapter(List<History> histories , Context context) {
        this.histories = histories;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=  LayoutInflater.from(parent.getContext()).inflate(R.layout.neighbour_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            holder.getName().setText(histories.get(position).getName());
            holder.getDistance().setText(String.valueOf((int) (Math.round(histories.get(position).getDistance() * 100)) / 100.0) + " m");
            if (histories.get(position).getBle_distance() != 0)
                holder.getBleDistance().setText("Ble  " + String.valueOf(histories.get(position).getBle_distance()) + " m");
            else
                holder.getBleDistance().setText("BLE not Found");
            if (histories.get(position).getTimeSpent() != 0) {
                String time = Utils.getTimeperiod(histories.get(position).getTimeSpent());
                holder.getTime().setText(time.split(";")[0] + "  " + time.split(";")[1]);
            } else {
                try {
                    String time = Utils.getTimeperiod(Utils.timeDifference(new Date().getTime(), sdf.parse(histories.get(position).getTimeStamp()).getTime()));
                    holder.getTime().setText(time.split(";")[0] + "  " + time.split(";")[1]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
            holder.getDate().setText(histories.get(position).getTimeStamp());
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView time;
        TextView distance;
        TextView ble_distance;
        TextView date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            time = (TextView) itemView.findViewById(R.id.duration);
            distance = (TextView) itemView.findViewById(R.id.distance);
            ble_distance = (TextView) itemView.findViewById(R.id.ble_distance);
            date = (TextView) itemView.findViewById(R.id.date);
        }

        public TextView getName() {
            return name;
        }

        public TextView getTime() {
            return time;
        }

        public TextView getDistance() {
            return distance;
        }

        public TextView getBleDistance() {
            return ble_distance;
        }

        public TextView getDate() {
            return date;
        }
    }
}
