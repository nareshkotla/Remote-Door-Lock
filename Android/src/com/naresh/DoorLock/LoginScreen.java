package com.naresh.DoorLock;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class LoginScreen extends Activity {

    public Socket socket = null;
    public String serverHostName = "10.176.66.44";

    public static int serverPort = 3001;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loginscreen);

        Button button = (Button)findViewById(R.id.login_button);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText emailId = (EditText) findViewById(R.id.editText);
                EditText password = (EditText) findViewById(R.id.editText2);

                String username = emailId.getText().toString();
                String passwd = password.getText().toString();

                new ClientThread().execute(username,passwd);
            }
        });
    }

    public class ClientThread extends AsyncTask<String, String, Void>{

        @Override
        protected Void doInBackground(String... status) {

            InetAddress serverAddress = null;

            try {
                serverAddress = InetAddress.getByName(serverHostName);
                Log.e("Server IP Address", serverAddress.getHostAddress());
                Log.e("Server Name ", serverAddress.getHostName());
                Log.e("Server port ", String.valueOf(ControlScreen.serverPort));
                socket = new Socket(serverAddress.getHostName(), serverPort);

                String send_message = null;
                PrintWriter output = null;
                output = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                if (status.length != 0) {
                    send_message = "android_validation:" + status[0] + ":" + status[1] + ":0";
                }

                output.println(send_message);
                output.flush();
                Log.e("Message sent from Android", send_message);

                while (true) {
                    String read = input.readLine();
                    Log.e("Message received from Server", read);
                    String statusUpdate = null;

                    String words[] = read.split(":");
                    if (words[0].equals("PASS")) {
                        statusUpdate = "Login Success  ... ";
                        Intent viewIntent = new Intent(getApplicationContext(), ControlScreen.class);
                        viewIntent.putExtra("UserName", status[0]);
                        startActivityForResult(viewIntent, 0);
                        publishProgress(statusUpdate);
                        break;
                    } else {
                        statusUpdate = "Login Failed. Invalid Email-id/Password. ";
                        publishProgress(statusUpdate);
                        break;
                    }
                }
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }

            try {
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
        }
    }
}