package com.example.jeffrey.stop;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class MainGame extends ActionBarActivity {

    String[] Answers = new String[7];
    int C = 0;
    String PlayerID;
    String RoomCode;
    int counter;
    String Letter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_game);

        //get sent info from WaitingRoom or ResultsPage
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        PlayerID = b.getString("username");
        RoomCode = b.getString("code");
        counter = b.getInt("counter");  //counter used to tell which letter the current round is for
                                        //in the current sequence

        //post request to get this round's letter
        getLetter();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //if the Stop button is pressed, begin the timer
    //if someone else presses stop, database will change, when that change is detected start timer
    //note: second method of starting timer does not work, waiting for post request to be written
    public void TimerStart(View view) {

        //C is a flag to check if the timer has already been activated
        //this makes sure a second timer is never started if one is already active.
        if (C == 1) {
            return;
        }
        C = 1;

        CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {

            Button button = (Button) findViewById(R.id.StopButton);

            public void onTick(long millisUntilFinished) {
                button.setTextColor(Color.RED);
                button.setText("" + millisUntilFinished / 1000);

            }

            public void onFinish() {
                button.setText("Stop!");

                EditText text = (EditText) findViewById(R.id.Name);
                Answers[0] = text.getText().toString();

                text = (EditText) findViewById(R.id.Country);
                Answers[1] = text.getText().toString();

                text = (EditText) findViewById(R.id.Color);
                Answers[2] = text.getText().toString();

                text = (EditText) findViewById(R.id.Fruit);
                Answers[3] = text.getText().toString();

                text = (EditText) findViewById(R.id.Animal);
                Answers[4] = text.getText().toString();

                text = (EditText) findViewById(R.id.Thing);
                Answers[5] = text.getText().toString();

                text = (EditText) findViewById(R.id.Sport);
                Answers[6] = text.getText().toString();

                sendAnswers(Answers[0], Answers[1], Answers[2], Answers[3], Answers[4],
                        Answers[5], Answers[6]);
            }
        }.start();
    }

    //send results of the round and info to ResultsPage, then close the current round
    public void toResults(String result) {
        //send info
        Bundle b = new Bundle();
        b.putString("username", PlayerID);
        b.putString("code", RoomCode);
        b.putString("result", result);
        b.putInt("counter", counter);
        Intent Rintent = new Intent(this, ResultsPage.class);
        Rintent.putExtras(b);
        Rintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(Rintent);
        //end current game round, to be restarted after results
        finish();


    }

    //send answers to the database
    //note: php script currently does not function correctly, waiting for fix
    private void sendAnswers(String Name, String Country, String Color, String Fruit, String Animal,
                             String Thing, String Sport) {

        //post request, send player ID, room code, and answers to database
        sendPostRequestR(Name, Country, Color, Fruit, Animal, Thing, Sport);

    }

    //get the current round's letter from the database
    private void getLetter() {

        sendPostRequestL(PlayerID, RoomCode, Integer.toString(counter));

    }

    //set the current round's letter to display at the top of the game screen
    //note: there is currently a bug in the post request php script for getting the letter that
    //          causes the script to always return the first letter in the sequence, waiting for fix
    public void setLetter(String result) {
        TextView Round = (TextView) (findViewById(R.id.RLetter));
        Round.setText("This Round's Letter: " + result);
        Letter = result;
    }

    private void sendPostRequestL(String givenUsername, String givenPassword, String counter) {

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String paramUsername = params[0];
                String paramPassword = params[1];
                String paramCounter = params[2];


                System.out.println("*** doInBackground ** paramUsername " + paramUsername + " paramPassword :" + paramPassword);

                HttpClient httpClient = new DefaultHttpClient();

                // In a POST request, we don't pass the values in the URL.
                //Therefore we use only the web page URL as the parameter of the HttpPost argument
                HttpPost httpPost = new HttpPost("http://stopgameapp.com/aplayGame.php");

                // Because we are not passing values over the URL, we should have a mechanism to pass the values that can be
                //uniquely separate by the other end.
                //To achieve that we use BasicNameValuePair
                //Things we need to pass with the POST request
                BasicNameValuePair usernameBasicNameValuePair = new BasicNameValuePair("username", paramUsername);
                BasicNameValuePair passwordBasicNameValuePair = new BasicNameValuePair("code", paramPassword);
                BasicNameValuePair counterBasicNameValuePair = new BasicNameValuePair("counter", paramCounter);

                // We add the content that we want to pass with the POST request to as name-value pairs
                //Now we put those sending details to an ArrayList with type safe of NameValuePair
                List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
                nameValuePairList.add(usernameBasicNameValuePair);
                nameValuePairList.add(passwordBasicNameValuePair);
                nameValuePairList.add(counterBasicNameValuePair);

                try {
                    // UrlEncodedFormEntity is an entity composed of a list of url-encoded pairs.
                    //This is typically useful while sending an HTTP POST request.
                    UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);

                    // setEntity() hands the entity (here it is urlEncodedFormEntity) to the request.
                    httpPost.setEntity(urlEncodedFormEntity);

                    try {
                        // HttpResponse is an interface just like HttpPost.
                        //Therefore we can't initialize them
                        HttpResponse httpResponse = httpClient.execute(httpPost);

                        // According to the JAVA API, InputStream constructor do nothing.
                        //So we can't initialize InputStream although it is not an interface
                        InputStream inputStream = httpResponse.getEntity().getContent();

                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                        StringBuilder stringBuilder = new StringBuilder();

                        String bufferedStrChunk = null;

                        while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
                            stringBuilder.append(bufferedStrChunk);
                        }

                        return stringBuilder.toString();

                    } catch (ClientProtocolException cpe) {
                        System.out.println("First Exception caz of HttpResponese :" + cpe);
                        cpe.printStackTrace();
                    } catch (IOException ioe) {
                        System.out.println("Second Exception caz of HttpResponse :" + ioe);
                        ioe.printStackTrace();
                    }

                } catch (UnsupportedEncodingException uee) {
                    System.out.println("An Exception given because of UrlEncodedFormEntity argument :" + uee);
                    uee.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                setLetter(result);
            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(givenUsername, givenPassword, counter);
    }

    private void sendPostRequestR(String Name, String Country, String Color, String Fruit, String Animal,
                                  String Thing, String Sport) {

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String paramName = params[0];
                String paramCountry = params[1];
                String paramColor = params[2];
                String paramFruit = params[3];
                String paramAnimal = params[4];
                String paramThing = params[5];
                String paramSport = params[6];


                HttpClient httpClient = new DefaultHttpClient();

                // In a POST request, we don't pass the values in the URL.
                //Therefore we use only the web page URL as the parameter of the HttpPost argument
                HttpPost httpPost = new HttpPost("http://stopgameapp.com/agameSubmit.php");

                // Because we are not passing values over the URL, we should have a mechanism to pass the values that can be
                //uniquely separate by the other end.
                //To achieve that we use BasicNameValuePair
                //Things we need to pass with the POST request
                BasicNameValuePair nameBasicNameValuePair = new BasicNameValuePair("name", paramName);
                BasicNameValuePair countryBasicNameValuePair = new BasicNameValuePair("country", paramCountry);
                BasicNameValuePair colorBasicNameValuePair = new BasicNameValuePair("color", paramColor);
                BasicNameValuePair fruitBasicNameValuePair = new BasicNameValuePair("fruit", paramFruit);
                BasicNameValuePair animalBasicNameValuePair = new BasicNameValuePair("animal", paramAnimal);
                BasicNameValuePair thingBasicNameValuePair = new BasicNameValuePair("thing", paramThing);
                BasicNameValuePair sportBasicNameValuePair = new BasicNameValuePair("sport", paramSport);
                BasicNameValuePair usernameBasicNameValuePair = new BasicNameValuePair("playerID", PlayerID);
                BasicNameValuePair codeBasicNameValuePair = new BasicNameValuePair("code", RoomCode);
                BasicNameValuePair letterBasicNameValuePair = new BasicNameValuePair("letter", Letter);

                // We add the content that we want to pass with the POST request to as name-value pairs
                //Now we put those sending details to an ArrayList with type safe of NameValuePair
                List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
                nameValuePairList.add(nameBasicNameValuePair);
                nameValuePairList.add(countryBasicNameValuePair);
                nameValuePairList.add(colorBasicNameValuePair);
                nameValuePairList.add(fruitBasicNameValuePair);
                nameValuePairList.add(animalBasicNameValuePair);
                nameValuePairList.add(thingBasicNameValuePair);
                nameValuePairList.add(sportBasicNameValuePair);
                nameValuePairList.add(usernameBasicNameValuePair);
                nameValuePairList.add(codeBasicNameValuePair);
                nameValuePairList.add(letterBasicNameValuePair);

                try {
                    // UrlEncodedFormEntity is an entity composed of a list of url-encoded pairs.
                    //This is typically useful while sending an HTTP POST request.
                    UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);

                    // setEntity() hands the entity (here it is urlEncodedFormEntity) to the request.
                    httpPost.setEntity(urlEncodedFormEntity);

                    try {
                        // HttpResponse is an interface just like HttpPost.
                        //Therefore we can't initialize them
                        HttpResponse httpResponse = httpClient.execute(httpPost);

                        // According to the JAVA API, InputStream constructor do nothing.
                        //So we can't initialize InputStream although it is not an interface
                        InputStream inputStream = httpResponse.getEntity().getContent();

                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                        StringBuilder stringBuilder = new StringBuilder();

                        String bufferedStrChunk = null;

                        while ((bufferedStrChunk = bufferedReader.readLine()) != null) {
                            stringBuilder.append(bufferedStrChunk);
                        }

                        return stringBuilder.toString();

                    } catch (ClientProtocolException cpe) {
                        System.out.println("First Exception caz of HttpResponese :" + cpe);
                        cpe.printStackTrace();
                    } catch (IOException ioe) {
                        System.out.println("Second Exception caz of HttpResponse :" + ioe);
                        ioe.printStackTrace();
                    }

                } catch (UnsupportedEncodingException uee) {
                    System.out.println("An Exception given because of UrlEncodedFormEntity argument :" + uee);
                    uee.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                System.out.println("**********" + result + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                toResults(result);
            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(Name, Country, Color, Fruit, Animal,
                Thing, Sport);
    }

}