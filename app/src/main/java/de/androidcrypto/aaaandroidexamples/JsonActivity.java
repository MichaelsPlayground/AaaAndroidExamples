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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
import java.util.List;

import de.androidcrypto.aaaandroidexamples.json.ArrayModel;
import de.androidcrypto.aaaandroidexamples.json.JsonModel;

public class JsonActivity extends AppCompatActivity {

    // basics: https://medium.com/@nayantala259/android-how-to-read-and-write-parse-data-from-json-file-226f821e957a

    private static final String FILE_NAME = "json.dat";
    private final String TAG = "JsonAct";

    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
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
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
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

                    // this is the array
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
                    File file = new File(getFilesDir(), FILE_NAME);
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
                File file = new File(getFilesDir(), FILE_NAME);
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        stringBuilder.append(line).append("\n");
                        line = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    // This response will have Json Format String
                    String response = stringBuilder.toString();

                    JSONObject jsonObject = null;
                    JSONArray jsonArray = null;
                    try {
                        jsonObject = new JSONObject(response);

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
                Log.i(TAG, "btn4 save JSON model");

                // lets create the Java json object
                ArrayModel arrayModel1 = new ArrayModel(et7.getText().toString(), et8.getText().toString());
                ArrayModel arrayModel2 = new ArrayModel(et9.getText().toString(), et9.getText().toString());
                ArrayModel[] arrayModels = new ArrayModel[2];
                arrayModels[0] = arrayModel1;
                arrayModels[1] = arrayModel2;
                JsonModel model = new JsonModel(et3.getText().toString(), et5.getText().toString(), et6.getText().toString(), arrayModels);

                System.out.println("model: " + model.toString());

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(et2.getText().toString(), model.getEntry1());
                    jsonObject.put(et4.getText().toString(), model.getEntry2());

                    // this is the array
                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObjectA1 = new JSONObject();
                    jsonObjectA1.put(model.getArray()[0].getEntryName(), model.getArray()[0].getEntryValue());
                    JSONObject jsonObjectA2 = new JSONObject();
                    jsonObjectA2.put(model.getArray()[1].getEntryName(), model.getArray()[1].getEntryValue());
                    jsonArray.put(jsonObjectA1);
                    jsonArray.put(jsonObjectA2);
                    jsonObject.put(model.getArrayName(), jsonArray);

                    // Convert JsonObject to String Format
                    String userString = jsonObject.toString();
                    System.out.println("userString: " + userString);

                    // Define the File Path and its Name
                    File file = new File(getFilesDir(), FILE_NAME);
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

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn5 load Json file model");
                File file = new File(getFilesDir(), FILE_NAME);
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        stringBuilder.append(line).append("\n");
                        line = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    // This response will have Json Format String
                    String response = stringBuilder.toString();

                    JSONObject jsonObject = null;
                    JSONArray jsonArray = null;
                    try {
                        jsonObject = new JSONObject(response);

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

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn6 save JSON model GSON");

                // lets create the Java json object
                ArrayModel arrayModel1 = new ArrayModel(et7.getText().toString(), et8.getText().toString());
                ArrayModel arrayModel2 = new ArrayModel(et9.getText().toString(), et10.getText().toString());
                ArrayModel[] arrayModels = new ArrayModel[2];
                arrayModels[0] = arrayModel1;
                arrayModels[1] = arrayModel2;
                JsonModel model = new JsonModel(et3.getText().toString(), et5.getText().toString(), et6.getText().toString(), arrayModels);

                String modelString = new GsonBuilder().setPrettyPrinting().create().toJson(model, JsonModel.class);
                //String jsonObjectString = new GsonBuilder().create().toJson(model, JsonModel.class); // without pretty print
                System.out.println("jsonObject:\n" + modelString);
                // Define the File Path and its Name
                File file = new File(getFilesDir(), FILE_NAME);
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter(file);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write(modelString);
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
            }
        });

        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn7 load Json file model Json");
                File file = new File(getFilesDir(), FILE_NAME);
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        stringBuilder.append(line).append("\n");
                        line = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    // This response will have Json Format String
                    String response = stringBuilder.toString();

                    Gson gson = new Gson();
                    JsonModel javaObject = gson.fromJson(response, JsonModel.class);

                    et3.setText(javaObject.getEntry1());
                    et5.setText(javaObject.getEntry2());
                    int javaArrayLength = javaObject.getArray().length;
                    // for ...
                    et7.setText(javaObject.getArray()[0].getEntryName());
                    et8.setText(javaObject.getArray()[0].getEntryValue());
                    et9.setText(javaObject.getArray()[1].getEntryName());
                    et10.setText(javaObject.getArray()[0].getEntryName());

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

        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn8 clear");
                et3.setText("");
                et5.setText("");
                et8.setText("");
                et10.setText("");

            }
        });

        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn9");

                // lets create the Java json object
                ArrayModel arrayModel1 = new ArrayModel(et7.getText().toString(), et8.getText().toString());
                ArrayModel arrayModel2 = new ArrayModel(et9.getText().toString(), et9.getText().toString());
                ArrayModel[] arrayModels = new ArrayModel[2];
                arrayModels[0] = arrayModel1;
                arrayModels[1] = arrayModel2;
                JsonModel model = new JsonModel(et3.getText().toString(), et5.getText().toString(), et6.getText().toString(), arrayModels);

                String jsonObjectString = new GsonBuilder().setPrettyPrinting().create().toJson(model, JsonModel.class);
                //String gsonObject = gson.toJson(JsonModel.class).toString();
                System.out.println("jsonObject:\n" + jsonObjectString);
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