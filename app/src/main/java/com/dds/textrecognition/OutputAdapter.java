package com.dds.textrecognition;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class OutputAdapter extends RecyclerView.Adapter<OutputAdapter.OutputViewHolder>{
    List<String> outputList;

    public OutputAdapter(List<String> outputList) {
        this.outputList = outputList;
    }

    @NonNull
    @Override
    public OutputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_output, parent, false);
        return new OutputViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OutputViewHolder holder, int position) {
        holder.tvOutput.setText(outputList.get(position));
    }

    @Override
    public int getItemCount() {
        return outputList.size();
    }

    public class OutputViewHolder extends RecyclerView.ViewHolder {
        TextView tvOutput;

        public OutputViewHolder(View view) {
            super(view);
            tvOutput = view.findViewById(R.id.tv_output);
        }
    }
}
