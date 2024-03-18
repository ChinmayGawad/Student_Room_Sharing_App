package com.pgshare.studentroomsharingapp.Adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pgshare.studentroomsharingapp.Message;
import com.pgshare.studentroomsharingapp.R;

import java.util.ArrayList;

public class MessageAdapt extends ArrayAdapter<Message> {
    private final Context mContext;
    private final ArrayList<Message> mMessages;

    public MessageAdapt(Context context, ArrayList<Message> messages) {
        super(context, 0, messages);
        mContext = context;
        mMessages = messages;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            if (getItemViewType(position) == 0) {
                listItem = inflater.inflate(R.layout.item_message_sent, parent, false);
            } else {
                listItem = inflater.inflate(R.layout.item_message_received, parent, false);
            }
        }

        Message message = mMessages.get(position);

        TextView messageTextView = listItem.findViewById(R.id.messageTextView);
        messageTextView.setText(message.getMessage());

        // Handle timestampTextView
        TextView timestampTextView = listItem.findViewById(R.id.timestampTextView);
        if (message.getTimestamp() != null) {
            timestampTextView.setVisibility(View.VISIBLE);
            timestampTextView.setText(message.getTimestamp());
        } else {
            timestampTextView.setVisibility(View.GONE);
        }

        return listItem;
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).isSentByUser() ? 0 : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
