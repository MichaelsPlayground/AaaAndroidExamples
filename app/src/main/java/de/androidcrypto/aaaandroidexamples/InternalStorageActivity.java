package de.androidcrypto.aaaandroidexamples;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InternalStorageActivity extends AppCompatActivity {

    private final String TAG = "InternalStorageAct";

    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7;
    TextView tv1;
    com.google.android.material.textfield.TextInputEditText etData, etFilename, etSubfolder, etLog;

    String uniqueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_storage);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        tv1 = findViewById(R.id.tv1);
        etData = findViewById(R.id.etData);
        etFilename = findViewById(R.id.etFilename);
        etSubfolder = findViewById(R.id.etSubfolder);
        etLog = findViewById(R.id.etLog);

        uniqueId = UUID.randomUUID().toString();
        etData.setText(uniqueId);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn1 back to main menu");
                Intent intent = new Intent(InternalStorageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn2 write text file");
                etLog.setText("start to write");
                String data = etData.getText().toString();
                String completeFilename = getSafeFilename(etFilename.getText().toString());
                String subfolder = getSafeFilename(etSubfolder.getText().toString());
                if (TextUtils.isEmpty(data)) {
                    Toast.makeText(InternalStorageActivity.this, "no data to write", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(completeFilename)) {
                    Toast.makeText(InternalStorageActivity.this, "no filename provided", Toast.LENGTH_SHORT).show();
                    return;
                }
                System.out.println("filename: " + completeFilename + " numberOfExtensions: " + getNumberOfExtensions(completeFilename));
                if (getNumberOfExtensions(completeFilename) != 1) {
                    Toast.makeText(InternalStorageActivity.this, "the filename has not 1 extension", Toast.LENGTH_SHORT).show();
                    return;
                }
                //String[] filenameParts = splitFilename(completeFilename);
                //String fileName = getSafeFilename(filenameParts[0]);
                //String fileExtension = getSafeFilename(filenameParts[1]);
                String message = "writing " + completeFilename + " is success: ";
                boolean writeSuccess = writeTextToInternalStorage(completeFilename, subfolder, data);
                etLog.setText(message + writeSuccess);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn3 read text file");

            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn4 write binary file");

            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn5 read binary file");

            }
        });
    }


    private String[] splitFilename(@NonNull String filename) {
        return filename.split(".");
    }

    /**
     * counts the number of file extensions (testing on '.' in the filename)
     * @param filename
     * @return number of extensions
     */
    //public int countChar(String str, char c)
    private int getNumberOfExtensions(@NonNull String filename)
    {
        char c = '.';
        int count = 0;
        for(int i=0; i < filename.length(); i++)
        {    if(filename.charAt(i) == c)
            count++;
        }
        return count;
    }
    private int getNumberOfExtensionsOld(@NonNull String filename) {


        String[] parts = filename.split(".");
        System.out.println("parts: " + parts.length);
        return parts.length - 1;
    }

    /**
     * converts a filename to a Android safe filename
     * @param filename WITHOUT extension
     * @return new filename
     */
    private String getSafeFilename(@NonNull String filename) {
        final int MAX_LENGTH = 127;
        filename = filename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        int end = Math.min(filename.length(),MAX_LENGTH);
        return filename.substring(0,end);
    }

    /**
     * writes a string to the filename in internal storage, If a subfolder is provided the file is created in the subfolder
     * if the file is existing it will be overwritten
     * @param filename
     * @param subfolder
     * @param data
     * @return true if writing is successful and false if not
     */
    private boolean writeTextToInternalStorage(@NonNull String filename, String subfolder, @NonNull String data){
        File file;
        if (TextUtils.isEmpty(subfolder)) {
            file = new File(getFilesDir(), filename);
        } else {
            File subfolderFile = new File(subfolder, filename);
            file = new File(getFilesDir(), subfolderFile.getAbsolutePath());
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.append(data);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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