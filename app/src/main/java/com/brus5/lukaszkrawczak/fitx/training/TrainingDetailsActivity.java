package com.brus5.lukaszkrawczak.fitx.training;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.brus5.lukaszkrawczak.fitx.DefaultView;
import com.brus5.lukaszkrawczak.fitx.R;
import com.brus5.lukaszkrawczak.fitx.RestAPI;
import com.brus5.lukaszkrawczak.fitx.SaveSharedPreference;
import com.brus5.lukaszkrawczak.fitx.converter.NameConverter;
import com.brus5.lukaszkrawczak.fitx.converter.TimeStampReplacer;
import com.brus5.lukaszkrawczak.fitx.dto.TrainingDTO;
import com.brus5.lukaszkrawczak.fitx.utils.ActivityView;
import com.brus5.lukaszkrawczak.fitx.utils.DateGenerator;
import com.brus5.lukaszkrawczak.fitx.utils.ImageLoader;
import com.brus5.lukaszkrawczak.fitx.validator.CharacterLimit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TrainingDetailsActivity extends AppCompatActivity implements View.OnClickListener, DefaultView
{
    private static final String TAG = "TrainingDetailsA";
    @SuppressLint("SimpleDateFormat")
    private String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    private String trainingTimeStamp, trainingTarget, previousActivity, newTimeStamp;
    private LinearLayout linearLayout;
    private int trainingID;
    private ImageView imgTrainingL, imgTrainingR;
    private EditText etNotepad;
    private TextView tvName, tvCharsLeft;
    private CheckBox checkBox;
    private TrainingInflater inflater = new TrainingInflater(TrainingDetailsActivity.this);
    private Timer timer;
    private CharacterLimit characterLimit;
    private ScrollView scrollView;
    private ProgressBar pbL, pbR;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_4_details);
        loadInput();
        loadDefaultView();

        getIntentFromPreviousActiity();
        String urlL = RestAPI.URL + "images/exercises/" + trainingTarget + "/" + trainingID + "_2" + ".jpg";
        String urlR = RestAPI.URL + "images/exercises/" + trainingTarget + "/" + trainingID + "_1" + ".jpg";
        new ImageLoader(TrainingDetailsActivity.this, imgTrainingL, pbL, urlL);
        new ImageLoader(TrainingDetailsActivity.this, imgTrainingR, pbR, urlR);

        timer = new Timer(this);
        timer.seekBarTimer();
        getPreviousActivity(previousActivity);
        characterLimit = new CharacterLimit(etNotepad, tvCharsLeft, 280);
        etNotepad.addTextChangedListener(characterLimit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_training_4_details, menu);

        MenuItem item = menu.findItem(R.id.menu_delete_exercise);
        if (previousActivity.equals( TrainingListActivity.class.getSimpleName() ))
        {
            item.setVisible(false);
        }
        else
        {
            item.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_save_exercise:
                if (previousActivity.equals( TrainingActivity.class.getSimpleName() ) && ( inflater.isValid()) && characterLimit.isLimit() )
                {
                    Toast.makeText(this, R.string.training_updated, Toast.LENGTH_SHORT).show();

                    TrainingService updateTraining = new TrainingService();
                    updateTraining.TrainingUpdate(saveDTO(), TrainingDetailsActivity.this);
                    finish();
                }

                else if (previousActivity.equals( TrainingListActivity.class.getSimpleName() ) && (inflater.isValid()) && characterLimit.isLimit() )
                {
                    Toast.makeText(this, R.string.training_inserted, Toast.LENGTH_SHORT).show();

                    TrainingService acceptService = new TrainingService();
                    acceptService.TrainingInsert(saveDTO(), TrainingDetailsActivity.this);
                    finish();
                }
                else
                {
                    DateGenerator cfg = new DateGenerator();
                    cfg.showError(TrainingDetailsActivity.this);
                }
                break;
            case R.id.menu_delete_exercise:
                Toast.makeText(this, R.string.training_deleted, Toast.LENGTH_SHORT).show();

                TrainingService deleteService = new TrainingService();
                deleteService.TrainingDelete(deleteDto(), TrainingDetailsActivity.this);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private TrainingDTO saveDTO()
    {
        Log.i(TAG, "onClick: " + "\nisValid: " + inflater.isValid() + inflater.printResult());

        TrainingDTO dto = new TrainingDTO();
        dto.setTrainingID(trainingID);
        dto.setTrainingDone(setOnCheckedChangeListener());
        dto.setTrainingRestTime(timer.START_TIME_IN_MILLIS);
        dto.setTrainingReps(inflater.getReps());
        dto.setTrainingWeight(inflater.getWeight());
        dto.setUserID(SaveSharedPreference.getUserID(TrainingDetailsActivity.this));
        dto.setTrainingNotepad(etNotepad.getText().toString());
        dto.setTrainingTimeStamp(newTimeStamp);

        Log.i(TAG, "saveDTO: " + dto.toString());

        return dto;
    }

    private TrainingDTO deleteDto()
    {
        TrainingDTO dto = new TrainingDTO();
        dto.setTrainingID(trainingID);
        dto.setUserID(SaveSharedPreference.getUserID(TrainingDetailsActivity.this));
        dto.setTrainingTimeStamp(getTimeStamp());

        Log.i(TAG, "deleteDto: " + dto.toString());

        return dto;
    }

    private String getTimeStamp()
    {
        if (previousActivity.equals(TrainingActivity.class.getSimpleName()))
        {
            return trainingTimeStamp;
        }
        else
        {
            return timeStamp;
        }
    }

    private void onTrainingChangerListener(int i)
    {
        setOnCheckedChangeListener();
        if (i == 0)
        {
            checkBox.setChecked(false);
            checkBox.setText(R.string.not_done);
        }
        else
        {
            checkBox.setChecked(true);
            checkBox.setText(R.string.done);
        }
    }

    private int setOnCheckedChangeListener()
    {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    checkBox.setText(R.string.done);
                }
                else
                {
                    checkBox.setText(R.string.not_done);
                }
            }
        });

        if (checkBox.isChecked())
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    private void getPreviousActivity(String previousActivity)
    {
        if (previousActivity.equals( TrainingActivity.class.getSimpleName() ))
        {
            getUserTrainingDetailsAsynch(TrainingDetailsActivity.this);
            getTrainingNameAsynch(TrainingDetailsActivity.this);
        }
        else if (previousActivity.equals( TrainingListActivity.class.getSimpleName() ))
        {
            getTrainingNameAsynch(TrainingDetailsActivity.this);
            timer.seekBar.setProgress(5);
        }
    }

    public void loadInput()
    {
        checkBox = findViewById(R.id.checkBox);
        linearLayout = findViewById(R.id.container);
        tvCharsLeft = findViewById(R.id.textViewCharsLeft);
        tvName = findViewById(R.id.textViewExerciseName);
        etNotepad = findViewById(R.id.editTextNotepad);
        etNotepad.clearFocus();
        etNotepad.didTouchFocusSelect();

        pbL = findViewById(R.id.progressBarTrainingDetailsL);
        pbR = findViewById(R.id.progressBarTrainingDetailsR);

        imgTrainingL = findViewById(R.id.imageViewTraining);
        imgTrainingR = findViewById(R.id.imageViewTraining1);

        scrollView = findViewById(R.id.scrollViewTrainingActivityDetails);
        scrollView.requestFocus();
    }

    @Override
    public void loadDefaultView()
    {
        ActivityView activityView = new ActivityView(TrainingDetailsActivity.this, getApplicationContext(), this);
        activityView.statusBarColor(R.id.toolbarTrainingExerciseShow);
        activityView.showBackButton();
    }


    @SuppressLint("LongLogTag")
    private void getIntentFromPreviousActiity()
    {
        Intent intent = getIntent();
        trainingID = intent.getIntExtra("trainingID", -1);
        trainingTarget = intent.getStringExtra("trainingTarget");
        trainingTimeStamp = intent.getStringExtra("trainingTimeStamp");
        trainingTimeStamp = timeStampChanger(trainingTimeStamp);
        previousActivity = intent.getStringExtra("previousActivity");

        TimeStampReplacer time = new TimeStampReplacer(DateGenerator.getSelectedDate(), trainingTimeStamp);
        newTimeStamp = time.getNewTimeStamp();
    }

    private String timeStampChanger(String trainingTimeStamp)
    {
        if (trainingTimeStamp == null) return timeStamp;
        else return this.trainingTimeStamp;
    }

    private void trainingSetsGenerator(int seriesNumber)
    {
        for (int i = 0; i < seriesNumber; i++)
        {
            linearLayout.addView(inflater.trainingSetGenerator());
        }
    }

    private void trainingGenerateNextSet()
    {
        linearLayout.addView(inflater.trainingSetGenerator());
    }

    private void getUserTrainingDetailsAsynch(final Context ctx)
    {
        StringRequest strRequest = new StringRequest(Request.Method.POST, RestAPI.URL_SHOW_TRAINING, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.d(TAG, "onResponse: " + jsonObject.toString(1));

                    int done = 0;
                    String rest = "";
                    String reps;
                    String weight;
                    String notepad = "";

                    String exerciseName;

                    NameConverter nameUpperCase = new NameConverter();

                    JSONArray jsonArray = jsonObject.getJSONArray("server_response");

                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject object = jsonArray.getJSONObject(i);
                        exerciseName = object.getString(RestAPI.DB_EXERCISE_NAME);
                        nameUpperCase.setName(exerciseName);
                    }


                    JSONArray trainings_info = jsonObject.getJSONArray("trainings_info");

                    for (int i = 0; i < trainings_info.length(); i++)
                    {
                        JSONObject tr_info = trainings_info.getJSONObject(i);
                        done = tr_info.getInt(RestAPI.DB_EXERCISE_DONE);
                        rest = tr_info.getString(RestAPI.DB_EXERCISE_REST_TIME);
                        reps = tr_info.getString(RestAPI.DB_EXERCISE_REPS);
                        weight = tr_info.getString(RestAPI.DB_EXERCISE_WEIGHT);
                        notepad = tr_info.getString(RestAPI.DB_EXERCISE_NOTEPAD);

                        String mReps = reps.replaceAll("\\p{Punct}", " ");
                        String[] mReps_table = mReps.split("\\s+");

                        inflater.setReps(reps);
                        inflater.setWeight(weight);

                        TrainingDetailsActivity.this.trainingSetsGenerator(mReps_table.length);
                    }


                    tvName.setText(nameUpperCase.getName());
                    TrainingDetailsActivity.this.onTrainingChangerListener(done);
                    etNotepad.setText(notepad);
                    timer.convertSetTime(Integer.valueOf(rest));

                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(ctx, RestAPI.CONNECTION_INTERNET_FAILED, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onErrorResponse: Error" + error);
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                HashMap<String, String> params = new HashMap<>();
                params.put(RestAPI.DB_EXERCISE_DATE,            trainingTimeStamp);
                params.put(RestAPI.DB_USER_ID_NO_PRIMARY_KEY,   String.valueOf(SaveSharedPreference.getUserID(ctx)));
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ctx);
        queue.add(strRequest);
    }

    private void getTrainingNameAsynch(final Context ctx)
    {
        StringRequest strRequest = new StringRequest(Request.Method.POST, RestAPI.URL_SHOW_TRAINING_DETAILS, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.d(TAG, "onResponse: " + jsonObject.toString(1));
                    String name = "";
                    JSONArray server_response = jsonObject.getJSONArray("server_response");
                    NameConverter nameUpperCase = new NameConverter();
                    for (int i = 0; i < server_response.length(); i++)
                    {
                        JSONObject object = server_response.getJSONObject(0);
                        name = object.getString(RestAPI.DB_EXERCISE_NAME);
                    }


                    nameUpperCase.setName(name);
                    tvName.setText(nameUpperCase.getName());
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(ctx, RestAPI.CONNECTION_INTERNET_FAILED, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onErrorResponse: Error" + error);
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                HashMap<String, String> params = new HashMap<>();
                params.put(RestAPI.DB_EXERCISE_ID, String.valueOf(trainingID));
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ctx);
        queue.add(strRequest);
    }

    private void getTrainingDescAsynch(final Context context)
    {
        StringRequest strRequest = new StringRequest(Request.Method.POST, RestAPI.URL_SHOW_TRAINING_DESCRIPTION, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(response);

                    Log.d(TAG, "onResponse: " + jsonObject.toString(1));


                    String description = "";

                    JSONArray server_response = jsonObject.getJSONArray("server_response");

                    for (int i = 0; i < server_response.length(); i++)
                    {
                        JSONObject object = server_response.getJSONObject(0);
                        description = object.getString(RestAPI.DB_EXERCISE_DESCRITION);
                    }
                    TrainingDetailsActivity.this.alertDialog(description);
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(context, RestAPI.CONNECTION_INTERNET_FAILED, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onErrorResponse: Error" + error);
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                HashMap<String, String> params = new HashMap<>();
                params.put(RestAPI.DB_EXERCISE_ID, String.valueOf(trainingID));
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(strRequest);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.buttonAddSeries:
                trainingGenerateNextSet();
                break;
            case R.id.buttonShowDescription:
                getTrainingDescAsynch(TrainingDetailsActivity.this);
                break;
            case R.id.floatingButtonStartPause:
                if (timer.timerRunning)
                {
                    timer.pauseTimer();
                }
                else
                {
                    timer.startTimer();
                }
                break;
            case R.id.buttonResetTimer:
                timer.resetTimer();
                break;
        }
    }

    private void alertDialog(String string)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.description).setPositiveButton("Close", null).setMessage(string).show();
    }

}