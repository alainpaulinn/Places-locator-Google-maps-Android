package com.example.alain_restaurants;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

import static com.example.alain_restaurants.R.id.btn_RegisterSubmit;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText et_RegisterLogin = findViewById(R.id.et_RegisterLogin);
        final EditText et_RegisterPassword = findViewById(R.id.et_RegisterPassword);
        final EditText et_RegisterEmail = findViewById(R.id.et_RegisterEmail);

        Button btn_RegisterSubmit = findViewById(R.id.btn_RegisterSubmit);

        final AsyncHttpClient client = new AsyncHttpClient();

        btn_RegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = et_RegisterLogin.getText().toString();
                String password = et_RegisterPassword.getText().toString();
                String email = et_RegisterEmail.getText().toString();

                if (login.isEmpty()||password.isEmpty()||email.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle(R.string.errTitle)
                            .setMessage(R.string.errorEmptyFields)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    String url = "http://dev.imagit.pl/mobilne/api/register";
                    RequestParams params = new RequestParams();
                    params.put("login", login);
                    params.put("pass", password);
                    params.put("email", email);



                    client.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String response = new String(responseBody);
                            if (response.equals("OK")){
                                Toast.makeText(RegisterActivity.this, R.string.infoRegisterSuccessfull,Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(RegisterActivity.this,R.string.errorAccountExists, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                }
            }
        });

    }
}