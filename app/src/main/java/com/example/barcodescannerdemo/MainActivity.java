package com.example.barcodescannerdemo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void handleClick(View view) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        String mode;
        switch (view.getId()) {
            case R.id.btn_qr_code:
                mode = "QR_CODE_MODE";
                break;
            case R.id.btn_product:
                mode = "PRODUCT_MODE";
                break;
            case R.id.btn_other:
                mode = "CODE_39,CODE_93,CODE_128,DATA_MATRIX,CODABAR";
                break;
            default:
                return;
        }
        intent.putExtra("SCAN_MODE", mode);
        try{
            startActivityForResult(intent,1);
        }catch(ActivityNotFoundException e){
            Toast.makeText(this, "Scanner nicht insatalliert", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1){
            TextView status = findViewById(R.id.tv_status);
            TextView result = findViewById(R.id.tv_result);
            if(resultCode== RESULT_OK){
                status.setText(data.getStringExtra("SCAN_RESULT_FORMAT"));
                result.setText(getProductName(data.getStringExtra("SCAN_RESULT")));
            }else if(resultCode == RESULT_CANCELED){
                status.setText("Scann wurde abgebrochen");
                result.setText("Nochmal versuchen");

            }
        }

    }
    private String getProductName(String scanResult){
        HoleDatenTask task = new HoleDatenTask();
        String result = null;
        try {
            result = task.execute(scanResult).get();
            JSONObject rootObject = new JSONObject(result);
            Log.d(TAG, "getProductName: "+ rootObject.toString(2));
            if(rootObject.has("product")){
                JSONObject productObject = rootObject.getJSONObject("product");
                if(productObject.has("product_name")){
                    return productObject.getString("product_name");
                }
            }
        } catch (ExecutionException e) {
            Log.e(TAG, "", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "", e);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        return "Artikel nicht gefunden";
    }

    public class HoleDatenTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            final String baseUrl = "https://world.openfoodfacts.org/api/v0/product/";
            final String requestUrl = baseUrl + strings[0]+".json";
            // super wichtig -> um die json-url zu finden!!!
            Log.d(TAG, "doInBackground: " + requestUrl);
            StringBuilder result = new StringBuilder();
            URL url = null;

            try {
                url = new URL(requestUrl);
            } catch (MalformedURLException e) {
                Log.e(TAG, "", e);
            }
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
                String line;
                while ((line=reader.readLine())!=null){
                    result.append(line);
                }
            }catch (IOException e){

            }
            Log.d(TAG, "doInBackground: " + result.toString());
            return result.toString();
        }
    }
}
