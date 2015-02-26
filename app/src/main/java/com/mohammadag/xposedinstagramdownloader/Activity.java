package com.mohammadag.xposedinstagramdownloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

public class Activity extends android.app.Activity {

    private int version = 123;
    String Code = "Missing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getHooksHttp();
            }
        });
    }

    public void getHooksHttp () {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for(int i=0;i<packs.size();i++) {
            PackageInfo p = packs.get(i);
            if (p.packageName.equals("com.instagram.android")) {
                version = p.versionCode;
            }
        }

        System.out.println(version);

        StringBuilder total = null;
        int broke = 0;

        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httppost = new HttpGet("http://pastebin.com/raw.php?i=sTXbUFcx");
            HttpResponse response = null;
            try {
                response = httpclient.execute(httppost);
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            HttpEntity ht = response.getEntity();

            BufferedHttpEntity buf = null;
            try {
                buf = new BufferedHttpEntity(ht);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            InputStream is = null;
            try {
                is = buf.getContent();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            BufferedReader r = new BufferedReader(new InputStreamReader(is));

            total = new StringBuilder();
            String line;
            try {
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Broke");
            broke = 1;
        }


        if (broke == 0) {

            SharedPreferences prfs = getSharedPreferences("Hooks", Context.MODE_WORLD_READABLE);
            int savedVersion = prfs.getInt("Version", 1);

            Toast toast = null;

                String hooks = total.toString();
                String[] html = hooks.split("<p>");

                int count = 0;
                int max = 0;
                for (String data : html) {
                    max++;
                }

                for (String data : html) {
                    count++;
                    Code = Integer.toString(version);
                    if (data.contains(Code)) {
                        data = data.replace("<p>", "");
                        data = data.replace("</p>", "");
                        Hooks(data);
                        count = 69;
                    } else {
                        if (count == max) {
                            System.out.println("Trying default hook!");
                            String fallback = html[1];
                            fallback = fallback.replace("<p>", "");
                            fallback = fallback.replace("</p>", "");
                            Hooks(fallback);
                        }
                    }
                }

            toast = Toast.makeText(getApplicationContext(), "Hooks have been updated.\nPlease reboot!", Toast.LENGTH_LONG);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            if( v != null) v.setGravity(Gravity.CENTER);
            toast.show();
        } else {
            Toast toast= Toast.makeText(getApplicationContext(), "Something went wrong.\nPlease check your data connection.", Toast.LENGTH_LONG);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            if( v != null) v.setGravity(Gravity.CENTER);
            toast.show();
        }
    }

    public void Hooks (String data) {
        String[] split = data.split(";");
        SharedPreferences.Editor editor = getSharedPreferences("Hooks", Context.MODE_WORLD_READABLE).edit();
        editor.putString("First", split[1]);
        editor.putString("Second", split[2]);
        editor.putString("Third", split[3]);
        editor.putString("Fourth", split[4]);
        editor.putString("Fifth", split[5]);
        editor.putString("Sixth", split[6]);
        editor.putString("Seventh", split[7]);
        editor.putString("Eighth", split[8]);
        editor.putString("Ninth", split[9]);
        editor.putString("Tenth", split[10]);
        editor.putString("Eleventh", split[11]);
        editor.putString("Twelfth", split[12]);
        editor.putString("Thirteenth", split[13]);
        editor.putString("Fourteenth", split[14]);
        editor.putString("Fifteenth", split[15]);
        editor.putString("Sixteenth", split[16]);
        editor.putString("Seventeenth", split[17]);

        editor.putInt("Version", version);
        editor.apply();
    }
}
