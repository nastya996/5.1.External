package com.example.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.sharedpreferences.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String LARGE_TEXT = "large_text";
    List<Map<String, String>> values = new ArrayList<>();
    public static final String APP_PREFERENCES = "app_preferences";
    SharedPreferences mSharedPreferences;
    BaseAdapter listContentAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<String> deletedValues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Button btnAdd = findViewById(R.id.btn_add);
        ListView list = findViewById(R.id.listView_text_1);
        mSharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        boolean hasVisited = mSharedPreferences.getBoolean("hasVisited", false);






        if (!hasVisited) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("hasVisited", true);
            editor.putString(LARGE_TEXT, getString(R.string.large_text));
            editor.apply();
        }

        values = prepareContent();
        if (savedInstanceState != null) {
            deletedValues = savedInstanceState.getStringArrayList("my_key");
        }

        if (deletedValues.size() > 0) {
            for (int k = 0; k < deletedValues.size(); k++) {
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i).values().iterator().next().equals(deletedValues.get(k))) {
                        values.remove(i);
                        break;
                    }
                }
            }
        }

        String[] from = {"text_1", "text_2"};
        int[] to = {R.id.text_1, R.id.text_2};
        listContentAdapter = createAdapter(values, from, to);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deletedValues.add(values.get(position).values().iterator().next());
                values.remove(position);
                listContentAdapter.notifyDataSetChanged();
            }
        });
        list.setAdapter(listContentAdapter);

        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright);
    }


    private BaseAdapter createAdapter(List<Map<String, String>> values, String[] from, int[] to) {
        return new SimpleAdapter(this, values, R.layout.list, from, to);
    }

    private List<Map<String, String>> prepareContent() {

        String[] strings = {""};
        if (mSharedPreferences.contains(LARGE_TEXT)) {
            strings = mSharedPreferences.getString(LARGE_TEXT, "").split("\n\n");
        }

        List<Map<String, String>> list = new ArrayList<>();

        for (int i = 0; i < strings.length; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("text_1", strings[i]);
            map.put("text_2", String.valueOf(strings[i].length()));
            list.add(map);
        }

        return list;
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        values.clear();
        values.addAll(prepareContent());
        listContentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("hello", "onSaveInstanceState() called with: deletedValues = " + deletedValues.size());
        outState.putStringArrayList("my_key", deletedValues);
    }
    private void saveText(String note) {

        try (Writer writer = new FileWriter(getNoteFile())) {
            if (loadText() != null) {
                writer.append(note);
                writer.flush();
            } else {
                writer.write(note);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Не возможно сохранить файл",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String loadText() {
        File noteFile = getNoteFile();
        if (!noteFile.canRead()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (Scanner scanner = new Scanner(new FileReader(noteFile))) {
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine());
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    private File getNoteFolder() {
        return getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
    }

    private File getNoteFile() {
        return new File(getNoteFolder(), "note.txt");
    }

    public void onClick(View view) {
        Toast.makeText(this, "Вы нажали", Toast.LENGTH_SHORT).show();
    }
}