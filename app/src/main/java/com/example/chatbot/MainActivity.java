package com.example.chatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

//    public static final MediaType JSON
//            = MediaType.get("application/json; charset=utf-8");
//    OkHttpClient client = new OkHttpClient();
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;

    List<Message> messageList;

    MessageAdapter messageAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageList = new ArrayList<>();
        anhxa();

        //setup recycler view
        messageAdapter = new MessageAdapter (messageList);
        recyclerView.setAdapter (messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager( this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager (llm);


        sendButton.setOnClickListener((v)->{
            String question = messageEditText.getText().toString().trim();
            addToChat(question, Message. SENT_BY_ME);
            messageEditText.setText("");
            welcomeTextView.setVisibility(View.GONE);
        });
    }

    void addToChat(String message, String sentBy) {
        Message userMessage = new Message(message, Message.SENT_BY_ME);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });

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
                    BotResponse question = response.body().get(0);
                    messageList.add(new Message(question.getText() ,Message.SENT_BY_BOT ));
                    messageAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                } else{
                    ;
                    messageList.add(new Message("Không thể tải phản hồi ",Message.SENT_BY_BOT));

                }
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<BotResponse>> call, @NonNull Throwable t) {
                messageList.add(new Message("kiểm tra lại kết nối của bạn ",Message.SENT_BY_BOT));
            }
        });
    }

    private void anhxa() {
        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

    }
}