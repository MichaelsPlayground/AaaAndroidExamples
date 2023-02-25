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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class InternalStorageActivity extends AppCompatActivity {


    private final String TAG = "InternalStorageAct";

    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
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
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
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
                if (getNumberOfExtensions(completeFilename) != 1) {
                    Toast.makeText(InternalStorageActivity.this, "the filename has not 1 extension", Toast.LENGTH_SHORT).show();
                    return;
                }
                String message = "writing " + completeFilename + " is success: ";
                boolean writeSuccess = writeTextToInternalStorage(completeFilename, subfolder, data);
                etLog.setText(message + writeSuccess);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn3 read text file");
                etData.setText("");
                String completeFilename = getSafeFilename(etFilename.getText().toString());
                String subfolder = getSafeFilename(etSubfolder.getText().toString());
                if (TextUtils.isEmpty(completeFilename)) {
                    Toast.makeText(InternalStorageActivity.this, "no filename provided", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getNumberOfExtensions(completeFilename) != 1) {
                    Toast.makeText(InternalStorageActivity.this, "the filename has not 1 extension", Toast.LENGTH_SHORT).show();
                    return;
                }
                String readData = readStringFileFromInternalStorage(completeFilename, subfolder);
                if (TextUtils.isEmpty(readData)) {
                    etData.setText("no data to read or file is not existing");
                    String message = "reading " + completeFilename + " is success: false";
                    etLog.setText(message);
                    Toast.makeText(InternalStorageActivity.this, "no data to read or file is not existing", Toast.LENGTH_SHORT).show();
                    return;
                }
                etData.setText(readData);
                String message = "reading " + completeFilename + " is success";
                etLog.setText(message);
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn4 write binary file");
                etLog.setText("start to write");
                String dataString = etData.getText().toString();
                String completeFilename = getSafeFilename(etFilename.getText().toString());
                String subfolder = getSafeFilename(etSubfolder.getText().toString());
                if (TextUtils.isEmpty(dataString)) {
                    Toast.makeText(InternalStorageActivity.this, "no data to write", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(completeFilename)) {
                    Toast.makeText(InternalStorageActivity.this, "no filename provided", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getNumberOfExtensions(completeFilename) != 1) {
                    Toast.makeText(InternalStorageActivity.this, "the filename has not 1 extension", Toast.LENGTH_SHORT).show();
                    return;
                }
                String message = "writing " + completeFilename + " is success: ";
                byte[] data = dataString.getBytes(StandardCharsets.UTF_8);
                boolean writeSuccess = writeBinaryDataToInternalStorage(completeFilename, subfolder, data);
                etLog.setText(message + writeSuccess);
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn5 read binary file");
                etLog.setText("start to read");
                etData.setText("");
                String completeFilename = getSafeFilename(etFilename.getText().toString());
                String subfolder = getSafeFilename(etSubfolder.getText().toString());
                if (TextUtils.isEmpty(completeFilename)) {
                    Toast.makeText(InternalStorageActivity.this, "no filename provided", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getNumberOfExtensions(completeFilename) != 1) {
                    Toast.makeText(InternalStorageActivity.this, "the filename has not 1 extension", Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] readData = readBinaryDataFromInternalStorage(completeFilename, subfolder);
                if (readData == null) {
                    etData.setText("no data to read or file is not existing");
                    String message = "reading " + completeFilename + " is success: false";
                    etLog.setText(message);
                    Toast.makeText(InternalStorageActivity.this, "no data to read or file is not existing", Toast.LENGTH_SHORT).show();
                    return;
                }
                etData.setText(new String(readData, StandardCharsets.UTF_8));
                String message = "reading " + completeFilename + " is success";
                etLog.setText(message);
            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn 6 file exists");
                etLog.setText("start fileExists");
                String completeFilename = getSafeFilename(etFilename.getText().toString());
                String subfolder = getSafeFilename(etSubfolder.getText().toString());
                if (TextUtils.isEmpty(completeFilename)) {
                    Toast.makeText(InternalStorageActivity.this, "no filename provided", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getNumberOfExtensions(completeFilename) != 1) {
                    Toast.makeText(InternalStorageActivity.this, "the filename has not 1 extension", Toast.LENGTH_SHORT).show();
                    return;
                }
                String filenameWithSubfolder = "";
                if (TextUtils.isEmpty(subfolder)) {
                    filenameWithSubfolder = completeFilename;
                } else {
                    filenameWithSubfolder = subfolder + File.separator + completeFilename;
                }
                boolean fileExists = fileExistsInInternalStorage(filenameWithSubfolder);
                String message = "file " + filenameWithSubfolder + " is existing: ";
                etLog.setText(message + fileExists);
            }
        });

        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn 7 delete file");
                etLog.setText("start delete file");
                String completeFilename = getSafeFilename(etFilename.getText().toString());
                String subfolder = getSafeFilename(etSubfolder.getText().toString());
                if (TextUtils.isEmpty(completeFilename)) {
                    Toast.makeText(InternalStorageActivity.this, "no filename provided", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getNumberOfExtensions(completeFilename) != 1) {
                    Toast.makeText(InternalStorageActivity.this, "the filename has not 1 extension", Toast.LENGTH_SHORT).show();
                    return;
                }

                String filenameWithSubfolder = concatenateFilenameWithSubfolder(completeFilename, subfolder);
                // delete file only if file is existing
                boolean fileExists = fileExistsInInternalStorage(filenameWithSubfolder);
                if (!fileExists) {
                    String message = "file " + filenameWithSubfolder + " is not existing, deletion not possible";
                    etLog.setText(message);
                    return;
                }
                boolean deletionSuccess = fileDeleteInInternalStorage(filenameWithSubfolder);
                String message = "file " + filenameWithSubfolder + " was deleted: ";
                etLog.setText(message + deletionSuccess);
            }
        });

        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn8 list files");
                etLog.setText("start list files");
                etData.setText("");
                etFilename.setText("");
                String subfolder = getSafeFilename(etSubfolder.getText().toString());
                ArrayList<String> fileList = listFilesInInternalStorage(subfolder);
                if (fileList.isEmpty()) {
                    etLog.setText("there are no files in (sub-) folder");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < fileList.size(); i++) {
                    sb.append(fileList.get(i)).append("\n");
                }
                etFilename.setText("");
                etData.setText("found " + fileList.size() + " files:\n" + sb.toString());
                etLog.setText("found " + fileList.size() + " files");
            }
        });

        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn9 list folder");
                etLog.setText("start list folder");
                etData.setText("");
                etFilename.setText("");
                String subfolder = getSafeFilename(etSubfolder.getText().toString());
                ArrayList<String> folderList = listFolderInInternalStorage(subfolder);
                if (folderList.isEmpty()) {
                    etLog.setText("there are no folder in (sub-) folder");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < folderList.size(); i++) {
                    sb.append(folderList.get(i)).append("\n");
                }
                etFilename.setText("");
                etData.setText("found " + folderList.size() + " folder:\n" + sb.toString());
                etLog.setText("found " + folderList.size() + " folder");
            }
        });
    }


    /**
     * concatenates the filename with a subfolder
     * @param filename
     * @param subfolder
     * @return a String subfolder | File.separator | filename
     */
    public String concatenateFilenameWithSubfolder(@NonNull String filename, String subfolder) {
        if (TextUtils.isEmpty(subfolder)) {
            return filename;
        } else {
            return subfolder + File.separator + filename;
        }
    }

    /**
     * splits a complete filename in the filename [0] and extension [1]
     * @param filename with extension
     * @return a String array with filename [0] and extension [1]
     */
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
     * read a file from internal storage and return the content as UTF-8 encoded string
     * @param filename
     * @param subfolder
     * @return the content as String
     */
    public String readStringFileFromInternalStorage(@NonNull String filename, String subfolder) {
        File file;
        if (TextUtils.isEmpty(subfolder)) {
            file = new File(getFilesDir(), filename);
        } else {
            File subfolderFile = new File(getFilesDir(), subfolder);
            if (!subfolderFile.exists()) {
                subfolderFile.mkdirs();
            }
            file = new File(subfolderFile, filename);
        }
        String completeFilename = concatenateFilenameWithSubfolder(filename, subfolder);
        if (!fileExistsInInternalStorage(completeFilename)) {
            return "";
        }
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
            in.close();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
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
            File subfolderFile = new File(getFilesDir(), subfolder);
            if (!subfolderFile.exists()) {
                subfolderFile.mkdirs();
            }
            file = new File(subfolderFile, filename);
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
     * read a file from internal storage and return the content as byte array
     * @param filename
     * @param subfolder
     * @return the content as String
     */
    public byte[] readBinaryDataFromInternalStorage(@NonNull String filename, String subfolder) {
        File file;
        if (TextUtils.isEmpty(subfolder)) {
            file = new File(getFilesDir(), filename);
        } else {
            File subfolderFile = new File(getFilesDir(), subfolder);
            if (!subfolderFile.exists()) {
                subfolderFile.mkdirs();
            }
            file = new File(subfolderFile, filename);
        }
        String completeFilename = concatenateFilenameWithSubfolder(filename, subfolder);
        if (!fileExistsInInternalStorage(completeFilename)) {
            return null;
        }
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
            in.close();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * writes a byte array to the filename in internal storage, If a subfolder is provided the file is created in the subfolder
     * if the file is existing it will be overwritten
     * @param filename
     * @param subfolder
     * @param data
     * @return true if writing is successful and false if not
     */
    private boolean writeBinaryDataToInternalStorage(@NonNull String filename, String subfolder, @NonNull byte[] data){
        final int BUFFER_SIZE = 8096;
        File file;
        if (TextUtils.isEmpty(subfolder)) {
            file = new File(getFilesDir(), filename);
        } else {
            File subfolderFile = new File(getFilesDir(), subfolder);
            if (!subfolderFile.exists()) {
                subfolderFile.mkdirs();
            }
            file = new File(subfolderFile, filename);
        }
        try (
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                FileOutputStream out = new FileOutputStream(file))
        {
            byte[] buffer = new byte[BUFFER_SIZE];
            int nread;
            while ((nread = in.read(buffer)) > 0) {
                out.write(buffer, 0, nread);
            }
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "ERROR on encryption: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * checks if a file in internal storage is existing
     * @param completeFilename with all subfolders
     * @return true if file exists and false if not
     */
    private boolean fileExistsInInternalStorage(String completeFilename) {
        File file = new File(getFilesDir(), completeFilename);
        return file.exists();
    }

    /**
     * deletes a file in internal storage
     * @param completeFilename with all subfolders
     * @return true if deletion was successful
     */
    private boolean fileDeleteInInternalStorage(String completeFilename) {
        File file = new File(getFilesDir(), completeFilename);
        return file.delete();
    }

    /**
     * list all files in the (sub-) folder of internal storage
     * @param subfolder
     * @return ArrayList<String> with filenames
     */
    public ArrayList<String> listFilesInInternalStorage( String subfolder) {
        File file;
        if (TextUtils.isEmpty(subfolder)) {
            file = new File(getFilesDir(), "");
        } else {
            file = new File(getFilesDir(), subfolder);
            /*
            if (!subfolderFile.exists()) {
                subfolderFile.mkdirs();
            }

             */

        }
        File[] files = file.listFiles();
        if (files == null) return null;
        ArrayList<String> fileNames = new ArrayList<>();
        for (File value : files) {
            if (value.isFile()) {
                fileNames.add(value.getName());
            }
        }
        return fileNames;
    }

    /**
     * list all folder in the (sub-) folder of internal storage
     * @param subfolder
     * @return ArrayList<String> with folder names
     */
    public ArrayList<String> listFolderInInternalStorage(String subfolder) {
        File file;
        if (TextUtils.isEmpty(subfolder)) {
            file = new File(getFilesDir(), "");
        } else {
            file = new File(getFilesDir(), subfolder);
        }
        File[] files = file.listFiles();
        if (files == null) return null;
        ArrayList<String> folderNames = new ArrayList<>();
        for (File value : files) {
            if (!value.isFile()) {
                folderNames.add(value.getName());
            }
        }
        return folderNames;
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