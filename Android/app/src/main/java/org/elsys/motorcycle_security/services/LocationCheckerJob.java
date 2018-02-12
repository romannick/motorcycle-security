package org.elsys.motorcycle_security.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.elsys.motorcycle_security.R;
import org.elsys.motorcycle_security.activities.CurrentLocation;
import org.elsys.motorcycle_security.http.Api;
import org.elsys.motorcycle_security.models.Device;
import org.elsys.motorcycle_security.models.DeviceConfiguration;
import org.elsys.motorcycle_security.models.GpsCordinates;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LocationCheckerJob extends JobService {
    private double parkedX;
    private double parkedY;
    private double currentX;
    private double currentY;
    private String deviceId;
    private String authorization;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        deviceId = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("Current device in use", "");
        authorization = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("Authorization", "");
        Log.d("Job","onStartJob");
        final Api api = Api.RetrofitInstance.create();
        api.getDevice(authorization, deviceId).enqueue(new Callback<Device>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                if(response.isSuccessful()) {
                    Device device = response.body();
                    parkedX = device.getParkedX();
                    parkedY = device.getParkedY();
                    Log.d("Job", "Response from device request");
                }
            }
            @Override
            public void onFailure(Call<Device> call, Throwable t) {
            }
        });
        api.getGPSCoordinates(deviceId, authorization).enqueue(new Callback<GpsCordinates>() {
            @Override
            public void onResponse(Call<GpsCordinates> call, Response<GpsCordinates> response) {
                if (response.isSuccessful()) {
                    GpsCordinates gpsCordinates = response.body();
                    boolean moving = false;
                    currentX = gpsCordinates.getX();
                    currentY = gpsCordinates.getY();
                    BigDecimal bdX = new BigDecimal(currentX).setScale(4, RoundingMode.HALF_EVEN);
                    BigDecimal bdY = new BigDecimal(currentY).setScale(4, RoundingMode.HALF_EVEN);
                    BigDecimal bdpX = new BigDecimal(parkedX).setScale(4, RoundingMode.HALF_EVEN);
                    BigDecimal bdpY = new BigDecimal(parkedY).setScale(4, RoundingMode.HALF_EVEN);
                    parkedX = bdpX.doubleValue();
                    parkedY = bdpY.doubleValue();
                    currentX = bdX.doubleValue();
                    currentY = bdY.doubleValue();
                    for(int adder = 0 ; adder != 5; adder++) {
                        double adder2 = adder;
                        if(adder != 0)  adder2 = adder2/ 10000;
                        if(parkedX != currentX + adder2 || parkedX != currentX - adder2) {
                            moving = true;
                            break;
                        }
                        if(parkedY != currentY + adder2 || parkedY != currentY - adder2) {
                            moving = true;
                            break;
                        }
                    }
                    if(parkedX == 0 || parkedY == 0) moving = false; //Against failed server response
                    if(moving) {
                        Log.d("Job", "Notify");
                        DeviceConfiguration deviceConfiguration = new DeviceConfiguration();
                        deviceConfiguration.setDeviceId(deviceId);
                        deviceConfiguration.setStolen(true);
                        api.updateStolenStatus(authorization, deviceConfiguration).enqueue(new Callback<DeviceConfiguration>() {
                            @Override
                            public void onResponse(Call<DeviceConfiguration> call, Response<DeviceConfiguration> response) {}
                            @Override
                            public void onFailure(Call<DeviceConfiguration> call, Throwable t) {}
                        });
                        DeviceConfiguration deviceConfiguration2 = new DeviceConfiguration();
                        deviceConfiguration2.setDeviceId(deviceId);
                        deviceConfiguration2.setTimeOut(10000);
                        api.updateTimeOut(authorization, deviceConfiguration2).enqueue(new Callback<DeviceConfiguration>() {
                            @Override
                            public void onResponse(Call<DeviceConfiguration> call, Response<DeviceConfiguration> response) {}
                            @Override
                            public void onFailure(Call<DeviceConfiguration> call, Throwable t) {}
                        });
                        sendNotification();
                    }
                    else System.out.println("BIKE's safe man dont kill the nigga");
                }
            }
            @Override
            public void onFailure(Call<GpsCordinates> call, Throwable t) {
            }
        });

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
            jobFinished(jobParameters, true);
        }else {
            jobFinished(jobParameters, false);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("Job","onStopJob Called");
        return false;
    }

    protected void sendNotification() {
        Intent intent = new Intent(this, CurrentLocation.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification = new Notification();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationBuilder
                .setDefaults(notification.defaults)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Device " + getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("Current device in use", "") + " is moving !")
                .setContentText("Tap to see current location")
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 , notificationBuilder.build());
    }
}