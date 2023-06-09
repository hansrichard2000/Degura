package com.uc.degura.view.result;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uc.degura.R;

import java.util.List;

public class ResultsImageAdapter extends RecyclerView.Adapter<ResultsImageAdapter.ViewPagerViewHolder>{

    private Context context;
    private List<Bitmap> images;

    public ResultsImageAdapter(Context context, List<Bitmap> images) {
        this.context = context;
        this.images = images;
    }


    @NonNull
    @Override
    public ResultsImageAdapter.ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewpager_image_content,parent, false);
        return new ResultsImageAdapter.ViewPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultsImageAdapter.ViewPagerViewHolder holder, int position) {
        Bitmap currentImage = images.get(position);
        holder.fish_image_item.setImageBitmap(currentImage);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewPagerViewHolder extends RecyclerView.ViewHolder {
        private ImageView fish_image_item;

        public ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            fish_image_item = itemView.findViewById(R.id.fish_image_item);
        }
    }
}
