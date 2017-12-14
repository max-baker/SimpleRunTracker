package com.max_baker.simpleruntracker;
/**
 * Written By: Max Baker
 * Last Modified; 12/7/17
 * Displays all past runs, and allows for a mass deletion
 */

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecordsActivity extends AppCompatActivity {
    SQLiteHelper myDb;
    final int ADD_RUN_CODE = 808;
    ArrayAdapter<Run> arrayAdapter;
    DialogClick dialogClick = new DialogClick();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDb = new SQLiteHelper(this);
        if(getIntent().getIntExtra("request",0)==ADD_RUN_CODE){
            Intent intent = getIntent();
            Run myRun = new Run(intent.getIntExtra("minutes",0), intent.getIntExtra("seconds",0), intent.getFloatExtra("distance",0));
            myDb.insertRun(myRun);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        final ListView listView = new ListView(this);
        final ArrayList<Run> myRuns = myDb.selectAllRunsList();
        arrayAdapter = new ArrayAdapter<Run>(this, android.R.layout.simple_list_item_2,
                android.R.id.text1,
                myRuns) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(formatSeconds(myRuns.get(position).getMinutes())+":"+formatSeconds(myRuns.get(position).getSeconds()));
                text2.setText(myRuns.get(position).getDistance()+ " Miles");
                return view;
            }
        };

        listView.setAdapter(arrayAdapter);
        setContentView(listView);
    }

    private String formatSeconds(int secondsParam)
    {
        if(secondsParam>=59){
            return "59";
        }else if(secondsParam<10){
            return "0"+String.valueOf(secondsParam);
        }else{
            return String.valueOf(secondsParam);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.request_menu, menu);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch(menuId){
            case R.id.deleteAll:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(RecordsActivity.this);
                alertBuilder.setTitle("Delete All Runs?");
                alertBuilder.setMessage("All of your past runs will be deleted, this cannot be undone.");
                alertBuilder.setPositiveButton("Yes", dialogClick);
                alertBuilder.setNegativeButton("No", dialogClick);
                alertBuilder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private class DialogClick implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which==DialogInterface.BUTTON_POSITIVE){
                myDb.deleteAllRuns();
                final ListView listView = new ListView(RecordsActivity.this);
                setContentView(listView);
            }
            //Nothing if no
        }
    }
}
