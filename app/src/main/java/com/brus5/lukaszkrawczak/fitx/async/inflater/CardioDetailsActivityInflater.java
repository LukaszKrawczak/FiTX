package com.brus5.lukaszkrawczak.fitx.async.inflater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import com.brus5.lukaszkrawczak.fitx.diet.Product;
import com.brus5.lukaszkrawczak.fitx.diet.adapter.DietSearchListAdapter;
import com.brus5.lukaszkrawczak.fitx.training.CardioDetailsActivity;
import com.brus5.lukaszkrawczak.fitx.training.Training;
import com.brus5.lukaszkrawczak.fitx.utils.RestAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@SuppressLint({"LongLogTag", "Registered"})

public class CardioDetailsActivityInflater extends CardioDetailsActivity
{
    private static final String TAG = "DietProductDetailsActivityInflater";

    private Context context;

    private ListView listView;

    private DietSearchListAdapter adapter;

    private ArrayList<Product> list = new ArrayList<>();


    public CardioDetailsActivityInflater(Context context, String response)
    {
        this.context = context;

        dataInflater(response);
    }

    private void dataInflater(String s)
    {
        Log.d(TAG, "dataInflater() called with: s = [" + s + "]");

        try
        {
            JSONObject jsonObject = new JSONObject(s);

            Log.d(TAG, "onResponse: " + jsonObject.toString(1));

            String name;
            double calories;
            int done = 1;

            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
            if (jsonArray.length() > 0)
            {
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject object = jsonArray.getJSONObject(i);

                    name = object.getString(RestAPI.DB_CARDIO_NAME);
                    calories = object.getDouble(RestAPI.DB_CARDIO_CALORIES);
                    //                    done = object.getInt(DB_CARDIO_DONE);

                    //                    Log.e(TAG, "done: " + done);

                    String trainingName = name.substring(0, 1).toUpperCase() + name.substring(1);

                    Training training = new Training(trainingName, calories, done);

                    load(training, context);



                    //                    tvName.setText(trainingName);

                    //                    onTrainingChangerListener(done);

                    //                    timer.setBurned(calories);
                }
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

}
