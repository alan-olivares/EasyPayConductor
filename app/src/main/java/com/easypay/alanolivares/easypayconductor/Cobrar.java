package com.easypay.alanolivares.easypayconductor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


public class Cobrar extends Fragment {
    SurfaceView surfaceView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    String temporal="";
    MediaPlayer mp;
    String correo;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_cobrar, container, false);
        surfaceView = view.findViewById(R.id.camera);
        SharedPreferences preferences = getActivity().getSharedPreferences("Usuarios",Context.MODE_PRIVATE);
        correo = preferences.getString("correo","No existe");
        barcodeDetector = new BarcodeDetector.Builder(getContext()).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(getContext(), barcodeDetector).setRequestedPreviewSize(640, 480).setAutoFocusEnabled(true).build();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
        }else{
            Toast.makeText(getContext(),"Se necesita acceder a la camara para realizar cobros",Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
        }
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                try{
                    cameraSource.start(holder);
                }catch (IOException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes= detections.getDetectedItems();

                if(qrCodes.size()!=0&&!temporal.equals(qrCodes.valueAt(0).displayValue)&&qrCodes.valueAt(0).displayValue.contains("@")){
                    Vibrator vibrator=(Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                    //Toast.makeText(getContext(),"Codigo: "+qrCodes.valueAt(0).displayValue,Toast.LENGTH_LONG);
                    temporal=qrCodes.valueAt(0).displayValue;
                    System.out.println(temporal);
                    String data[]=qrCodes.valueAt(0).displayValue.split("::");
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            //Toast.makeText(getActivity(),"Codigo: "+qrCodes.valueAt(0).displayValue,Toast.LENGTH_LONG).show();
                            new Handler().postDelayed(new Runnable(){
                                @Override
                                public void run() {
                                    temporal="";
                                }
                            }, 4000);
                        }
                    });

                    new Cobrando().execute("http://10.10.26.195/easypay/cobrar.php?user="+data[0]+"&password="+data[1]+"&chofer="+correo);
                    //Toast.makeText(getContext(),"Codigo: "+qrCodes.valueAt(0),Toast.LENGTH_LONG);
                }
            }
        });
        return view;

    }

    private class Cobrando extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(String result) {
            String mensaje="";
            JSONArray ja = null;
            try {
                System.out.println(result);
                ja = new JSONArray(result);
                mensaje=ja.getString(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Toast.makeText(getContext(),mensaje,Toast.LENGTH_LONG).show();
            if(mensaje.equals("Pago exitoso")){
                mp= (MediaPlayer) MediaPlayer.create(getContext(),R.raw.pago_aceptado);
                mp.start();

            }else{
                mp= (MediaPlayer) MediaPlayer.create(getContext(),R.raw.saldo_insufiente);
                mp.start();
            }

        }



    }
    private String downloadUrl(String myurl) throws IOException {
        Log.i("URL",""+myurl);
        myurl = myurl.replace(" ","%20");
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("respuesta", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


}


