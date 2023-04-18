package com.uc.degura.view.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uc.degura.R;
import com.uc.degura.model.InstructionNote;

import java.util.List;

public class FishGillAdapter extends RecyclerView.Adapter<FishGillAdapter.ViewPagerViewHolder>{

    private Context context;

    private List<InstructionNote> instructionNoteList;

    public FishGillAdapter(Context context) {
        this.context = context;
    }

    public List<InstructionNote> getInstructionNoteList() {
        return instructionNoteList;
    }

    public void setInstructionNoteList(List<InstructionNote> instructionNoteList) {
        this.instructionNoteList = instructionNoteList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FishGillAdapter.ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewpager_instruction, parent, false);
        return new FishGillAdapter.ViewPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FishGillAdapter.ViewPagerViewHolder holder, int position) {
        InstructionNote instructionNote = instructionNoteList.get(position);

        holder.fish_instruction_item.setImageResource(instructionNote.getImages());
        holder.detail_instruction_txt.setText(instructionNote.getInstruction());
    }

    @Override
    public int getItemCount() {
        return getInstructionNoteList().size();
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
