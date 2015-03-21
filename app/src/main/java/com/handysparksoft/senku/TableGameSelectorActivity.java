package com.handysparksoft.senku;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TableGameSelectorActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_game_selector);

        final ListView listview = (ListView) findViewById(R.id.listViewSelectorGame);

        String[] values = getResources().getStringArray(R.array.table_game); // new String[] { "CROSS" ,"CRUZ", "EIGHT", "EIGHT2", "ARROW", "DIAMOND", "RHOMBUS" };
        int[] icons = {R.drawable.cross, R.drawable.light_cross, R.drawable.eight, R.drawable.big_eight, R.drawable.arrow, R.drawable.diamond, R.drawable.rhombus};

        /*final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }
        final ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list_view_game_selector, list);
        listview.setAdapter(adapter);*/

        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();

        for(int i=0;i<7;i++){
            HashMap<String, String> hm = new HashMap<String,String>();
            hm.put("txtTable", values[i]);
            hm.put("iconTable", Integer.toString(icons[i]) );
            aList.add(hm);
        }

        // Keys used in Hashmap
        String[] from = { "iconTable","txtTable"};

        // Ids of views in listview_layout
        int[] to = { R.id.iconTable, R.id.txtTable};

        // Instantiating an adapter to store each items
        // R.layout.listview_layout defines the layout of each item
        SimpleAdapter sAdapter = new SimpleAdapter(getBaseContext(), aList, R.layout.list_view_game_selector, from, to);
        listview.setAdapter(sAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final  HashMap<String,String> item = (HashMap<String,String>) parent.getItemAtPosition(position);

                Intent returnIntent = new Intent(getApplicationContext(), MainActivity.class);
                returnIntent.putExtra("result",item.get("txtTable"));
                setResult(1, returnIntent);
                finish();

                //view.animate().setDuration(2000).alpha(0);

                /*
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                list.remove(item);
                                adapter.notifyDataSetChanged();
                                view.setAlpha(1);
                            }
                        });
                        */
            }

        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_table_game_selector, menu);
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
