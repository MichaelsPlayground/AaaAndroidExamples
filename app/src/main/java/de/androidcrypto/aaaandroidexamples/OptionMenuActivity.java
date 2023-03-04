package de.androidcrypto.aaaandroidexamples;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class OptionMenuActivity extends AppCompatActivity {

    private final String TAG = "OptionMenu Act";

    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7;
    TextView tv1;
    com.google.android.material.textfield.TextInputEditText etRead, etWrite;

    String uniqueId; // some random data
    /**
     * global variables that take the content for exporting and mailing
     */
    String importString = "";
    String exportString = "";
    String stringFileName = "test.txt";
    byte[] importByte = new byte[0];
    byte[] exportByte = new byte[0];
    String byteFileName = "test.dat";
    String importFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_menu);

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
        etWrite = findViewById(R.id.etWrite);
        etRead = findViewById(R.id.etRead);

        // random data
        uniqueId = UUID.randomUUID().toString();
        etWrite.setText(uniqueId);
        exportString = uniqueId;
        exportByte = uniqueId.getBytes(StandardCharsets.UTF_8);


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn1 back to main menu");
                Intent intent = new Intent(OptionMenuActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn2 new write data");
                uniqueId = UUID.randomUUID().toString();
                etWrite.setText(uniqueId);
                exportString = uniqueId;
                exportByte = uniqueId.getBytes(StandardCharsets.UTF_8);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn3");

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
     * section UI
     */

    private void writeToUiToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(),
                    message,
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * section OptionsMenu mail data methods
     */

    private void exportMail() {
        if (exportString.isEmpty()) {
            writeToUiToast("Scan a tag first before sending emails :-)");
            return;
        }
        String subject = "Dump data";
        String body = exportString;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * section OptionsMenu export text file methods
     */

    private void exportTextFile() {
        if (exportString.isEmpty()) {
            writeToUiToast("Scan a tag first before writing files :-)");
            return;
        }
        writeStringToExternalSharedStorage();
    }

    private void writeStringToExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //boolean pickerInitialUri = false;
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        // get filename from edittext
        String filename = stringFileName;
        // sanity check
        if (filename.equals("")) {
            writeToUiToast("scan a tag before writing the content to a file :-)");
            return;
        }
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        selectTextFileActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> selectTextFileActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            // Perform operations on the document using its URI.
                            try {
                                // get file content from edittext
                                String fileContent = exportString;
                                writeTextToUri(uri, fileContent);
                                writeToUiToast("file written to external shared storage: " + uri.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                                writeToUiToast("ERROR: " + e.toString());
                                return;
                            }
                        }
                    }
                }
            });

    private void writeTextToUri(Uri uri, String data) throws IOException {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().getContentResolver().openOutputStream(uri));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            System.out.println("Exception File write failed: " + e.toString());
        }
    }

    /**
     * section OptionsMenu export binary file methods
     */

    private void exportBinaryFile() {
        if (exportByte.length == 0) {
            writeToUiToast("Scan a tag first before writing files :-)");
            return;
        }
        writeByteToExternalSharedStorage();
    }

    private void writeByteToExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //boolean pickerInitialUri = false;
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        // get filename from edittext
        String filename = byteFileName;
        // sanity check
        if (filename.equals("")) {
            writeToUiToast("scan a tag before writing the content to a file :-)");
            return;
        }
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        selectBinaryFileActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> selectBinaryFileActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            // Perform operations on the document using its URI.
                            try {
                                // get file content from edittext
                                writeByteToUri(uri, exportByte);
                                //String message = "file written to external shared storage: " + uri.toString();
                                writeToUiToast("binary file written to external shared storage: " + uri.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                                writeToUiToast("ERROR: " + e.toString());
                                return;
                            }
                        }
                    }
                }
            });

    private boolean writeByteToUri(Uri uri, byte[] data) throws IOException {
        try (OutputStream outputStream = getApplicationContext().getContentResolver().openOutputStream(uri);) {
            outputStream.write(data);
            outputStream.close();
            return true;
        } catch (Exception e) {
            System.out.println("*** EXCEPTION: " + e);
            return false;
        }
    }

    /**
     * section OptionsMenu import text file methods
     */

    private void importTextFile() {
        /*
        if (exportString.isEmpty()) {
            writeToUiToast("Scan a tag first before writing files :-)");
            return;
        }
        */
        readStringFromExternalSharedStorage();
    }

    private void readStringFromExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //boolean pickerInitialUri = false;
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        selectImportTextFileActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> selectImportTextFileActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            try {
                                importString = readTextFromUri(getApplicationContext(), uri);
                                etRead.setText(importString);
                                // todo set filename
                            } catch (IOException e) {
                                //throw new RuntimeException(e);
                                etRead.setText("Error: " + e.getMessage());
                            }
                        }
                    }
                }
            });

    public static String readTextFromUri(Context context, Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     context.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * section OptionsMenu import binary file methods
     */

    private void importBinaryFile() {
        /*
        if (exportString.isEmpty()) {
            writeToUiToast("Scan a tag first before writing files :-)");
            return;
        }
        */
        readByteFromExternalSharedStorage();
    }

    private void readByteFromExternalSharedStorage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        //boolean pickerInitialUri = false;
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        selectImportBinaryFileActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> selectImportBinaryFileActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();
                            try {
                                readBytesFromUri(getApplicationContext(), uri);
                                //etRead.setText(importString);
                                // todo set filename
                            } catch (IOException e) {
                                //throw new RuntimeException(e);
                                etRead.setText("Error: " + e.getMessage());
                            }
                        }
                    }
                }
            });
    private byte[] readBytesFromUri(Context context, Uri uri) throws IOException {
        if (context != null) {
            ContentResolver contentResolver = context.getContentResolver();
            String filename = queryName(contentResolver, uri);
            importFileName = filename;
            Thread DoBasicCreateFolder = new Thread() {
                public void run() {
                    try (InputStream inputStream = contentResolver.openInputStream(uri);
                         // this dynamically extends to take the bytes you read
                         ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();) {
                        // this is storage overwritten on each iteration with bytes
                        int bufferSize = 1024;
                        byte[] buffer = new byte[bufferSize];
                        // we need to know how may bytes were read to write them to the byteBuffer
                        int len = 0;
                        while ((len = inputStream.read(buffer)) != -1) {
                            byteBuffer.write(buffer, 0, len);
                        }
                        // and then we can return your byte array.
                        //return byteBuffer.toByteArray();
                        importByte = byteBuffer.toByteArray();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // todo set filename
                                etRead.setText(BinaryUtils.bytesToHex(importByte));
                                //Toast.makeText(DeleteGoogleDriveFile.this, "selected file deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            };
            DoBasicCreateFolder.start();

            /*
            try (InputStream inputStream = contentResolver.openInputStream(uri);
                 // this dynamically extends to take the bytes you read
                 ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();) {
                // this is storage overwritten on each iteration with bytes
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                // we need to know how may bytes were read to write them to the byteBuffer
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                // and then we can return your byte array.
                return byteBuffer.toByteArray();
            }
            */
        }
        return null;
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    /**
     * section for OptionsMenu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option_sample_activity, menu);

        MenuItem mExportMail = menu.findItem(R.id.action_export_mail);
        mExportMail.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mExportMail");
                exportMail();
                return false;
            }
        });

        MenuItem mExportTextFile = menu.findItem(R.id.action_export_text_file);
        mExportTextFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mExportTextFile");
                exportTextFile();
                return false;
            }
        });

        MenuItem mExportBinaryFile = menu.findItem(R.id.action_export_binary_file);
        mExportBinaryFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mExportBinaryFile");
                exportBinaryFile();
                return false;
            }
        });

        MenuItem mImportTextFile = menu.findItem(R.id.action_import_text_file);
        mImportTextFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mImportTextFile");
                importTextFile();
                return false;
            }
        });

        MenuItem mImportBinaryFile = menu.findItem(R.id.action_import_binary_file);
        mImportBinaryFile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "mImportBinaryFile");
                importBinaryFile();
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

}