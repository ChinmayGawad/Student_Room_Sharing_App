package com.pgshare.studentroomsharingapp

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pgshare.studentroomsharingapp.Adapter.ImageAdapter
import com.pgshare.studentroomsharingapp.Adapter.Room

class RoomDetailsActivity : AppCompatActivity() {
    protected var room: Room? = null
    private var bookRoomButton: Button? = null
    private var isRoomBooked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_details)
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.C_color)))

        // Initialize views
        val roomNameTextView = findViewById<TextView>(R.id.roomNameTextView)
        val locationTextView = findViewById<TextView>(R.id.locationTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)
        val priceTextView = findViewById<TextView>(R.id.priceTextView)
        val recyclerView = findViewById<RecyclerView>(R.id.imageRecyclerView)
        val chatWithRoomMate = findViewById<Button>(R.id.ChatWithRoomMate)
        bookRoomButton = findViewById<Button>(R.id.bookRoomButton)

        // Set layout manager for RecyclerView
        recyclerView.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )

        // Get the Room object from the intent
        room = getIntent().getParcelableExtra<Room?>("Rooms")

        // Check if room object is not null
        if (room != null) {
            // Set room details
            roomNameTextView.setText(room!!.roomName)
            locationTextView.setText(room!!.location)
            descriptionTextView.setText(room!!.description)
            priceTextView.setText(room!!.getFormatPrice())

            // Load images into RecyclerView
            val imageUrls: MutableList<String?>? = room!!.imageUrls
            if (imageUrls != null && !imageUrls.isEmpty()) {
                val imageAdapter = ImageAdapter()
                recyclerView.setAdapter(imageAdapter)
                imageAdapter.setImageUrls(imageUrls)
            }

            // Retrieve booking status of the room from the database
            // Check if the room is booked
            isRoomBooked = room!!.isRoomBooked // Example: Retrieve booked status from Room object
            if (isRoomBooked) {
                // If room is booked, disable the book button and display a message
                bookRoomButton!!.setText("Room Booked")
                bookRoomButton!!.setEnabled(false)
            }
        } else {
            // Handle case where room object is null
            Toast.makeText(this, "Failed to load room details", Toast.LENGTH_SHORT).show()
        }

        // Set onClickListener for chat button
        chatWithRoomMate.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("roomId", room!!.id)
            startActivity(intent)
        })
    }

    // Method to handle booking of the room
    fun bookRoom(view: View?) {
        val intent = Intent(this@RoomDetailsActivity, PaymentActivity::class.java)
        startActivity(intent)
        finish()
    }
}