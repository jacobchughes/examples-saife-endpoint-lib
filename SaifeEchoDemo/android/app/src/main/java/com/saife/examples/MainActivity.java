package com.saife.examples;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.saife.examples.saifeecho.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends ActionBarActivity {

  private static String LOG_TAG = MainActivity.class.getSimpleName();
  private SaifeManager saifeManager;
  private ExecutorService executorService = Executors.newCachedThreadPool();

  private static final String PASSWORD = "hhhh";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    saifeManager = new SaifeManager(executorService);

    try {
      if (saifeManager.init(PASSWORD, this.getApplicationContext())) {
        Log.i(LOG_TAG, "Successfully keyed. Exiting.");
        finish();
      }
    } catch (Exception e) {
      Log.e(LOG_TAG, "Exception: ", e);
    }

    ToggleButton toggle = (ToggleButton) findViewById(R.id.sessionToggleButton);
    toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          saifeManager.runSessionClient();
        } else {
          saifeManager.stopSessions();
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
