package com.dds.textrecognition;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dds.textrecognition.Model.DrawingDataRequest;
import com.dds.textrecognition.Model.GoogleApiResult;
import com.dds.textrecognition.Model.Request;
import com.dds.textrecognition.Model.Sample;
import com.dds.textrecognition.Model.SampleData;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    DatabaseReference dbRef;
    StorageReference storageRef;
    List<Sample> samples = new ArrayList<>();
    Request request;

    DrawingView dv;
    RelativeLayout rlDrawingView;
    AutoCompleteTextView etUserId;
    Button btnOutput, btnPrev, btnNext, btnAddUser, btnDialogAdd, btnDialogSet, btnDialogCancel;
    TextView tvSampleWords, tvUserId;

    List<String> wordList = new ArrayList<>();

    OutputAdapter outputAdapter;
    RecyclerView rvOutput;
    LinearLayoutManager layoutManager;
    List<String> outputs = new ArrayList<>();

    RetrofitService service;

    int current, totalWords;
    long totalSamples;
    String currentUser;

    int max;
    String userId, deviceId;

    AlertDialog alert;
    SessionManager session;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("Asif", "onCreate() called ...");

        progressBar = findViewById(R.id.progress_bar);
        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            current = session.getLastWordPosition();
            currentUser = session.getUserName();
        }
        else {
            current = 0;
            currentUser = "";
        }



        deviceId = getUniqueIMEIId();
        service = RetrofitHelper.getRetrofitService();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        dbRef = firebaseDatabase.getReference("samples2");
        storageRef = firebaseStorage.getReference();

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totalSamples = dataSnapshot.getChildrenCount();
//                Toast.makeText(MainActivity.this, "Total Samples:" + totalSamples, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        tvUserId = findViewById(R.id.tv_user_id);
        if (currentUser.isEmpty()) tvUserId.setText("none");
        else tvUserId.setText(currentUser);
        tvSampleWords = findViewById(R.id.tv_sample_words);
        outputAdapter = new OutputAdapter(outputs);
        rvOutput = findViewById(R.id.rv_output);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvOutput.setLayoutManager(layoutManager);
        rvOutput.setAdapter(outputAdapter);

        rlDrawingView = findViewById(R.id.rl_canvas);
        btnOutput = findViewById(R.id.btn_output);
        btnOutput.setOnClickListener(this);
        btnPrev = findViewById(R.id.btn_prev);
        btnPrev.setOnClickListener(this);
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        btnAddUser = findViewById(R.id.btn_set_user);
        btnAddUser.setOnClickListener(this);

        dv = new DrawingView(this);
        rlDrawingView.addView(dv);

