package com.dds.textrecognition;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dds.textrecognition.Model.Sample;
import com.dds.textrecognition.Model.SampleData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.SampleViewHolder>{
    List<Sample> sampleList;

    public SampleAdapter(List<Sample> sampleList) {
        this.sampleList = sampleList;
    }

    @NonNull
    @Override
    public SampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_show_data, parent, false);
        return new SampleViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleViewHolder holder, int position) {
        Sample sample = sampleList.get(position);
        List<String> output;
        String strOutput = "";

        if (sample != null) {
            byte[] decodedString = Base64.decode(sample.getImageUrl(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imgSample.setImageBitmap(decodedByte);
//        Picasso.get()
//                .load()
//                .into(holder.imgSample);
            holder.tvWord.setText(sample.getWord());
            holder.tvUserId.setText(sample.getUserId());
            holder.tvDeviceId.setText(sample.getDeviceId());

            SampleData data = sample.getData();
            if (data.getRequest().getLanguage().equals("bn")) holder.tvLanguage.setText("Bangla");
            output = data.getResult().getOutput();
            if (output != null) {
                Log.e("Asif", "Output Total" + output.size());

                int outputSize = output.size();

                for (int i=0; i<outputSize; i++) {
                    strOutput += output.get(i);
                    if (i != (outputSize-1)) strOutput += ", ";
                }

                holder.tvOutput.setText(strOutput);
            }
        }
    }

    @Override
    public int getItemCount() {
        return sampleList.size();
    }


    public class SampleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSample;
        TextView tvUserId, tvDeviceId, tvWord, tvLanguage, tvOutput;

        public SampleViewHolder(View view) {
            super(view);

            tvUserId = view.findViewById(R.id.user_id);
            tvDeviceId = view.findViewById(R.id.device_id);
            imgSample = view.findViewById(R.id.img_sample);
            tvWord = view.findViewById(R.id.sample_word);
            tvLanguage = view.findViewById(R.id.sample_language);
            tvOutput = view.findViewById(R.id.sample_output);
        }
    }
}
