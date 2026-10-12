package com.example.vegitrp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vegitrp.R;
import com.example.vegitrp.adapters.ChatAdapter;
import com.example.vegitrp.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity {

    private RecyclerView rvChatMessages;
    private EditText etChatMessage;
    private ImageButton btnSendChat;
    private View layoutEmptyChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList;

    private String orderId;
    private String partnerId;
    private String currentUserId;
    private DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.chatToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.chat_toolbar_title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            orderId = getIntent().getStringExtra("orderId");
        }
        partnerId = getIntent().getStringExtra("PARTNER_ID");
        if (partnerId == null) {
            partnerId = getIntent().getStringExtra("partnerId");
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvChatMessages = findViewById(R.id.rvChatMessages);
        etChatMessage = findViewById(R.id.etChatMessage);
        btnSendChat = findViewById(R.id.btnSendChat);
        layoutEmptyChat = findViewById(R.id.layoutEmptyChat);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatList, currentUserId);
        rvChatMessages.setAdapter(chatAdapter);

        if (orderId != null) {
            chatRef = FirebaseDatabase.getInstance().getReference("chats").child(orderId);
            loadMessages();
        } else {
            Toast.makeText(this, "Order ID missing", Toast.LENGTH_SHORT).show();
        }

        btnSendChat.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ChatMessage msg = data.getValue(ChatMessage.class);
                    if (msg != null) {
                        chatList.add(msg);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                if (layoutEmptyChat != null) {
                    layoutEmptyChat.setVisibility(chatList.isEmpty() ? View.VISIBLE : View.GONE);
                }
                if (chatList.size() > 0) {
                    rvChatMessages.smoothScrollToPosition(chatList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = etChatMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String msgId = chatRef.push().getKey();
        if (msgId != null) {
            ChatMessage msg = new ChatMessage(msgId, currentUserId, partnerId, orderId, text, System.currentTimeMillis());
            chatRef.child(msgId).setValue(msg);
            etChatMessage.setText("");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
