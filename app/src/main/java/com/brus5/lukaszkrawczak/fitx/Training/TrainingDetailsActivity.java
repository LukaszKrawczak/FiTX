package com.brus5.lukaszkrawczak.fitx.Training;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.brus5.lukaszkrawczak.fitx.Configuration;
import com.brus5.lukaszkrawczak.fitx.R;
import com.brus5.lukaszkrawczak.fitx.RestApiNames;
import com.brus5.lukaszkrawczak.fitx.SaveSharedPreference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingDetailsActivity extends AppCompatActivity {
    private static final String TAG = "TrainingDetailsActivity";
    LinearLayout container;
//    RelativeLayout relativeLayoutTrainingDetails;
    Button buttonTrainingDetailsAdd;
    int trainingID;
    String trainingTimeStamp, trainingTarget;
    ImageView trainingImageView,trainingImageView2;
    EditText editTextTrainingExerciseShow;
    TextView textViewExerciseName;
    Map<Integer,String> mapWeight = new HashMap<>();
    Map<Integer,String> mapReps = new HashMap<>();
    List arrayReps = new ArrayList();
    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_details);
        changeStatusBarColor();
        onBackButtonPressed();
        loadInput();
        getIntentFromPreviousActiity();

        String url = "http://justfitx.xyz/images/exercises/" + trainingTarget + "/" + trainingID + "_1" + ".jpg";
        String url2 = "http://justfitx.xyz/images/exercises/" + trainingTarget + "/" + trainingID + "_2" + ".jpg";

        Log.i(TAG, "onCreate: " + url);
        Log.i(TAG, "onCreate: " + url2);

        loadImageFromUrl(url);
        loadImageFromUrl2(url2);




        asynchTask(TrainingDetailsActivity.this);

    }

    private void loadInput() {
        buttonTrainingDetailsAdd = findViewById(R.id.buttonTrainingDetailsAdd);

        container = findViewById(R.id.container);





        textViewExerciseName = findViewById(R.id.textViewExerciseName);
        editTextTrainingExerciseShow = findViewById(R.id.editTextTrainingExerciseShow);
        trainingImageView = findViewById(R.id.trainingImageView);
        trainingImageView2 = findViewById(R.id.trainingImageView2);
    }

    @SuppressLint("LongLogTag")
    private void getIntentFromPreviousActiity() {
        Intent intent = getIntent();
        trainingID = intent.getIntExtra("trainingID",-1);
        trainingTimeStamp = intent.getStringExtra("trainingTimeStamp");
        trainingTarget = intent.getStringExtra("trainingTarget");

        Log.e(TAG, "onCreate: "+trainingID);
        Log.e(TAG, "onCreate: "+trainingTimeStamp);
        Log.e(TAG, "onCreate: "+trainingTarget);


    }

    private void loadImageFromUrl(String url) {
        Picasso.with(TrainingDetailsActivity.this).load(url).placeholder(null)
                .error(R.mipmap.ic_launcher_round)
                .into(trainingImageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
//                        progressBarDietProductShowActivity.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError() {
//                        progressBarDietProductShowActivity.setVisibility(View.VISIBLE);
                        isError();
                    }
                });
    }
    private void loadImageFromUrl2(String url) {
        Picasso.with(TrainingDetailsActivity.this).load(url).placeholder(null)
                .error(R.mipmap.ic_launcher_round)
                .into(trainingImageView2, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
//                        progressBarDietProductShowActivity.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError() {
//                        progressBarDietProductShowActivity.setVisibility(View.VISIBLE);
                        isError();
                    }
                });
    }

    private void isError() {
        Toast.makeText(TrainingDetailsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(TrainingDetailsActivity.this, R.color.color_main_activity_statusbar));
        }
        Toolbar toolbar = findViewById(R.id.toolbarTrainingExerciseShow);
        setSupportActionBar(toolbar);
    }
    private void onBackButtonPressed() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_training_exercise_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("LongLogTag")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_save_exercise:
                saveExerciseToDB();
                editTextTrainingExerciseShow.getText().toString();
                Log.e(TAG, "save button pressed: "+editTextTrainingExerciseShow.getText().toString());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveExerciseToDB() {
    }

    private void buttonTrigger(int a) {

        buttonTrainingDetailsAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.training_details_add_row, null);

                EditText editTextTrainingRowReps = addView.findViewById(R.id.editTextTrainingRowReps);

