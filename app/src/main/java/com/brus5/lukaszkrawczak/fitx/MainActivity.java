package com.brus5.lukaszkrawczak.fitx;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.brus5.lukaszkrawczak.fitx.DTO.MainDTO;
import com.brus5.lukaszkrawczak.fitx.Diet.DietActivity;
import com.brus5.lukaszkrawczak.fitx.Stats.StatsActivity;
import com.brus5.lukaszkrawczak.fitx.Training.TrainingActivity;
import com.brus5.lukaszkrawczak.fitx.Training.TrainingInflater;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DefaultView
{
    private static final String TAG = "MainActivity";
    private Button btnDiet, btnTraining, btnSettings, btnStats;
    private HorizontalCalendar calendar;
    private Configuration cfg = new Configuration();
    private ArrayList<Main> list = new ArrayList<>();
    private ListView listView;
    private MainAdapter adapter;
    private Main main;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cfg.changeStatusBarColor(this, getApplicationContext(), R.id.toolbarMainActivity,this);
        loadInput();
        weekCalendar(cfg.oldestDay(), cfg.newestDay());
        main = new Main();
    }

    private void weekCalendar(Calendar endDate, Calendar startDate)
    {
        calendar = new HorizontalCalendar.Builder(MainActivity.this, R.id.calendarViewMainActivity)
                .startDate(startDate.getTime())
                .endDate(endDate.getTime())
                .datesNumberOnScreen(5)
                .dayNameFormat("EE")
                .dayNumberFormat("dd")
                .showDayName(true)
                .showMonthName(false)
                .build();

        calendar.setCalendarListener(new HorizontalCalendarListener()
        {
            @Override
            public void onDateSelected(Date date, int position)
            {
                Configuration.setDate(cfg.getDateFormat().format(date.getTime()));
                asynchPreparator();
            }
        });
    }

    private void asynchPreparator()
    {
        list.clear();
        MainDTO dto = new MainDTO();
        dto.userID = SaveSharedPreference.getUserID(MainActivity.this);
        dto.date = Configuration.getDate();

        loadAsynchTask(dto, MainActivity.this);
    }

    public void loadInput()
    {
        listView = findViewById(R.id.listViewMain);
        btnDiet = findViewById(R.id.buttonDiet);
        btnDiet.setOnClickListener(this);
        btnTraining = findViewById(R.id.buttonTraining);
        btnTraining.setOnClickListener(this);
        btnSettings = findViewById(R.id.buttonSettings);
        btnSettings.setOnClickListener(this);
        btnStats = findViewById(R.id.buttonStats);
        btnStats.setOnClickListener(this);
    }

    public void loadAsynchTask(final MainDTO dto, final Context context)
    {
        StringRequest strRequest = new StringRequest(Request.Method.POST, RestAPI.URL_MAIN_DIET, new Response.Listener<String>() {
            @Override
            public void onResponse(String globalResponse)
            {


        try
            {
                int kcal = 0;
                int kcalLimit;

                JSONObject jsonObject = new JSONObject(globalResponse);

                Log.d(TAG, "onResponse: " + jsonObject.toString(1));

                JSONArray response = jsonObject.getJSONArray("response");

                JSONObject kcalObj = response.getJSONObject(0);
                JSONObject kcalLimitObj = response.getJSONObject(1);

                if (response.length() > 0)
                {
                    for (int i = 0; i < response.length(); i++)
                    {
                        kcal = kcalObj.getInt("kcal");
                        kcalLimit = kcalLimitObj.getInt("kcal_limit");

                        main = new Main(kcal, kcalLimit, 1);

                        adapter = new MainAdapter(MainActivity.this, R.layout.row_main_diet, list);
                    }

                    if (kcal > 0)
                    {
                        list.add(main);
                    }

                }
            } catch (JSONException e)
            {
                e.printStackTrace();
            }





            try
            {
                JSONObject jsonObject1 = new JSONObject(globalResponse);
                JSONArray response = jsonObject1.getJSONArray("response");

                JSONObject repetitionsObj = response.getJSONObject(2);
                JSONObject liftedObj = response.getJSONObject(3);
                JSONObject restObj = response.getJSONObject(4);

                String weight = "0";
                String rest;
                String reps;

                TrainingInflater inflater = new TrainingInflater(MainActivity.this);

                for (int i = 0; i < response.length(); i++)
                {
                    reps = repetitionsObj.getString("repetitions");
                    weight = liftedObj.getString("lifted");
                    rest = restObj.getString("rest");

                    inflater.setWeight(weight);
                    inflater.setReps(reps);

                    adapter = new MainAdapter(MainActivity.this, R.layout.row_main_diet, list);

                    main = new Main(rest, reps, weight, 2);
                }

                if (!weight.equals("0"))
                {
                    list.add(main);
                }

            } catch (JSONException e)
            {
                e.printStackTrace();
            }





            try
            {
                JSONObject jsonObject1 = new JSONObject(globalResponse);
                JSONArray response = jsonObject1.getJSONArray("response");

                JSONObject cardio_counted = response.getJSONObject(5);
                JSONObject cardio_time = response.getJSONObject(6);


                double kcalBurned = 0;
                int time;

                for (int i = 0; i < response.length(); i++)
                {
                    kcalBurned = cardio_counted.getDouble("cardio_counted");
                    time = cardio_time.getInt("cardio_time");

                    adapter = new MainAdapter(MainActivity.this, R.layout.row_main_diet, list);

                    main = new Main(kcalBurned, time, 3);
                }

                if (kcalBurned != 0)
                {
                    list.add(main);
                }

            } catch (JSONException e)
            {
                e.printStackTrace();
            }

            listView.setDividerHeight(0);
            listView.setAdapter(adapter);
            // TODO: 01.08.2018 work here

            listView.invalidate();
            // TODO: 31.07.2018 zrobic if listview is empty to pokazuj wiadomość "Brak danych z dnia: xx.XX.xxxx"

        }}, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(context, RestAPI.CONNECTION_INTERNET_FAILED, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onErrorResponse: Error" + error);
            }
        }
        )

        {
            @Override
            protected Map<String, String> getParams()
            {
                HashMap<String, String> params = new HashMap<>();
                params.put(RestAPI.DB_USER_ID_NO_PRIMARY_KEY, String.valueOf(SaveSharedPreference.getUserID(context)));
                params.put(RestAPI.DB_DATE, dto.date);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(strRequest);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.buttonDiet:
                runNextActivity(MainActivity.this, DietActivity.class);
                break;
            case R.id.buttonTraining:
                runNextActivity(MainActivity.this, TrainingActivity.class);
                break;
            case R.id.buttonStats:
                runNextActivity(MainActivity.this, StatsActivity.class);
                break;
            case R.id.buttonSettings:
                runNextActivity(MainActivity.this, SettingsActiviy.class);
                break;
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        asynchPreparator();
    }

    public void runNextActivity(Context packageContext, Class<?> cls)
    {
        Intent intent = new Intent(packageContext, cls);
        MainActivity.this.startActivity(intent);
    }

    public void onClickDiet(View view)
    {
        runNextActivity(MainActivity.this,DietActivity.class);
    }

    public void onClickTraining(View view)
    {
        runNextActivity(MainActivity.this,TrainingActivity.class);
    }

    public void onClickCardio(View view)
    {
        runNextActivity(MainActivity.this,TrainingActivity.class);
    }
}
