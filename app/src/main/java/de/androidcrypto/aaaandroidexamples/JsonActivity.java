package de.androidcrypto.aaaandroidexamples;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonActivity extends AppCompatActivity {

    // basics: https://medium.com/@nayantala259/android-how-to-read-and-write-parse-data-from-json-file-226f821e957a

    private static final String FILE_NAME = "json.dat";
    private final String TAG = "JsonAct";

    Button btn1, btn2, btn3, btn4, btn5;
    TextView tv1;
    EditText et1, et2, et3, et4, et5, et6, et7, et8, et9, et10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        tv1 = findViewById(R.id.tv1);
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        et4 = findViewById(R.id.et4);
        et5 = findViewById(R.id.et5);
        et6 = findViewById(R.id.et6);
        et7 = findViewById(R.id.et7);
        et8 = findViewById(R.id.et8);
        et9 = findViewById(R.id.et9);
        et10 = findViewById(R.id.et10);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn1 back to main menu");
                Intent intent = new Intent(JsonActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn2 save JSON");

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(et2.getText().toString(), et3.getText().toString());
                    jsonObject.put(et4.getText().toString(), et5.getText().toString());

                    // hier das array
                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObjectA1 = new JSONObject();
                    jsonObjectA1.put(et7.getText().toString(), et8.getText().toString());
                    JSONObject jsonObjectA2 = new JSONObject();
                    jsonObjectA2.put(et9.getText().toString(), et10.getText().toString());
                    jsonArray.put(jsonObjectA1);
                    jsonArray.put(jsonObjectA2);
                    jsonObject.put(et6.getText().toString(), jsonArray);

                    // Convert JsonObject to String Format
                    String userString = jsonObject.toString();
                    System.out.println("userString: " + userString);

                    // Define the File Path and its Name
                    File file = new File(getFilesDir(),FILE_NAME);
                    FileWriter fileWriter = null;
                    try {
                        fileWriter = new FileWriter(file);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                        bufferedWriter.write(userString);
                        bufferedWriter.close();
                        Toast.makeText(JsonActivity.this, "File was written to internal storage", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        //throw new RuntimeException(e);
                        Toast.makeText(JsonActivity.this, "ERROR - File was NOT written to internal storage", Toast.LENGTH_SHORT).show();
                    }

                    /*
                    boolean result = FileUtils.writeTextToInternalStorage(view.getContext(), FILE_NAME, "", userString);
                    if (result) {
                        Toast.makeText(JsonActivity.this, "File was written to internal storage", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(JsonActivity.this, "ERROR - File was NOT written to internal storage", Toast.LENGTH_SHORT).show();
                    }*/
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn3 load Json file");
                File file = new File(getFilesDir(),FILE_NAME);
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null){
                        stringBuilder.append(line).append("\n");
                        line = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    // This responce will have Json Format String
                    String responce = stringBuilder.toString();

                    JSONObject jsonObject  = null;
                    JSONArray jsonArray = null;
                    try {
                        jsonObject = new JSONObject(responce);

                        et3.setText(jsonObject.getString(et2.getText().toString()));
                        et5.setText(jsonObject.getString(et4.getText().toString()));

                        jsonArray = jsonObject.getJSONArray(et6.getText().toString());
                        int jsonArrayLength = jsonArray.length();
                        // for ...
                        JSONObject jsonObjectA1 = jsonArray.getJSONObject(0);
                        JSONObject jsonObjectA2 = jsonArray.getJSONObject(1);
                        et8.setText(jsonObjectA1.getString(et7.getText().toString()));
                        et10.setText(jsonObjectA2.getString(et9.getText().toString()));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    /*
                    //Java Object
                    JavaObject javaObject =
                            new JavaObject(jsonObject.get("name").toString(),
                                    jsonObject.get("enroll_no").toString(),
                                    jsonObject.get("mobile").toString(),
                                    jsonObject.get("address").toString(),
                                    jsonObject.get("branch").toString());

                     */
                } catch (IOException e) {
                    Toast.makeText(JsonActivity.this, "ERROR - File was NOT read from internal storage", Toast.LENGTH_SHORT).show();
                    //throw new RuntimeException(e);
                }


            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn4 clear data");
                et3.setText("");
                et5.setText("");
                et8.setText("");
                et10.setText("");

            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn5");

            }
        });

    }


    /**
     * section for OptionsMenu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        MenuItem mOpenFile = menu.findItem(R.id.action_open_file);
        mOpenFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mOpenFile");
                //Intent i = new Intent(MainActivity.this, AddEntryActivity.class);
                //startActivity(i);
                //readResult.setText("");
                //dumpFileName = "";
                //dumpExportString = "";
                //openFileFromExternalSharedStorage();
                return false;
            }
        });

        MenuItem mPlusTextSize = menu.findItem(R.id.action_plus_text_size);
        mPlusTextSize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mPlusTextSize");
                /*
                //int textSizeInDp = sharedPreferences.getInt(TEXT_SIZE, defaultTextSizeInDp) + 1;
                //readResult.setTextSize(coverPixelToDP(textSizeInDp));
                System.out.println("textSizeInDp: " + textSizeInDp);
                try {
                    sharedPreferences.edit().putInt(TEXT_SIZE, textSizeInDp).apply();
                } catch (Exception e) {
                    writeToUiToast("Error on size storage: " + e.getMessage());
                    return false;
                }

                 */
                return false;
            }
        });

        MenuItem mMinusTextSize = menu.findItem(R.id.action_minus_text_size);
        mMinusTextSize.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mMinusTextSize");
                /*
                int textSizeInDp = sharedPreferences.getInt(TEXT_SIZE, defaultTextSizeInDp) - 1;
                if (textSizeInDp < MINIMUM_TEXT_SIZE_IN_DP) {
                    writeToUiToast("You cannot decrease text size any further");
                    return false;
                }
                readResult.setTextSize(coverPixelToDP(textSizeInDp));
                try {
                    sharedPreferences.edit().putInt(TEXT_SIZE, textSizeInDp).apply();
                } catch (Exception e) {
                    writeToUiToast("Error on size storage: " + e.getMessage());
                    return false;
                }

                 */
                return false;
            }
        });

        MenuItem mExportDumpFile = menu.findItem(R.id.action_export_dump_file);
        mExportDumpFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mExportDumpFile");
                //exportDumpFile();
                return false;
            }
        });

        MenuItem mMailDumpFile = menu.findItem(R.id.action_mail_dump_file);
        mMailDumpFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mMailDumpFile");
                //mailDumpFile();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}