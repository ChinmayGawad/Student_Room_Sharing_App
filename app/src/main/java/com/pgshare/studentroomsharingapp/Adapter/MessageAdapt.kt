package com.pgshare.studentroomsharingapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pgshare.studentroomsharingapp.R
import java.util.Locale

class MessageAdapt(private val mContext: Context, private val mMessages: ArrayList<Message>) :
    ArrayAdapter<Message?>(
        mContext, 0,
        mMessages as MutableList<Message?>
    ) {
    private var mDatabaseReference: DatabaseReference? = null
    private var currentRoom: String? = null // To store the currently selected room

    fun updateRoom(roomId: String?) {
        currentRoom = roomId
        mMessages.clear() // Clear existing messages
        if (currentRoom != null && !currentRoom!!.isEmpty()) {
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms")
                .child(currentRoom!!)
            mDatabaseReference!!.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val message = dataSnapshot.getValue<Message?>(Message::class.java)
                    mMessages.add(message!!)
                    notifyDataSetChanged()
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                } // Other event listener methods (onChildChanged, onChildRemoved, etc.)
            })
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        val message = mMessages.get(position)

        if (listItem == null) {
            val inflater = LayoutInflater.from(mContext)
            if (getItemViewType(message.email) == 0) {
                listItem = inflater.inflate(R.layout.item_message_sent, parent, false)
            } else {
                listItem = inflater.inflate(R.layout.item_message_received, parent, false)
                val textViewUserInitial = listItem.findViewById<TextView>(R.id.textViewUserInitial)
                val textViewUserName = listItem!!.findViewById<TextView>(R.id.textViewUserName)
                val userInitial =
                    message.username!!.uppercase(Locale.getDefault()).get(0).toString()

                textViewUserInitial!!.text = userInitial
                textViewUserName!!.text = message.username
            }
        }


        val messageTextView = listItem!!.findViewById<TextView>(R.id.messageTextView)


        messageTextView!!.text = message.message


        // Handle timestampTextView
//        TextView timestampTextView = listItem.findViewById(R.id.timestampTextView);
//        if (message.getTimestamp() != null) {
//            timestampTextView.setVisibility(View.VISIBLE);
//            timestampTextView.setText(message.getTimestamp());
//        } else {
//            timestampTextView.setVisibility(View.GONE);
//        }
        return listItem
    }


    fun getItemViewType(email: String?): Int {
//        return mMessages.get(position).isSentByUser() ? 0 : 1;
        val user = FirebaseAuth.getInstance().currentUser
        //        Log.d("temp_debug", user.getEmail() + "::" + email + "::" + (user.getEmail().toString().equals(email.toString())));
        return if (user!!.email == email) 0 else 1
    }

    override fun getViewTypeCount(): Int {
        return 2
    }
}