// FIXME: 17.07.2018 workigng hereeeee!!!
                Button buttonRemove = addView.findViewById(R.id.buttonTrainingRowRemove);
                buttonRemove.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        ((LinearLayout)addView.getParent()).removeView(addView);

                        Log.i(TAG, "onClick: " + ((EditText)addView.findViewById(R.id.editTextTrainingRowReps)).getText().toString() + ((EditText)addView.findViewById(R.id.editTextTrainingRowWeight)).getText().toString());
                    }});
                Button buttonChecker = addView.findViewById(R.id.buttonChecker);
                buttonChecker.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: reps: " + ((EditText)addView.findViewById(R.id.editTextTrainingRowReps)).getText().toString() + " weight: " + ((EditText)addView.findViewById(R.id.editTextTrainingRowWeight)).getText().toString());
                    }});


                container.addView(addView);
            }
        });

        for (int i = 0; i < a; i++){
            buttonTrainingDetailsAdd.performClick();
        }
    }

    private void asynchTask(final Context ctx){
        StringRequest strRequest = new StringRequest(Request.Method.POST, Configuration.SHOW_TRAINING_URL,
                new Response.Listener<String>()
                {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onResponse(String response)
                    {
                        try {
                            /* Getting DietRatio from MySQL */
                            JSONObject jsonObject = new JSONObject(response);

                            Log.d(TAG, "onResponse: "+jsonObject.toString(1));

                            int trainingID;
                            int done;
                            int rest;
                            String reps = "";
                            String weight = "";
                            String timeStamp;
                            String notepad = "";

                            String exerciseName;
                            String trainingName = "";

                            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    exerciseName = object.getString(RestApiNames.DB_EXERCISE_NAME);


                                    trainingName = exerciseName.substring(0,1).toUpperCase() + exerciseName.substring(1);
                                }
                            }

                            JSONArray trainings_info_array = jsonObject.getJSONArray("trainings_info");
                            if (trainings_info_array.length() > 0) {
                                for (int i = 0; i < trainings_info_array.length(); i++) {
                                    JSONObject object = trainings_info_array.getJSONObject(i);
                                    reps = object.getString(RestApiNames.DB_EXERCISE_REPS);
                                    weight = object.getString(RestApiNames.DB_EXERCISE_WEIGHT);
                                    notepad = object.getString(RestApiNames.DB_EXERCISE_NOTEPAD);

                                    String mReps = reps.replaceAll("\\p{Punct}"," ");
                                    String[] mReps_table = mReps.split("\\s+");

                                    String mWeight = weight.replaceAll("\\p{Punct}"," ");
                                    String[] mWeight_table = mWeight.split("\\s+");




                                    buttonTrigger(mReps_table.length);

                                    for (int a = 0; a < mReps_table.length; a++){
                                        mapWeight.put(a,mWeight_table[a]);
                                        mapReps.put(a,mReps_table[a]);
                                    }

                                    editTextTrainingExerciseShow.setText(notepad);

                                    Log.i(TAG, "mReps_table: " + Arrays.toString(mReps_table));
                                    Log.i(TAG, "mWeight_table: " + Arrays.toString(mWeight_table));

                                    for (String s : mapWeight.values()){
                                        Log.i(TAG, "onResponse: " + s);
                                    }
                                    for (String s : mapReps.values()){
                                        Log.i(TAG, "onResponse: " + s);
                                    }

                                }
                            }
                            /* End */

                            textViewExerciseName.setText(trainingName);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(ctx, Configuration.CONNECTION_INTERNET_FAILED, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onErrorResponse: Error"+error);
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                HashMap<String,String> params = new HashMap<>();
                params.put(RestApiNames.DB_EXERCISE_ID, String.valueOf(trainingID));
                params.put(RestApiNames.DB_EXERCISE_DATE, trainingTimeStamp);
                params.put(RestApiNames.DB_USERNAME, SaveSharedPreference.getUserName(ctx));
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ctx);
        queue.add(strRequest);
    }

}