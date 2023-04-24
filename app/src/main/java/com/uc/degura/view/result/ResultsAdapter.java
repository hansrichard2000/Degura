package com.uc.degura.view.result;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uc.degura.R;

import java.util.ArrayList;
import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.TextViewHolder> {

    private static final String TAG = "ResultsAdapter";

    private Context context;
    private List<String> result_list;

    private List<String> title_list;

    public ResultsAdapter(Context context) {
        this.context = context;
    }

    public List<String> getResult_list() {
        return result_list;
    }

    public void setResult_list(List<String> result_list) {
        this.result_list = result_list;
    }

    public List<String> getTitle_list() {
        return title_list;
    }

    public void setTitle_list(List<String> title_list) {
        this.title_list = title_list;
    }

    @NonNull
    @Override
    public ResultsAdapter.TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_freshness_stats, parent, false);
        return new TextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultsAdapter.TextViewHolder holder, int position) {
        final String fresh_result = getResult_list().get(position);
//        final String title = getTitle_list().get(position);
        Log.d(TAG, "onBindViewHolder: "+fresh_result);
        int real_pos = position+1;
//        "eye-fresh", "eye-non-fresh", "gill-fresh", "gill-non-fresh"
        if (fresh_result.equals("eye-fresh")){
            holder.fish_attr_txt.setText("Status Kesegaran Mata "+real_pos+" :");
            holder.clasifier_result.setText("segar");
        } else if (fresh_result.equals("eye-non-fresh")) {
            holder.fish_attr_txt.setText("Status Kesegaran Mata "+real_pos+" :");
            holder.clasifier_result.setText("tidak segar");
        } else if (fresh_result.equals("gill-fresh")){
            holder.fish_attr_txt.setText("Status Kesegaran Insang "+real_pos+" :");
            holder.clasifier_result.setText("segar");
        } else if (fresh_result.equals("gill-non-fresh")) {
            holder.fish_attr_txt.setText("Status Kesegaran Insang "+real_pos+" :");
            holder.clasifier_result.setText("tidak segar");
        } else {
            holder.fish_attr_txt.setText("Tidak terdeteksi apapun");
            holder.clasifier_result.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return result_list.size();
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {

        TextView fish_attr_txt, clasifier_result;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            fish_attr_txt = itemView.findViewById(R.id.fish_attr_txt);
            clasifier_result = itemView.findViewById(R.id.fresh_stats);
        }
    }
}