//        wordList = readFile("WordList.txt");
        wordList = readMedicalCorpusFile("MedicalWordList.txt");
        totalWords = wordList.size();
        tvSampleWords.setText(wordList.get(current));

        alert = new AlertDialog.Builder(this)
                .setView(R.layout.dialog_add_user)
                .create();
    }



    @Override
    protected void onStop() {
        super.onStop();
        Log.e("Asif", "onStop() called ...");
        session.setLastWordPosition(current);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String word;

        switch (id) {
            case R.id.btn_output:
                if (NetworkConnectionHelper.isConnected(this)) {
                    viewInkData();
                }
                else {
                    Toast.makeText(this, "Sorry! No Internet Connection.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_set_user:
                openNewUserWindow();
                break;

            case R.id.btn_prev:
                if (current > 0) {
                    outputs.clear();
                    outputAdapter.notifyDataSetChanged();
                    dv.clearDraw();
                    current--;
                    if ((word = wordList.get(current)) != null) tvSampleWords.setText(word);
                }
                break;

            case R.id.btn_next:
                if (currentUser.isEmpty())
                    Toast.makeText(this, "Please set your User ID to save your data!", Toast.LENGTH_SHORT).show();
                else {
                    if (outputs.isEmpty()) {
                        if (current < (totalWords-1)) {
                            outputs.clear();
                            outputAdapter.notifyDataSetChanged();
                            dv.clearDraw();
                            current++;
                            if ((word = wordList.get(current)) != null) tvSampleWords.setText(word);
                        }
                    }
                    else saveDataToFirebase();
                }
                break;

            case R.id.btn_dialog_add:
                addNewUser();
                break;

            case R.id.btn_dialog_set:
                setExistingUser();
                break;
        }
    }

    private DrawingDataRequest createRequestBody() {
        DrawingDataRequest drawingDataRequest = new DrawingDataRequest();
        drawingDataRequest.setDevice("Chrome/19.0.1084.46 Safari/536.5");

        List<Request> requestList = new ArrayList<>();
        List<List<List<Integer>>> inkList = InkData.getInkList();

        request = new Request();
        request.setInks(inkList);
        request.setLanguage("bn");
        requestList.add(request);
        drawingDataRequest.setRequests(requestList);

        return drawingDataRequest;
    }

    private void saveDataToFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        final Sample sample = new Sample();

        byte[] imgData = saveBitMap(dv);
        UploadTask uploadTask = storageRef.child("sample2_" + totalSamples + ".jpg").putBytes(imgData);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) throw task.getException();
                return storageRef.child("sample2_" + totalSamples + ".jpg").getDownloadUrl();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("asif-upload", "Upload Error: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Sorry! There was a problem saving your drawing", Toast.LENGTH_SHORT).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
        @Override
        public void onComplete(@NonNull Task<Uri> task) {
            progressBar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    Uri imgUri = task.getResult();
                    Log.e("asif-url", imgUri.toString());
                    Toast.makeText(MainActivity.this, "Your drawing has been saved", Toast.LENGTH_SHORT).show();

                    sample.setSampleId(totalSamples);
                    sample.setWord(wordList.get(current));
                    sample.setWordId(current);
                    sample.setUserId(currentUser);
                    sample.setDeviceId(deviceId);
                    sample.setImageUrl(imgUri.toString());

                    SampleData data = new SampleData();

                    if (request != null) data.setRequest(request);

                    GoogleApiResult googleApiResult = new GoogleApiResult();
                    googleApiResult.setOutput(outputs);
                    googleApiResult.setStatus("SUCCESS");
                    data.setResult(googleApiResult);
                    sample.setData(data);

                    Map<String, Object> mapSample = new HashMap<>();
                    mapSample.put("" + totalSamples, sample);

                    dbRef.updateChildren(mapSample).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Your data has been uploaded to Firebase", Toast.LENGTH_SHORT).show();

                                String word;
                                if (current < (totalWords - 1)) {
                                    outputs.clear();
                                    outputAdapter.notifyDataSetChanged();
                                    dv.clearDraw();
                                    current++;
                                    if ((word = wordList.get(current)) != null)
                                        tvSampleWords.setText(word);
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Sorry! Your data could not be saved. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                /***************************/
            }
        });

    }

    private void viewInkData () {
        DrawingDataRequest drawingDataRequest = createRequestBody();
        outputs.clear();
        outputAdapter.notifyDataSetChanged();
//        InkData.clearAll();

        Call responseCall = service.getGoogleApiResults(drawingDataRequest, "handwriting");
        responseCall.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.e("Asif", "Response Code: " + response.code());
                if (response.code() == 200) {
                    Gson gson = new Gson();
                    try {
                        JSONArray jsonArray = new JSONArray(gson.toJson(response.body()));
                        JSONArray jsonArray1 = (JSONArray) jsonArray.get(1);
                        JSONArray jsonArray2 = (JSONArray) jsonArray1.get(0);
                        JSONArray outputList = (JSONArray) jsonArray2.get(1);

                        int totalOutput = outputList.length();
                        Log.e("Asif", "Total Results: " + totalOutput);

                        for (int i=0; i<totalOutput; i++) {
                            outputs.add(outputList.get(i).toString());
                        }

                        outputAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });
    }

    private List<String> readFile(String file) {
        AssetManager assetManager = getAssets();
        List<String> words = new ArrayList<>();

        try {
            InputStream is = assetManager.open(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;

            while ((line = br.readLine()) != null) {
                words.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }

    private List<String> readMedicalCorpusFile(String file) {
        AssetManager assetManager = getAssets();
        List<String> words = new ArrayList<>();

        try {
            InputStream is = assetManager.open(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;

            while ((line = br.readLine()) != null) {
                words.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }

    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private byte[] saveBitMap(View drawView) {
        Bitmap bitmap = getBitmapFromView(drawView);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        return data;
    }

    private File saveBitMapAsFile(View drawView){
        File pictureFileDir = new File(Environment.getExternalStorageDirectory(),"Logicchip");
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if(!isDirectoryCreated)
                Log.i("TAG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() +File.separator+ System.currentTimeMillis()+".jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap = getBitmapFromView(drawView);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue saving the image.");
        }
        return pictureFile;
    }

    private void createUserId () {
        max = 0;
        if (etUserId != null) etUserId.setText("");

//        Log.e("asif-user", "Total Samples: " + totalSamples);
        if (totalSamples == 0) {
            if (etUserId != null) etUserId.append("User_" + max);
        }
        else {
            DatabaseReference refSamples = dbRef;
            refSamples.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        Sample sample = snapshot.getValue(Sample.class);
                        String userId = sample.getUserId();

                        Log.e("asif-user", "User ID: " + userId);
                        //check user id with numbers
                        if (userId.contains("User_")) {
                            String strId = userId.replace("User_", "");

                            try {
                                int id = Integer.parseInt(strId);
                                if (max < id) max = id;
                            }
                            catch (NumberFormatException e) {

                            }
                        }
                    }
                    ++max;
                    if (etUserId != null) etUserId.append("User_" + max);
                    Log.e("asif-user", "MAX: " + max);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void openNewUserWindow() {
        alert.show();
        etUserId = alert.findViewById(R.id.et_user_id);
        btnDialogAdd = alert.findViewById(R.id.btn_dialog_add);
        btnDialogAdd.setOnClickListener(this);
        btnDialogSet = alert.findViewById(R.id.btn_dialog_set);
        btnDialogSet.setOnClickListener(this);
        btnDialogCancel = alert.findViewById(R.id.btn_dialog_cancel);
        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        createUserId();
    }

    private void addNewUser() {
        userId = "";

        if (etUserId != null) userId = etUserId.getText().toString();

        if (userId.isEmpty()) {
            Toast.makeText(this, "Please give a User ID!", Toast.LENGTH_SHORT).show();
        }
        else {
            DatabaseReference refSamples = dbRef;
            refSamples.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isExists = false;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Sample sample = data.getValue(Sample.class);
                        if (sample.getUserId().equals(userId)) {
                            isExists = true;
                        }
                    }
                    if (isExists) Toast.makeText(MainActivity.this, "This user ID already exists", Toast.LENGTH_SHORT).show();
                    else {
                        currentUser = userId;
                        tvUserId.setText(currentUser);
                        session.createUserSession(currentUser);
                        current = 0;
                        if (currentUser.equals("shepon")) current = 2067;
                        tvSampleWords.setText(wordList.get(current));
                        alert.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("asif-query", "" + databaseError);
                }
            });

        }
    }

    private void setExistingUser() {
        userId = "";

        if (etUserId != null) userId = etUserId.getText().toString();

        if (userId.isEmpty()) {
            Toast.makeText(this, "Please give a User ID!", Toast.LENGTH_SHORT).show();
        }
        else {
            DatabaseReference refSamples = dbRef;
            refSamples.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isExists = false;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Sample sample = data.getValue(Sample.class);
                        if (sample.getUserId().equals(userId)) {
                            isExists = true;
                        }
                    }
                    if (isExists) {
                        currentUser = userId;
                        tvUserId.setText(currentUser);
                        session.createUserSession(currentUser);
                        current = 0;
                        tvSampleWords.setText(wordList.get(current));
                        alert.dismiss();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "This user ID doesn't exist. Please add a new one.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("asif-query", "" + databaseError);
                }
            });
        }
    }

    public String getUniqueIMEIId() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "";
            }
            String imei = telephonyManager.getDeviceId();
            if (imei != null && !imei.isEmpty()) {
                return imei;
            } else {
                return android.os.Build.SERIAL;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
