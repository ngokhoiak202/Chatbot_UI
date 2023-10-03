package com.example.chatbot;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";

    private static final int SPEECH_REQUEST_CODE = 100;
    TextToSpeech textToSpeech;
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton, speakButton;

    List<Message> messageList;

    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageList = new ArrayList<>();
        anhxa();

        //setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        // quản lý việc sắp xếp các mục trong RecyclerView.
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.getDefault());
                }
            }
        });

        sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();
            SENT_BY_ME(question);
            addToChat(question, false);
            messageEditText.setText("");
            welcomeTextView.setVisibility(View.GONE);

        });

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need To Speak");
                try {
                    startActivityForResult(intent, SPEECH_REQUEST_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(MainActivity.this, "Sorry, Your Device not Supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        if (textToSpeech != null)
            textToSpeech.stop();
        super.onPause();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            ArrayList result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            result.get(0);
            String question = result.toString().trim();
            SENT_BY_ME(question);
            addToChat(question, true);
        }
    }

    void SENT_BY_ME(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, Message.SENT_BY_ME));
            }
        });
    }

    void addToChat(String message, boolean isSpeak) {
        Message userMessage = new Message(message, Message.SENT_BY_ME);
        OkHttpClient okHttpClient = new OkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://rasa-server-ngokhoiak202.cloud.okteto.net")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MessageSender messagerSender = retrofit.create(MessageSender.class);
        Call<ArrayList<BotResponse>> response = messagerSender.messageSender(userMessage);
        response.enqueue(new Callback<ArrayList<BotResponse>>() {

            @Override
            public void onResponse(@NonNull Call<ArrayList<BotResponse>> call, @NonNull Response<ArrayList<BotResponse>> response) {

                if (response.code() == 200 && response.body() != null) {
                    BotResponse answer = response.body().get(0);
                    messageList.add(new Message(answer.getText(), Message.SENT_BY_BOT));
                    if(isSpeak) {
                        textToSpeech.speak(answer.getText(), TextToSpeech.QUEUE_FLUSH, null);
                    }
                } else {
                    messageList.add(new Message("Không thể tải phản hồi ", Message.SENT_BY_BOT));
                }
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());

            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<BotResponse>> call, @NonNull Throwable t) {
                messageList.add(new Message("kiểm tra lại kết nối của bạn ", Message.SENT_BY_BOT));
            }
        });
    }

    private void anhxa() {
        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);
        speakButton = findViewById(R.id.speak_btn);

    }
}