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

    public ResultsAdapter(Context context) {
        this.context = context;
    }

    public List<String> getResult_list() {
        return result_list;
    }

    public void setResult_list(List<String> result_list) {
        this.result_list = result_list;
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
        Log.d(TAG, "onBindViewHolder: "+fresh_result);
        int urutan = position+1;
        holder.fish_attr_txt.setText("Status Kesegaran Mata "+urutan+" :");
        holder.clasifier_result.setText(fresh_result);
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
