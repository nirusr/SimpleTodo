package com.codepath.simpletodo;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ListView lvItems;
    private SQLiteDatabase db;
    private Cursor cursor;
    private CursorAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //find item view using resource id
        lvItems = (ListView) findViewById(R.id.lvItems);
        readItemsDB();
        setupListViewListener();

    }

    //Add an item when "Add Item" button is clicked
    public void onAddItem(View v) {
        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
        //Get item text
        String itemText = etNewItem.getText().toString();
        //add item text to list view through adapter
        if ( itemText.length() == 0) {
            Toast toast = Toast.makeText(this,"Empty TODO item can't added", Toast.LENGTH_SHORT);
            toast.show();

        } else {
            new WriteItemsDBAsync().execute("");
        }


    }

    //Remove item from the todo list
    public void setupListViewListener() {
        //Add long click listener
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /*deleteItems((int) id);
                refresh();*/
                new DeleteItemsDBAsync().execute((int)id);
                return true;
            }
        });

       //Add item click listner
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
                intent.putExtra(EditItemActivity.TODO_INDEX, (int) id);
                startActivity(intent);

            }

        };
        lvItems.setOnItemClickListener(itemClickListener);
    }

    //Read items from the DB
    public void readItemsDB() {
        SQLiteOpenHelper todoDBHelper = new ToDoDatabaseHelper(this);

        try {
            db = todoDBHelper.getReadableDatabase();
            cursor = db.query("TODO", new String[]{"_id", "NAME", "STATUS"}, null, null, null, null, null);
            itemsAdapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    cursor,
                    new String[]{"NAME"},
                    new int[]{android.R.id.text1},
                    0
            );
            lvItems.setAdapter(itemsAdapter);
        }catch (SQLiteException e) {
            Toast toast = Toast.makeText(this,"TODO Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //Refresh list after adding an item
    public void refresh() {
        SQLiteOpenHelper todoDBHelper = new ToDoDatabaseHelper(this);
        Cursor newCursor;
        try {
            db = todoDBHelper.getReadableDatabase();
            newCursor = db.query("TODO", new String[]{"_id", "NAME", "STATUS"}, null, null, null, null, null);
            itemsAdapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    cursor,
                    new String[]{"NAME"},
                    new int[]{android.R.id.text1},
                    0
            );

            CursorAdapter adapter = (CursorAdapter) lvItems.getAdapter();
            adapter.changeCursor(newCursor);
            cursor = newCursor;
        }catch (SQLiteException e) {
            Toast toast = Toast.makeText(this,"TODO Database unavailable for REFRESH", Toast.LENGTH_SHORT);
            toast.show();
        }


    }
   @Override
    protected void onRestart() {
        super.onRestart();
        refresh();
    }
   @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();

    }
    // Aysnc database calls
    //Run DB updates in async
    private class WriteItemsDBAsync extends AsyncTask<String, Void, Boolean> {
        ContentValues itemValues ;
        @Override
        protected void onPreExecute() {
            itemValues = new ContentValues();
            super.onPreExecute();
            EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
            //Get item text
            String itemText = etNewItem.getText().toString();
            //add item text to list view through adapter
            if (!TextUtils.isEmpty(itemText)) {
                itemValues.put("NAME", itemText);
                itemValues.put("STATUS", 0);
                Log.v("ITEM NAME:", itemValues.getAsString("NAME"));
                Log.v("STATUS", itemValues.getAsString("STATUS"));
                etNewItem.setText("");
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            SQLiteOpenHelper todoDBHelper = new ToDoDatabaseHelper(MainActivity.this);
            try {
                db = todoDBHelper.getWritableDatabase();
                db.insert("TODO", null, itemValues);
                db.close();
                return true;
            }catch (SQLiteException e) {
                return false;
            }
       }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (! success) {
                Toast toast = Toast.makeText(MainActivity.this,"TODO Database unavailable for UPDATE", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                refresh();
            }
        }
    }

    //Run DB Delete in Async
      private class DeleteItemsDBAsync extends AsyncTask<Integer, Void, Boolean>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... ids) {
            int id = ids[0];
            SQLiteOpenHelper todoDatabaseHelper = new ToDoDatabaseHelper(MainActivity.this);
            SQLiteDatabase db;
            try {
                db = todoDatabaseHelper.getWritableDatabase();
                db.delete("TODO", "_id = ?", new String[] {Integer.toString(id)});
                db.close();
                return true;
            } catch(SQLiteException e) {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                refresh();
            } else {
                Toast toast = Toast.makeText(MainActivity.this,"TODO Database unavailable for DELETE", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}
