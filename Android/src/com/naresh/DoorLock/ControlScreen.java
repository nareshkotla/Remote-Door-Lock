//Calling methods from background

package com.naresh.DoorLock;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;
import com.utd.DoorLock.R;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ControlScreen extends Activity implements
        ActionBar.OnNavigationListener{

    private Switch lock;
    private TextView status;
    /*Server address*/
    public String serverHostName = "10.176.66.44";
    public static int serverPort = 3001;
    public Socket socket = null;


    // action bar
    private ActionBar actionBar;
    public ClientThread clientThread = null;
    public PiThread threadForPi = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_controlscreen);
        actionBar = getActionBar();
        lock = (Switch)findViewById(R.id.switch1);
        status = (TextView)findViewById(R.id.displayText);
        status.setText("Locked");

        threadForPi = new PiThread();
        threadForPi.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        threadForPi.execute();

        lock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    System.out.println("lock pressed");
                    clientThread = new ClientThread();
                    clientThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"lock");
                    System.out.println("sending message");
                }
                else {
                    System.out.println("unlock pressed");
                    clientThread = new ClientThread();
                    clientThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"unlock");
//                    clientThread.execute("unlock");
               }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * On selecting action bar icons
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.logout:
                Log.e("Sending logout from normal user to the server","");
                new ClientThread().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "logout");
                Intent viewIntent = new Intent(getApplicationContext(), HomeScreen.class);
                viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(viewIntent, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // Action to be taken after selecting a spinner item
        return false;
    }

    public class PiThread extends AsyncTask<String, String, Void> {

        public Socket skt = null;
        private BufferedReader in = null;

       @Override
        protected Void doInBackground(String... status) {

            try {
                skt = new Socket("10.176.66.44", 8500);
                in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                Log.e("Server Name ", "10.176.66.44");
                Log.e("Server port ", "8500");

            } catch (IOException e) {
                e.printStackTrace();
            }

            while(true) {
                try {
                    String rd = in.readLine();
                    Log.e("-------------------- PI: Message received from Server", rd);

                    if(rd.contains("android")) {
                        String statusText = null, statusToast = null;
                        if (rd.contains("unlocked")) {
                            statusText = "UNLOCKED";
                            statusToast = "Door UNLOCKED successfully ... ";
                            publishProgress(statusText, statusToast);
                            Log.e("Update from raspberrypi : ", statusToast);
                        } else if (rd.contains("locked")) {
                            statusText = "LOCKED";
                            statusToast = "Door LOCKED successfully ... ";
                            publishProgress(statusText, statusToast);
                            Log.e("Update from raspberrypi : ", statusToast);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            status.setText(values[0]);
            if(values[0].equalsIgnoreCase("locked")) {
                lock.setChecked(true);
            }
            Toast.makeText(getApplicationContext(), values[1], Toast.LENGTH_SHORT).show();
        }
    }

    public class ClientThread extends AsyncTask<String, String, Void>{


        @Override
        protected Void doInBackground(String... status) {

            try{
                InetAddress serverAddress = InetAddress.getByName(serverHostName);
                Log.e("Server IP Address", serverAddress.getHostAddress());
                Log.e("Server Name ", serverAddress.getHostName());
                Log.e("Server port ", String.valueOf(ControlScreen.serverPort));

                socket = new Socket(serverAddress.getHostName(), serverPort);

                String send_message=null;
                String user_name=null;

                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Bundle extras = getIntent().getExtras();

                if(extras!=null){
                    user_name = extras.getString("UserName");
                }


                if(status[0]=="lock"){
                    send_message = "android:"+user_name+":lock"+":0";
                }
                else if(status[0]=="unlock"){
                    send_message = "android:"+user_name+":unlock"+":0";
                }
                else if(status[0]=="logout"){
                    send_message = "android:"+user_name+":logout"+":0";
                }

                output.println(send_message);
                output.flush();
                Log.e("Message sent from Android", send_message);

                while(true) {
                    String read = input.readLine();
                    Log.e("Message received from Server", read);
                    if(read.contains("android")) {
                        String statusText = null, statusToast = null;
                        if(read.contains("unlocked")) {
                            statusText = "UNLOCKED";
                            statusToast = "Door UNLOCKED successfully ... ";
                            publishProgress(statusText, statusToast);
                            Log.e("Update from raspberrypi : ", statusToast);
                            break;
                        } else if(read.contains("locked")) {
                            statusText = "LOCKED";
                            statusToast = "Door LOCKED successfully ... ";
                            publishProgress(statusText, statusToast);
                            Log.e("Update from raspberrypi : ", statusToast);
                            break;
                        }
                        else if(read.contains("Error1")){
                            statusText = "No Raspberry Pi linked with your UserID";
                            statusToast = "No Raspberry Pi linked with your UserID";
                            publishProgress(statusText, statusToast);
                            Log.e("Update from raspberrypi : ", statusToast);
                            break;
                        }
                        else if(read.contains("Error2")){
                            statusText = "RaspberryPi linked with your ID is not matching with the one you are trying to access";
                            statusToast = "RaspberryPi Serial Number not matching";
                            publishProgress(statusText, statusToast);
                            break;
                        }
                        else{
                            statusText = "UNKNOWN";
                            statusToast = "unknown status ";
                            publishProgress(statusText);
                            Log.e("Update from raspberrypi : ", statusToast);
                            break;
                        }
//                        break;
                    } else if(read.contains("loggedout")) {
                        Log.e(" ", "Logged out!!");
                    } else {
                        String invalidResponse = "Unknown response!!";
                        Log.e("!", invalidResponse);
                        break;
                    }
                }
            }
            catch(UnknownHostException e1){
                Log.e("Exception","Unknown Hostname Exception ", e1);
            }catch(EOFException e2){
                e2.printStackTrace();
            }catch (IOException e3) {
                Log.e("Exception", "IO Exception ", e3);
            }
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("Exception", "IO Exception ", e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            status.setText(values[0]);
            if(values[0].equalsIgnoreCase("locked")) {
                lock.setChecked(true);
            }
            Toast.makeText(getApplicationContext(), values[1], Toast.LENGTH_SHORT).show();
        }
    }
}