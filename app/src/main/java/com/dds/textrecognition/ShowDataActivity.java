package com.dds.textrecognition;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.dds.textrecognition.Model.Sample;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowDataActivity extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;

    RecyclerView rvShowData;
    SampleAdapter sampleAdapter;
    List<Sample> sampleList = new ArrayList<>();

    long counter, total;

    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_data);

        firebaseDatabase = FirebaseDatabase.getInstance();

        sampleAdapter = new SampleAdapter(sampleList);
        rvShowData = findViewById(R.id.rv_show_data);
        layoutManager = new LinearLayoutManager(this);
        rvShowData.setLayoutManager(layoutManager);
        rvShowData.setAdapter(sampleAdapter);

        fetchData();
    }

    private void fetchData() {
        dbRef = firebaseDatabase.getReference("samples");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                total = dataSnapshot.getChildrenCount();
//                Toast.makeText(ShowDataActivity.this, "Total: "  + total, Toast.LENGTH_SHORT).show();
                for (counter = 0; counter < total; counter++) {
                    addSampleToList(counter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void addSampleToList(final long i) {
        DatabaseReference itemRef = dbRef.child("" + i);

        itemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Sample sample =  dataSnapshot.getValue(Sample.class);
                sampleList.add(sample);
                Log.e("Asif", "" + sample.getWord());

                if (i == (total-1)) {
                    Log.e("Asif", "Total:- " + sampleList.size());
                    sampleAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
