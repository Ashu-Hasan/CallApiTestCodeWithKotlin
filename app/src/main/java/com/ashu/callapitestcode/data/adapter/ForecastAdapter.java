package com.ashu.callapitestcode.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ashu.callapitestcode.R;
import com.ashu.callapitestcode.data.model.ForecastItem;
import com.ashu.callapitestcode.other.graphs.ImageLoaderUtil;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private List<ForecastItem> list;
    private Context context;

    public ForecastAdapter(Context context, List<ForecastItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ForecastItem item = list.get(position);

        holder.tvDay.setText(item.getDay());
        holder.tvTemp.setText(item.getMaxTemp() + "°/" + item.getMinTemp() + "°");
        holder.tvRain.setText("☁"+item.getRain() + "%");

        // SVG load (your method)
        ImageLoaderUtil.loadSvgIntoImageView(context, item.getIcon(), holder.imgIcon);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTemp, tvRain;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvRain = itemView.findViewById(R.id.tvRain);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}
