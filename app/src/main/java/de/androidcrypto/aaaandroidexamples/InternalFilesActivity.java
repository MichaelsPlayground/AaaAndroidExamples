package de.androidcrypto.aaaandroidexamples;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class InternalFilesActivity extends AppCompatActivity {

    private final String TAG = "InternalFilesAct";

    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7;
    TextView tv1, tv2;
    EditText et1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_files);

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
        tv2 = findViewById(R.id.tv2);
        et1 = findViewById(R.id.et1);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn1 back to main menu");
                Intent intent = new Intent(InternalFilesActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn2 Load a file from resources folder");

                tv2.setText("");
                String filesName = "data.txt";
                String resourcesTextFileRead = null;
                // minimum API 24
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    try {
                        resourcesTextFileRead = getResourceFileAsString(filesName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                String outputString = "read with getResourceFileAsString:\n" + resourcesTextFileRead;
                tv2.setText(outputString);

                // see: https://mkyong.com/java/java-read-a-file-from-resources-folder/
                /*
                By default, build tools like Maven, Gradle, or common Java practice will copy all files
                from src/main/resources to the root of target/classes or build/classes. So, when we
                try to read a file from src/main/resources, we read the file from the root of the
                project classpath.
                 */
                // the stream holding the file content
                //InputStream is = getClass().getClassLoader().getResourceAsStream("file.txt");

                // for static access, uses the class name directly
                // InputStream isStatic = InternalFilesActivity.class.getClassLoader().getResourceAsStream("file.txt");

            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn3 Load a file from resources folder byte array");
                tv2.setText("");
                String filesName = "data.txt";
                String resourcesTextFileRead = null;
                resourcesTextFileRead = new String(getResourceFileAsByteArray(filesName), StandardCharsets.UTF_8);
                String outputString = "read byte with getResourceFileAsByteArray:\n" + resourcesTextFileRead;
                tv2.setText(outputString);
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn4");

            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn5");

            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn 6");

            }
        });

        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn 7");

            }
        });
    }

    /**
     * Reads given resource file as a string.
     * min. API 24
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getResourceFileAsString(String fileName) throws IOException {
        //ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        ClassLoader classLoader = getClass().getClassLoader();
        //try (InputStream is = classLoader.getResourceAsStream(fileName)) {
        if (classLoader != null) {
            try (InputStream is = classLoader.getResourceAsStream(fileName)) {
                if (is == null) return null;
                try (InputStreamReader isr = new InputStreamReader(is);
                     BufferedReader reader = new BufferedReader(isr)) {
                    return reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
            }
        }
        return null;
    }

    private byte[] getResourceFileAsByteArray(String fileName) {
        // https://stackoverflow.com/a/38912527/8166854
        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader != null) {
            try (InputStream is = classLoader.getResourceAsStream(fileName)) {
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                //Create buffer
                byte[] buffer = new byte[4096];
                if (is == null) return null;
                for (;;) {
                    int nread = is.read(buffer);
                    if (nread <= 0) {
                        break;
                    }
                    byteArray.write(buffer, 0, nread);
                }
                return byteArray.toByteArray();
            } catch (IOException e) {
                //throw new RuntimeException(e);
                return null;
            }
        }
        return null;
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