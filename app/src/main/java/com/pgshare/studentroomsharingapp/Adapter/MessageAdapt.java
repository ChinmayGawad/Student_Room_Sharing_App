package com.pgshare.studentroomsharingapp.Adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pgshare.studentroomsharingapp.Message;
import com.pgshare.studentroomsharingapp.R;

import java.util.ArrayList;

   public class MessageAdapt extends ArrayAdapter<Message> {
    private final Context mContext;
    private final ArrayList<Message> mMessages;
    private DatabaseReference mDatabase;

    public MessageAdapt(Context context, ArrayList<Message> messages) {
        super(context, 0, messages);
        mContext = context;
        mMessages = messages;
        mDatabase = FirebaseDatabase.getInstance().getReference("messages");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            listItem = inflater.inflate(R.layout.list_item_message, parent, false);
        }

        Message message = mMessages.get(position);

        TextView messageTextView = listItem.findViewById(R.id.messageTextView);
        messageTextView.setText(message.getMessage());

        // Set other views if needed

        return listItem;
    }

    // Method to add a message to the Firebase Realtime Database
    public void addMessageToDatabase(Message message) {
        // Push the message to the "messages" node in the database
        String messageId = mDatabase.push().getKey();
        mDatabase.child(messageId).setValue(message);
    }
}
