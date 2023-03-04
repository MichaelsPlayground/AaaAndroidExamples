package de.androidcrypto.aaaandroidexamples;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainAct";

    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    TextView tv1;
    EditText et1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn1");

            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn2 internal storage");
                Intent intent = new Intent(MainActivity.this, InternalStorageActivity.class);
                startActivity(intent);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn3 external storage");
                Intent intent = new Intent(MainActivity.this, ExternalStorageActivity.class);
                startActivity(intent);
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn4 shared preferences");
                Intent intent = new Intent(MainActivity.this, SharedPreferencesActivity.class);
                startActivity(intent);
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn5 encrypted shared preferences");
                Intent intent = new Intent(MainActivity.this, EncryptedSharedPreferencesActivity.class);
                startActivity(intent);
            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn6 NFC");
                Intent intent = new Intent(MainActivity.this, NfcActivity.class);
                startActivity(intent);
            }
        });

        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn7 NFC CreditCard");
                //Intent intent = new Intent(MainActivity.this, NfcCreditCardActivity.class);
                //startActivity(intent);
            }
        });

        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn8 Option menu");
                Intent intent = new Intent(MainActivity.this, OptionMenuActivity.class);
                startActivity(intent);
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