package de.androidcrypto.aaaandroidexamples;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    /**
     * This class provides service methods for working with files on internal and external storage
     */

    /**
     * writes a string to the filename in internal storage, If a subfolder is provided the file is created in the subfolder
     * if the file is existing it will be overwritten
     * @param filename
     * @param subfolder
     * @param data
     * @return true if writing is successful and false if not
     */
    public static boolean writeTextToInternalStorage(@NonNull Context context, @NonNull String filename, String subfolder, @NonNull String data){
        File file;
        if (TextUtils.isEmpty(subfolder)) {
            file = new File(context.getFilesDir(), filename);
        } else {
            File subfolderFile = new File(context.getFilesDir(), subfolder);
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
}
