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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class EditItemActivity extends AppCompatActivity {
    public static final String TODO_INDEX = "TODO_INDEX";
    public static final String SUCCESS_CODE = "SUCCESS_CODE";
    int todo_index = 0;
    EditText edTodo;
    private ArrayList<String> items;

    private SQLiteDatabase db;
    private Cursor cursor;
    private CursorAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        
        items = new ArrayList<String>();
        edTodo = (EditText) findViewById(R.id.etToDo);
        todo_index =  getIntent().getExtras().getInt(TODO_INDEX);
        String todoItem =  readItemsDB(todo_index);
        if (! TextUtils.isEmpty(todoItem)) {
            edTodo.setText(todoItem);
            //Set cursor at the end of item
            edTodo.setSelection(todoItem.length());
            
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_item, menu);
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

    //write updated items to the file
    public void onSaveItem(View v) {
        edTodo = (EditText) findViewById(R.id.etToDo);
        String itemValue = edTodo.getText().toString();
        Log.v("Item value:", itemValue);
        if (itemValue.length() == 0) {
            Toast toast = Toast.makeText(EditItemActivity.this,"Todo Item can't be empty", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            new UpdateItemsDBAsync().execute(todo_index);
        }

    }

    //Read items from the DB
    public String readItemsDB(int id) {
        SQLiteOpenHelper todoDBHelper = new ToDoDatabaseHelper(this);
        String itemValue = new String();
        try {
            db = todoDBHelper.getReadableDatabase();
            cursor = db.query("TODO",
                    new String[]{"_id", "NAME", "STATUS"},
                    "_id = ?",
                    new String[]{Integer.toString(id)} ,
                    null,
                    null,
                    null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    itemValue = cursor.getString(1);

                }

            }
            return itemValue;
      }catch (SQLiteException e) {
            Toast toast = Toast.makeText(this,"TODO Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }
    }

    //Update DB in Asyn mode
    private class UpdateItemsDBAsync extends AsyncTask<Integer, Void, Boolean> {
        ContentValues itemUpdateValues ;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            edTodo = (EditText) findViewById(R.id.etToDo);
            String itemValue = edTodo.getText().toString();
            itemUpdateValues = new ContentValues();
            itemUpdateValues.put("NAME", itemValue);
            itemUpdateValues.put("STATUS", 0);


        }

        @Override
        protected Boolean doInBackground(Integer... itemId) {
            int updateItemId = itemId[0];
            SQLiteOpenHelper todoDatabaseHelper = new ToDoDatabaseHelper(EditItemActivity.this);

            try {
                SQLiteDatabase db = todoDatabaseHelper.getWritableDatabase();
                db.update("TODO", itemUpdateValues, "_id = ?", new String[] { Integer.toString(updateItemId) }  );

                return true;
            } catch (SQLiteException e) {
                return false;
            }
      }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if ( success) {
                //Intent intent = new Intent(EditItemActivity.this, MainActivity.class);
                //startActivity(intent);
                Intent intent = new Intent();
                intent.putExtra(SUCCESS_CODE, "200");
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast toast = Toast.makeText(EditItemActivity.this,"TODO Database unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

}
