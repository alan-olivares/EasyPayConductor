package com.easypay.alanolivares.easypayconductor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    Boolean as=true;
    Button boton,reg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Configure el formulario de inicio de sesión.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
        String correo = preferences.getString("correo","No existe");
        if(!correo.equals("No existe")){
            if(isOnlineNet()){
                new ConsultarDatos().execute("http://10.10.26.195/easypay/consultaConductor.php?correo="+correo);
            }else{
                Intent re = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(re);
            }
        }
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    as=true;
                    return true;
                }
                as=false;
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();

            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        reg = (Button) findViewById(R.id.registro);
        reg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent re = new Intent(LoginActivity.this, Registro.class);
                startActivity(re);
            }
        });
    }

    public Boolean isOnlineNet() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }


    /**
     * Intenta iniciar sesión o registrar la cuenta especificada en el formulario de inicio de sesión.
     * Si hay errores de formulario (correo electrónico no válido, campos faltantes, etc.),
     *  se presentan los errores y no se realiza ningún intento de inicio de sesión real.
     */
    private static ProgressDialog progressDialog;
    private void attemptLogin() {
        // Restablecer errores.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel=false;
        View focusView = null;

        // Compruebe si hay una contraseña válida, si el usuario ha ingresado una.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError("Contraseña incorrecta");
            focusView = mPasswordView;
            cancel = true;
        }

        // Compruebe si hay una dirección de correo electrónico válida.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("Campo necesario");
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError("Correo invalido");
            focusView = mEmailView;
            cancel = true;
        }
        if (cancel) {
            // Hubo un error; No intente iniciar sesión
            // y enfocar el primer campo de formulario con un error.
            focusView.requestFocus();
        } else {
            // Mostrar un hilandero de progreso y iniciar una tarea
            // en segundo plano para realizar el intento de inicio de sesión de usuario.
            new ConsultarDatos().execute("http://10.10.26.195/easypay/consultaConductor.php?correo="+mEmailView.getText().toString());
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }

    private class ConsultarDatos extends AsyncTask<String, Void, String> {
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
        @Override
        protected void onPostExecute(String result) {
            SharedPreferences preferences = getSharedPreferences("Usuarios",MODE_PRIVATE);
            String verificar="",vercorreo="";
            String verificar2;
            String vercorreo2;
            String nom = "";
            if(!mEmailView.getText().toString().isEmpty()){
                vercorreo2=mEmailView.getText().toString();
                verificar2=mPasswordView.getText().toString();
            }else{
                vercorreo2=preferences.getString("correo","No existe");
                verificar2=preferences.getString("contra","No existe");
            }
            JSONArray ja = null;
            try {
                System.out.println(result);
                ja = new JSONArray(result);
                verificar=ja.getString(3);
                vercorreo=ja.getString(2);
                nom=ja.getString(0);
                System.out.println(verificar);
                System.out.println(vercorreo);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(verificar.equals(verificar2)&&vercorreo.equals(vercorreo2)){
                SharedPreferences.Editor editor = getSharedPreferences("Usuarios",MODE_PRIVATE).edit();
                editor.putString("correo",vercorreo);
                editor.putString("contra",verificar);
                editor.putString("nombre",nom);
                editor.commit();
                progressDialog = ProgressDialog.show(LoginActivity.this,"","Iniciando sesion..",true);
                Toast.makeText(getApplicationContext(), "Inicio de seccion satisfactorio", Toast.LENGTH_LONG).show();
                Intent re = new Intent(LoginActivity.this, MainActivity.class);
                re.putExtra("correo",mEmailView.getText().toString());
                startActivity(re);
            } else{
                SharedPreferences.Editor editor = getSharedPreferences("Usuarios", MODE_PRIVATE).edit();
                editor.putString("correo", "No existe");
                editor.putString("contra", "No existe");
                editor.putString("nombre", "No existe");
                editor.commit();
                Toast.makeText(getApplicationContext(), "Correo o contraseña incorrecta", Toast.LENGTH_LONG).show();

                Toast.makeText(LoginActivity.this, "Credenciales invalidas", Toast.LENGTH_SHORT).show();
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