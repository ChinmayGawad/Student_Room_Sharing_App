package com.pgshare.studentroomsharingapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.pgshare.studentroomsharingapp.Adapter.MessageAdapter
import com.pgshare.studentroomsharingapp.databinding.ActivityChatBinding
import com.pgshare.studentroomsharingapp.model.Message
import com.pgshare.studentroomsharingapp.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    private val viewModel = ChatViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val receiverId = intent.getStringExtra("RECEIVER_ID") ?: ""
        val roomId = intent.getStringExtra("ROOM_ID") ?: "UnknownRoom"
        val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (receiverId.isEmpty() || senderId.isEmpty()) {
            Toast.makeText(this, "Session expired. Please go back.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        chatAdapter.setReceiverId(receiverId)
        observeUiState()

        viewModel.initialize(senderId, receiverId, roomId)

        binding.btnSend.setOnClickListener {
            val text = binding.etMessageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.etMessageInput.text?.clear()
            }
        }

        binding.topToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        messageList = ArrayList()
        chatAdapter = MessageAdapter(messageList, FirebaseAuth.getInstance().currentUser?.uid ?: "")
        binding.messageList.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    messageList.clear()
                    messageList.addAll(state.messages)
                    chatAdapter.notifyDataSetChanged()
                    chatAdapter.setReceiverName(state.receiverName)
                    binding.tvChatName.text = state.receiverName

                    if (messageList.isNotEmpty()) {
                        binding.messageList.scrollToPosition(messageList.size - 1)
                    }
                }
            }
        }
    }
}
