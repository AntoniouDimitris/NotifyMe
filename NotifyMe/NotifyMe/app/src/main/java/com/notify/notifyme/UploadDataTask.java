package com.notify.notifyme;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadDataTask extends AsyncTask<String, Void, Boolean> {

    private Context ctx;
    private String tag = "Uploader";
    private MyNotifService callingservice;

    public UploadDataTask(Context con, MyNotifService ns)
    {

        this.ctx=con;
        this.callingservice=ns;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        String sourceFileUri = params[0];
        String upLoadServerUri = "http://150.140.15.50/elton/receivedb_no_insert.php";
        Log.i(tag, "uploading file "+sourceFileUri);

        try {

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(sourceFileUri);

            if (sourceFile.isFile()) {

                try {
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(
                            sourceFile);
                    URL url = new URL(upLoadServerUri);

                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();

                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("file", sourceFileUri);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                            + sourceFileUri + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math
                                .min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0,
                                bufferSize);

                    }

                    // send multipart form data necesssary after file
                    // data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens
                            + lineEnd);

                    // Responses from the server (code and message)
                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn
                            .getResponseMessage();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // close the streams //
                    fileInputStream.close();
                    //dos.flush();
                    //dos.close();


                    if (serverResponseCode == 200) {
                        Log.i(tag, "File Upload Success: "+serverResponseCode+"\n"+serverResponseMessage+
                                "\n"+result.toString());
                        return true;
                    }
                    else {
                        Log.i(tag, "File Upload Failed: "+serverResponseCode+"\n"+serverResponseMessage+
                                "\n"+result.toString());
                        return false;
                    }


                } catch (Exception e) {
                    conn.disconnect();
                    e.printStackTrace();
                    return false;

                }
                finally
                {
                    conn.disconnect();
                }


            }
            else
                return false;// End else block


        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor e = sp.edit();

        if (result)
        {
            Toast.makeText(ctx, "File Upload Success",
                    Toast.LENGTH_SHORT).show();

            if(callingservice!=null) {
                e.putBoolean("uploaded", true);
                e.commit();
                callingservice.notifyToUninstall();
            }
            else
            {
                e.putBoolean("uploaded", false);
                e.commit();
            }
        }
        else
        {
            Toast.makeText(ctx, "File Upload Failed",
                    Toast.LENGTH_SHORT).show();
            e.putBoolean("uploaded", false);
            e.commit();
        }
    }
}

