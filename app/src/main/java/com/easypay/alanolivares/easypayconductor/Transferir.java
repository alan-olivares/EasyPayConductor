package com.easypay.alanolivares.easypayconductor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Transferir extends AppCompatActivity {
    EditText nombre,apellido,clabe,banco,contra;
    Button transfer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferir);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
        SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
        final String contras=preferences.getString("contra","No existe");
        final String correo=preferences.getString("correo","No existe");
        nombre=findViewById(R.id.nombreTrans);
        apellido=findViewById(R.id.apellidoTrans);
        clabe=findViewById(R.id.ClabeTrans);
        banco=findViewById(R.id.bancoTrans);
        contra=findViewById(R.id.passwordTrans);
        transfer=findViewById(R.id.buttonTrans);
        final String saldo =getIntent().getStringExtra("saldo");
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(contras);
                System.out.println(contra.getText().toString());
                if(contras.equals(contra.getText().toString())){
                    if(Float.parseFloat(saldo.replace("$",""))>5){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext()).setTitle("Confirmación").
                                setMessage("Estas seguro de retirar "+saldo.replace("$","")).
                                setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new Transfiriendo().execute("http://10.10.26.195/easypay/transferir.php?correo="+correo+"&nombre="+nombre+"&apellido="+apellido+"&banco="+banco+"&clabe="+clabe+"&estado=Procesando");


                                    }
                                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }else{
                        Snackbar.make(v, "El monto minimo de retiro son 50 pesos",Snackbar.LENGTH_LONG)
                                .show();
                    }

                }else{
                    Snackbar
                            .make(v, "Contraseña incorrecta",Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;


            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private class Transfiriendo extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            //progressDialog = ProgressDialog.show(Registro.this,"","Registrando..",true);
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
            if(result.startsWith("1")){
                Toast.makeText(Transferir.this, "Hubo un problema al realizar la transferencia", Toast.LENGTH_SHORT).show();
            }
            else{
                //progressDialog.dismiss();

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
}
