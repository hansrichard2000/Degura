package com.uc.degura.view.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uc.degura.R;
import com.uc.degura.model.InstructionNote;
import com.uc.degura.view.detection.FishImageAdapter;

import java.util.List;

public class FishEyeAdapter extends RecyclerView.Adapter<FishEyeAdapter.ViewPagerViewHolder>{

    private Context context;

    private List<InstructionNote> instructionList;

    public FishEyeAdapter(Context context) {
        this.context = context;
    }

    public List<InstructionNote> getInstructionList() {
        return instructionList;
    }

    public void setInstructionList(List<InstructionNote> instructionList) {
        this.instructionList = instructionList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FishEyeAdapter.ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewpager_instruction, parent, false);
        return new FishEyeAdapter.ViewPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FishEyeAdapter.ViewPagerViewHolder holder, int position) {
        InstructionNote instructionNote = instructionList.get(position);

        holder.fish_instruction_item.setImageResource(instructionNote.getImages());
        holder.detail_instruction_txt.setText(instructionNote.getInstruction());
    }

    @Override
    public int getItemCount() {
        return getInstructionList().size();
    }

    public class ViewPagerViewHolder extends RecyclerView.ViewHolder {
        private ImageView fish_instruction_item;
        private TextView detail_instruction_txt;

        public ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            fish_instruction_item = itemView.findViewById(R.id.fish_instruction_item);
            detail_instruction_txt = itemView.findViewById(R.id.detail_instruct_txt);
        }
    }
}
