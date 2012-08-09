package com.cw.diacloud.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.cw.diacloud.Server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("unused")
public final class Diacloud extends Activity {
    public static final String TAG = "Diacloud";
    public static String provider = "";
	public static String operation = "";
	
    private static final String ENDPOINT = "http://10.0.2.2:" + Server.PORT;
	private static final String PROVIDERS_CONTEXT = "/providers";
	private static final String INSTANCES_CONTEXT = "/instances";

	private TextView mQueryTextView;
	private Button mProvidersButton;
	private Button mOperationsButton;
	private AlertDialog providersAlert;
	private AlertDialog operationsAlert;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try {
    		Log.v(TAG, "Activity State: onCreate()");
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.diacloud);
	        
	        mQueryTextView = (TextView) findViewById(R.id.queryTextView);
	        
	        final String[] providers = Diacloud.getProviders();
	        AlertDialog.Builder providersBuilder = new AlertDialog.Builder(this);
	        providersBuilder.setTitle("Pick a provider");
	        providersBuilder.setSingleChoiceItems(providers, -1, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int index) {
	                Toast.makeText(getApplicationContext(), providers[index], Toast.LENGTH_SHORT).show();
	                provider = providers[index];
	            }
	        });
	        providersAlert = providersBuilder.create();
	        
	        mProvidersButton = (Button) findViewById(R.id.providersButton);
	        mProvidersButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                Log.d(TAG, "mProvidersButton clicked");
	                providersAlert.show();
	            }
	        });

	        final String[] operations = {"instances", "images"};//Diacloud.getProviders();
	        AlertDialog.Builder operationsBuilder = new AlertDialog.Builder(this);
			operationsBuilder.setTitle("Pick an operation");
			operationsBuilder.setSingleChoiceItems(operations, -1, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int index) {
	            	try {
						Toast.makeText(getApplicationContext(), operations[index], Toast.LENGTH_SHORT).show();
						operation = operations[index];
	                	mQueryTextView.setText(Diacloud.getInstances(Diacloud.provider));
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }
	        });
	        operationsAlert = operationsBuilder.create();
	        
	        mOperationsButton = (Button) findViewById(R.id.operationsButton);
	        mOperationsButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                Log.d(TAG, "mOperationsButton clicked");
	                operationsAlert.show();
	            }
	        });
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private static String[] getProviders() throws Exception {
    	List<String> providers = new ArrayList<String>();
    	String providersJson = null;
    	HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(ENDPOINT + PROVIDERS_CONTEXT);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			providersJson = Diacloud.inputStreamToString(entity.getContent());
			httpget.abort();
		}
		httpclient.getConnectionManager().shutdown();
		JSONObject json = new JSONObject(providersJson);
		JSONArray providersJsonArray = json.getJSONArray("providers");
		for (int i = 0; i < providersJsonArray.length(); i++) {
			String providerName = providersJsonArray.getJSONObject(i).getString("name");
			providers.add(providerName);
		}
    	return providers.toArray(new String[providers.size()]);
    }
    
    private static String getInstances(String provider) throws Exception {
    	String instancesJson = null;
    	HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(ENDPOINT + INSTANCES_CONTEXT);
        HttpEntity requestBody = new StringEntity("{\"provider\":{\"name\":\"rackspace\"}}");
        httppost.setEntity(requestBody);
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			instancesJson = Diacloud.inputStreamToString(entity.getContent());
			httppost.abort();
		}
		httpclient.getConnectionManager().shutdown();
		return instancesJson;
    }
    
    private static String inputStreamToString(InputStream is) {
        try {
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (Exception e) {
            return "";
        }
    }
}
