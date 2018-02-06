package org.elsys.motorcycle_security.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.elsys.motorcycle_security.R;
import org.elsys.motorcycle_security.http.Api;
import org.elsys.motorcycle_security.models.Device;
import org.elsys.motorcycle_security.models.DevicePin;
import org.elsys.motorcycle_security.models.Globals;
import org.elsys.motorcycle_security.models.LoginDetails;
import org.elsys.motorcycle_security.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.util.Patterns.EMAIL_ADDRESS;

public class Register extends AppCompatActivity implements View.OnClickListener {
    private EditText passwordInput;
    private EditText emailInput;
    private EditText deviceIdInput;
    private TextView errorsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
         passwordInput = findViewById(R.id.RegPassword);
         emailInput = findViewById(R.id.RegEmail);
         deviceIdInput = findViewById(R.id.RegDeviceId);
         errorsText = findViewById(R.id.ErrorsRegText);
        Button registerButton = findViewById(R.id.RegBtn);
        registerButton.setOnClickListener(this);
    }

    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.RegBtn: {
                if(deviceIdInput.getText().toString().length() == 0) errorsText.setText("Device pin number field can't be blank");
                else if(emailInput.getText().toString().length() == 0) errorsText.setText("Email field can't be blank");
                else if(passwordInput.getText().toString().length() == 0) errorsText.setText("Password field can't be blank");
                else if(deviceIdInput.getText().toString().length() == 0) errorsText.setText("Device pin number field can't be blank");
                else if(EMAIL_ADDRESS.matcher(emailInput.toString()).matches()) errorsText.setText("Invalid email address.");
                else if(passwordInput.getText().toString().length() < 6) errorsText.setText("Password is too short (Minimum 6 characters)");
                else {
                   final Api api = Api.RetrofitInstance.create();
                    api.getDevicePin(deviceIdInput.getText().toString()).enqueue(new Callback<DevicePin>() {
                        @Override
                        public void onResponse(Call<DevicePin> call, Response<DevicePin> response) {
                            if (response.isSuccessful()) {
                                DevicePin devicePin = response.body();
                                if(devicePin.getPin().equals(deviceIdInput.getText().toString())) {
                                    api.getDeviceOnlyDeviceId(deviceIdInput.getText().toString()).enqueue(new Callback<Device>() {
                                        @Override
                                        public void onResponse(Call<Device> call, Response<Device> response) {
                                            Device device = response.body();
                                            if(device == null) {
                                                api.getUserAccountOnlyEmail(emailInput.getText().toString()).enqueue(new Callback<User>() {
                                                    @Override
                                                    public void onResponse(Call<User> call, Response<User> response) {
                                                        User checkUser = response.body();
                                                        if(checkUser == null) {
                                                            User user = new User(emailInput.getText().toString(), passwordInput.getText().toString(), deviceIdInput.getText().toString());
                                                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putInt("Number of devices", 1).apply();
                                                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isAuthorized", true).apply();
                                                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("Device 1", user.getDevices().get(0).getDeviceId()).apply();
                                                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("Current device in use", deviceIdInput.getText().toString()).apply();
                                                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putLong("UserId", user.getId()).apply();
                                                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("User email", user.getEmail()).apply();
                                                            api.createUserAccount(user).enqueue(new Callback<User>() {
                                                                @Override
                                                                public void onResponse(Call<User> call, Response<User> response) {
                                                                }
                                                                @Override
                                                                public void onFailure(Call<User> call, Throwable t) {
                                                                }
                                                            });
                                                           final LoginDetails loginDetails = new LoginDetails(emailInput.getText().toString(), passwordInput.getText().toString());
                                                           Log.d("NIGGER COD LOOGER.d", loginDetails.getEmail() + " " + loginDetails.getPassword());
                                                           api.Login(loginDetails).enqueue(new Callback<Void>() {
                                                                @Override
                                                                public void onResponse(Call<Void> call, Response<Void> response) {
                                                                    String token = response.headers().get("authorization");
                                                                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("Authorization", token).apply();
                                                                    Log.d("RIIIIIIIIIIIIIIII", token);
                                                                }
                                                                @Override
                                                                public void onFailure(Call<Void> call, Throwable t) {
                                                                    Log.d("DADADDA", t.getMessage().toString());
                                                                }
                                                            });
                                                            Intent myIntent = new Intent(v.getContext(), Main.class);
                                                            startActivity(myIntent);
                                                        }
                                                        else errorsText.setText("Email is already in use");
                                                    }
                                                    @Override
                                                    public void onFailure(Call<User> call, Throwable t) {
                                                    }
                                                });
                                            }
                                            else errorsText.setText("Device pin number is already in use");
                                        }
                                        @Override
                                        public void onFailure(Call<Device> call, Throwable t) {
                                        }
                                    });
                                }
                            }
                            else errorsText.setText("Invalid device pin number");
                        }
                        @Override
                        public void onFailure(Call<DevicePin> call, Throwable t) {
                        }
                    });
                }
            }
        }
    }
}

