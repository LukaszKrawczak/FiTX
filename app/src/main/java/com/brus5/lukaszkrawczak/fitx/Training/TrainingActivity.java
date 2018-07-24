package com.brus5.lukaszkrawczak.fitx.Training;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.brus5.lukaszkrawczak.fitx.Configuration;

import com.brus5.lukaszkrawczak.fitx.DTO.TrainingDTO;
import com.brus5.lukaszkrawczak.fitx.RestApiNames;
import com.brus5.lukaszkrawczak.fitx.R;
import com.brus5.lukaszkrawczak.fitx.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarListener;

public class TrainingActivity extends AppCompatActivity {


    private static final String TAG = "TrainingActivity";

    HorizontalCalendar horizontalCalendar;

    /* Gettings DB_DATE */
    Configuration cfg = new Configuration();
    String dateInside, dateInsideTextView;
    TextView textViewShowDayTrainingActivity;

    ArrayList<Training> trainingArrayList = new ArrayList<>();
    ListView listViewTrainingActivity;
    TrainingAdapter trainingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        changeStatusBarColor();
        onBackButtonPressed();
        loadInput();

        weekCalendar(cfg.generateEndDay(),cfg.generateNextDay());

        onListViewItemSelected();
    }

    private void loadInput() {
        textViewShowDayTrainingActivity = findViewById(R.id.textViewShowDayTrainingActivity);
        listViewTrainingActivity = findViewById(R.id.listViewTrainingActivity);
    }

    private void weekCalendar(Calendar endDate, Calendar startDate) {
        horizontalCalendar = new HorizontalCalendar.Builder(TrainingActivity.this, R.id.calendarViewTrainingActivity)
                .startDate(startDate.getTime())
                .endDate(endDate.getTime())
                .datesNumberOnScreen(5)
                .dayNameFormat("EE")
                .dayNumberFormat("dd")
//                .textSize(10f, 16f, 14f)
                .showDayName(true)
                .showMonthName(false)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Date date, int position) {
                dateInside = cfg.getSimpleDateDateInside().format(date.getTime());
                dateInsideTextView = cfg.getSimpleDateTextView().format(date.getTime());

                textViewShowDayTrainingActivity.setText(dateInsideTextView);

                trainingArrayList.clear();

                TrainingDTO dto = new TrainingDTO();
                dto.userName = SaveSharedPreference.getUserName(TrainingActivity.this);
                dto.trainingDate = dateInside;
                dto.printStatus();
                loadAsynchTask(dto,TrainingActivity.this);

                Log.i(TAG, "onDateSelected: "+ dateInside +" position: "+position);
            }
        });
    }

    private void onBackButtonPressed() {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void loadAsynchTask(final TrainingDTO dto, final Context ctx){
        StringRequest strRequest = new StringRequest(Request.Method.POST, Configuration.SHOW_TRAINING_URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d(TAG, "onResponse: "+jsonObject.toString(1));
                            JSONArray trainings_info = jsonObject.getJSONArray("trainings_info");
                            JSONArray server_response = jsonObject.getJSONArray("server_response");

                            int exerciseRestTime = 0;
                            int exerciseDone = 0;
                            String exerciseReps = "";
                            String exerciseWeight = "";
                            int excerciseId = 0;
                            String exerciseName = "";
                            String excerciseDate = "";
                            String exerciseTarget = "";

                            if (trainings_info.length() > 0) {
                                for (int i = 0; i < trainings_info.length(); i++) {
                                    JSONObject trainings_infoObj = trainings_info.getJSONObject(i);
                                    exerciseRestTime = trainings_infoObj.getInt(RestApiNames.DB_EXERCISE_REST_TIME);
                                    exerciseDone = trainings_infoObj.getInt(RestApiNames.DB_EXERCISE_DONE);
                                    exerciseReps = trainings_infoObj.getString(RestApiNames.DB_EXERCISE_REPS);
                                    exerciseWeight = trainings_infoObj.getString(RestApiNames.DB_EXERCISE_WEIGHT);
                                    excerciseDate = trainings_infoObj.getString(RestApiNames.DB_EXERCISE_DATE);


                                    JSONObject server_responseObj = server_response.getJSONObject(i);
                                    excerciseId = server_responseObj.getInt(RestApiNames.DB_EXERCISE_ID);
                                    exerciseName = server_responseObj.getString(RestApiNames.DB_EXERCISE_NAME);
                                    exerciseTarget = server_responseObj.getString(RestApiNames.DB_EXERCISE_TARGET);

                                    Training training = new Training(excerciseId,exerciseDone,exerciseName,exerciseRestTime,exerciseWeight,exerciseReps, excerciseDate, exerciseTarget);
                                    trainingArrayList.add(training);
                                }
                            }
                            trainingAdapter = new TrainingAdapter(TrainingActivity.this,R.layout.training_excercise_row,trainingArrayList);
                            listViewTrainingActivity.setAdapter(trainingAdapter);
                            listViewTrainingActivity.invalidate();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
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
                params.put(RestApiNames.DB_USERNAME, dto.userName);
                params.put(RestApiNames.DB_DATE, dto.trainingDate);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ctx);
        queue.add(strRequest);
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(TrainingActivity.this, R.color.color_main_activity_statusbar));
        }
        Toolbar toolbar = findViewById(R.id.toolbarTrainingActivity);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_training_exercise_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_search_exercise:
                Intent intent = new Intent(TrainingActivity.this,TrainingSearchActivity.class);
                TrainingActivity.this.startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onListViewItemSelected() {
        listViewTrainingActivity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView trainingID = view.findViewById(R.id.trainingExcerciseID);
                TextView trainingTimeStamp = view.findViewById(R.id.trainingTimeStamp);
                TextView trainingTarget = view.findViewById(R.id.trainingTarget);

                Intent intent = new Intent(TrainingActivity.this,TrainingDetailsActivity.class);
                intent.putExtra("trainingID", Integer.valueOf(trainingID.getText().toString()));
                intent.putExtra("trainingTimeStamp", trainingTimeStamp.getText().toString());
                intent.putExtra("trainingTarget", trainingTarget.getText().toString());
                intent.putExtra("previousActivity", "TrainingActivity");
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // this backendcall
        trainingAdapter.clear();
        TrainingDTO dto = new TrainingDTO();
        dto.userName = SaveSharedPreference.getUserName(TrainingActivity.this);
        dto.trainingDate = dateInside;
        dto.printStatus();
        loadAsynchTask(dto,TrainingActivity.this);
    }


}
