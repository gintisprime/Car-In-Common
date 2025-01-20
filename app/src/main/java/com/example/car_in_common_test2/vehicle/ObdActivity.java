package com.example.car_in_common_test2.vehicle;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.pires.obd.commands.ObdCommand;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ObdActivity extends AppCompatActivity {

    public interface FuelLevelListener {
        void onFuelLevelReceived(String fuelLevel);
        void onError(String errorMessage);
    }

    private Socket socket;

    public void fetchFuelLevel(FuelLevelListener listener) {
        new Thread(() -> {
            try {
                ObdAdapter obdAdapter = new ObdAdapter("192.168.1.2", 35000);
                socket = obdAdapter.connectToObdAdapter();

                if (socket == null) {
                    listener.onError("No OBD-II adapter connected.");
                    return;
                }

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                // Log connection success
                Log.d("OBD", "Connected to OBD adapter successfully.");

                // Send ATSH7C0 command
                Log.d("OBD", "Sending: ATSH7C0");
                outputStream.write("ATSH7C0\r".getBytes());
                outputStream.flush();
                Thread.sleep(200);
                String atsh7c0Response = reader.readLine();
                Log.d("OBD", "Response to ATSH7C0: " + atsh7c0Response);

                // Execute the custom OBD command
                Log.d("OBD", "Sending: 2129");
                CustomFuelLevelCommand fuelLevelCommand = new CustomFuelLevelCommand();
                fuelLevelCommand.run(inputStream, outputStream);

                // Get the formatted fuel level result
                String fuelLevelResult = fuelLevelCommand.getFormattedResult();
                Log.d("OBD", "Fuel Level: " + fuelLevelResult);

                // Pass the fuel level back via the listener
                listener.onFuelLevelReceived(fuelLevelResult);
            } catch (Exception e) {
                Log.e("OBD", "Error communicating with OBD adapter: " + e.getMessage(), e);
                listener.onError("Error: " + e.getMessage());
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                        Log.d("OBD", "Socket closed successfully.");
                    } catch (Exception e) {
                        Log.e("OBD", "Error closing socket: " + e.getMessage(), e);
                    }
                }
            }
        }).start();
    }

    public static class CustomFuelLevelCommand extends ObdCommand {

        private double fuelLevelPercentage = 0.0;

        public CustomFuelLevelCommand() {
            super("2129");
        }

        @Override
        protected void performCalculations() {
            String rawResult = getResult();
            Log.d("OBD", "Raw result from emulator: " + rawResult);

            if (rawResult != null && rawResult.length() >= 6) {
                try {
                    String hexValue = rawResult.substring(rawResult.length() - 2).trim();
                    int decimalValue = Integer.parseInt(hexValue, 16);
                    fuelLevelPercentage = (decimalValue / 255.0) * 100;
                } catch (NumberFormatException e) {
                    Log.e("OBD", "Error parsing fuel level: " + e.getMessage(), e);
                    fuelLevelPercentage = 0.0;
                }
            } else {
                Log.e("OBD", "Invalid raw result: " + rawResult);
                fuelLevelPercentage = 0.0;
            }
        }

        @Override
        public String getFormattedResult() {
            return String.format("%.2f%%", fuelLevelPercentage);
        }

        @Override
        public String getCalculatedResult() {
            return String.valueOf(fuelLevelPercentage);
        }

        @Override
        public String getName() {
            return "Custom Fuel Level Command";
        }
    }
}
