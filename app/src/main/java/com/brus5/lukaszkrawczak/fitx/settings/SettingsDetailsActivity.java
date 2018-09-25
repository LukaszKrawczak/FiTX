package com.brus5.lukaszkrawczak.fitx.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.brus5.lukaszkrawczak.fitx.IDefaultView;
import com.brus5.lukaszkrawczak.fitx.IPreviousActivity;
import com.brus5.lukaszkrawczak.fitx.MainService;
import com.brus5.lukaszkrawczak.fitx.R;
import com.brus5.lukaszkrawczak.fitx.async.HTTPService;
import com.brus5.lukaszkrawczak.fitx.utils.ActivityView;
import com.brus5.lukaszkrawczak.fitx.utils.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.brus5.lukaszkrawczak.fitx.utils.RestAPI.URL_SETTINGS_GET;
import static com.brus5.lukaszkrawczak.fitx.utils.RestAPI.URL_SETTINGS_INSERT;

/**
 * This class shows selected value by user,
 * where user can edit information (override) in database.
 */
public class SettingsDetailsActivity extends AppCompatActivity implements IDefaultView, IPreviousActivity
{
    private String db;
    private String name;
    private String descriptionLong;
    private TextView tvName;
    private TextView tvDescription;
    private double value;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_2_details);
        loadInput();
        loadDefaultView();
        getIntentFromPreviousActiity();

        tvName.setText(name);
        tvDescription.setText(descriptionLong);

//        new Provider(SettingsDetailsActivity.this,SettingsDetailsActivity.this).load(db);

        // Attributing proper information to variables
        int userID = SaveSharedPreference.getUserID(this);

        // Glueing SERVER_URL with variables
        String params = "?id=" + userID + "&table=" + db;


        new MySettings(this).execute(URL_SETTINGS_GET, params);

    }

    @Override
    public void loadInput()
    {
        // Creating workaround
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayoutSettingsDetails);
        // Request focus for non focusing on EditText
        constraintLayout.requestFocus();
        tvName = findViewById(R.id.textViewSettings);
        tvDescription = findViewById(R.id.textViewDescriptionLong);
    }

    @Override
    public void loadDefaultView()
    {
        ActivityView activityView = new ActivityView(SettingsDetailsActivity.this, getApplicationContext(), this);
        activityView.statusBarColor(R.id.toolbaSettingsDetailsActivity);
        activityView.showBackButton();
    }

    /**
     * This method is responsible for showing OptionsMenu
     *
     * @param menu item on ActionBar
     * @return created item
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings_details, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This method is responsible for accepting result entered by user
     *
     * @param item is item on ActionBar menu
     * @return clicked item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_save_setting:
                EditText et = findViewById(R.id.editTextSettings);

                String id = String.valueOf(SaveSharedPreference.getUserID(SettingsDetailsActivity.this));
                Calendar cal = Calendar.getInstance();
                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String date = sdf.format(cal.getTime());

                String RESULT = et.getText().toString();
                String LINK = URL_SETTINGS_INSERT + "?id=" + id + "&date=" + date + "&RESULT=" + RESULT + "&table=" + db;
                MainService s = new MainService(SettingsDetailsActivity.this);
                s.post(LINK);

                Toast.makeText(this, getApplicationContext().getString(R.string.updated) + " " + et.getText().toString(), Toast.LENGTH_SHORT).show();

                finish();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

    }

    /**
     * Getting necessary values from previous activity.
     */
    @Override
    public void getIntentFromPreviousActiity()
    {
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        descriptionLong = intent.getStringExtra("descriptionLong");
        db = intent.getStringExtra("db");
    }

    /**
     * Loading value from AsyncTask
     *
     * @param context contains actual view
     * @param value   result of asynctask
     */
    public void load(Context context, String value)
    {
        EditText et = ((Activity) context).findViewById(R.id.editTextSettings);
        et.setText(value);
        this.value = Double.valueOf(value);
    }


    class MySettings extends HTTPService
    {
        private static final String TAG = "MySettings";
        private Context context;

        public MySettings(Context context)
        {
            super(context);
            this.context = context;
        }

        @Override
        public void onPostExecute(String s)
        {
            super.onPostExecute(s);

            try
            {
                JSONObject obj = new JSONObject(s);
                JSONArray arr = obj.getJSONArray("server_response");
                String val = arr.getJSONObject(0).getString("RESULT");


                EditText et = ((Activity) context).findViewById(R.id.editTextSettings);

                et.setText(val);
                value = Double.valueOf(val);


                Log.d(TAG, "onPostExecute() called with: s = [" + s + "]");
            }
            catch (JSONException e)
            {
                Log.e(TAG, "dataInflater: ", e);
            }


        }
    }


}
