package com.example.aimlchatbot;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.example.aimlchatbot.adapter.ChatMessageAdapter;
import com.example.aimlchatbot.pojo.ChatMessage;
import com.pradeep.bilometric.SimpleBiometric;
import com.pradeep.bilometric.interfaces.SimpleBiometricCallback;

import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.Graphmaster;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.PCAIMLProcessorExtension;
import org.alicebot.ab.Timer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SimpleBiometricCallback {


    private static final String TAG = "MainActivity";
    public static Chat chat;
    public Bot bot;
    ChatMessageAdapter mAdapter;
    private FragmentActivity mActivity;
    private ListView mListView;
    private ImageView mButtonSend;
    private EditText mEditTextMessage;
    private TextView textView;

    //check SD card availability
    public static boolean isSDCARDAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? true : false;
    }

    //Request and response of user and the bot
    public static void mainFunction(String[] args) {
        MagicBooleans.trace_mode = false;
        System.out.println("trace mode = " + MagicBooleans.trace_mode);
        Graphmaster.enableShortCuts = true;
        Timer timer = new Timer();
        String request = "Hello.";
        String response = chat.multisentenceRespond(request);

        System.out.println("Human: " + request);
        System.out.println("Robot: " + response);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        textView = findViewById(R.id.textView);
        handleBiometricAuthentication();
    }

    private void handleBiometricAuthentication() {
        //Using SimpleBiometric Library for biometric authentication.
        SimpleBiometric simpleBiometric = new SimpleBiometric(mActivity);
        if (simpleBiometric.canAuthenticate()) {//True means biometric is enabled and can be used.
            simpleBiometric.openBiometricDialog("SimpleBiometric Library", "Using SimpleBiometric Library for login into chatbot...", "Cancel", this);
        } else {//False means either biometric is not present in device or biometric is enabled in phone.
            textView.setVisibility(View.VISIBLE);
            textView.setText("Seems either biometric is not present in device or biometric is enabled in phone.");
            Toast.makeText(mActivity, "Seems either biometric is not present in device or biometric is enabled in phone.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleChatbotThings() {
        mListView = (ListView) findViewById(R.id.listView);
        mButtonSend = (ImageView) findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);
        mAdapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>());
        mListView.setAdapter(mAdapter);

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditTextMessage.getText().toString();
                //bot
                String response = chat.multisentenceRespond(mEditTextMessage.getText().toString());
                if (TextUtils.isEmpty(message)) {
                    return;
                }
                sendMessage(message);
                mimicOtherMessage(response);
                mEditTextMessage.setText("");
                mListView.setSelection(mAdapter.getCount() - 1);
            }
        });
        //checking SD card availablility
        boolean a = isSDCARDAvailable();
        //receiving the assets from the app directory
        AssetManager assets = getResources().getAssets();
        File jayDir = new File(Environment.getExternalStorageDirectory().toString() + "/hari/bots/Hari");
        boolean b = jayDir.mkdirs();
        if (jayDir.exists()) {
            //Reading the file
            try {
                for (String dir : assets.list("Hari")) {
                    File subdir = new File(jayDir.getPath() + "/" + dir);
                    boolean subdir_check = subdir.mkdirs();
                    for (String file : assets.list("Hari/" + dir)) {
                        File f = new File(jayDir.getPath() + "/" + dir + "/" + file);
                        if (f.exists()) {
                            continue;
                        }
                        InputStream in = null;
                        OutputStream out = null;
                        in = assets.open("Hari/" + dir + "/" + file);
                        out = new FileOutputStream(jayDir.getPath() + "/" + dir + "/" + file);
                        //copy file from assets to the mobile's SD card or any secondary memory
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //get the working directory
        MagicStrings.root_path = Environment.getExternalStorageDirectory().toString() + "/hari";
        System.out.println("Working Directory = " + MagicStrings.root_path);
        AIMLProcessor.extension = new PCAIMLProcessorExtension();
        //Assign the AIML files to bot for processing
        bot = new Bot("Hari", MagicStrings.root_path, "chat");
        chat = new Chat(bot);
        String[] args = null;
        mainFunction(args);
    }

    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true, false);
        mAdapter.add(chatMessage);

        //mimicOtherMessage(message);
    }

    private void mimicOtherMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, false, false);
        mAdapter.add(chatMessage);
    }

    private void sendMessage() {
        ChatMessage chatMessage = new ChatMessage(null, true, true);
        mAdapter.add(chatMessage);

        mimicOtherMessage();
    }

    private void mimicOtherMessage() {
        ChatMessage chatMessage = new ChatMessage(null, false, true);
        mAdapter.add(chatMessage);
    }

    //copying the file
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    public void onAuthenticationSuccess() {
        Log.d(TAG, "Success");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.GONE);
                handleChatbotThings();
            }
        });
    }

    @Override
    public void onAuthenticationError() {
        Log.d(TAG, "An unrecoverable error has been encountered and the operation is complete.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.VISIBLE);
                textView.setText("An unrecoverable error has been encountered and the operation is complete.");
                handleBiometricAuthentication();
            }
        });
    }

    @Override
    public void onAuthenticationFailed() {
        Log.d(TAG, "Error, Try again");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.VISIBLE);
                textView.setText("Error, Try again");
                handleBiometricAuthentication();
            }
        });
    }

    @Override
    public void onNegativeButtonClick() {
        Log.d(TAG, "Negative button is clicked");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.VISIBLE);
                textView.setText("Negative button is clicked");
                finish();
            }
        });
    }
}
