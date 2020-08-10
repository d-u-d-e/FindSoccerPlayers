package com.es.findsoccerplayers.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.models.Message;
import com.es.findsoccerplayers.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> chats;
    private static int MSG_TYPE_LEFT = 0;
    private static int MSG_TYPE_RIGHT = 1;

    public MessageAdapter(Context c, List<Message> chats){
        context = c;
        this.chats = chats;
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_LEFT)
            view = LayoutInflater.from(context).inflate(R.layout.frag_chat_message_left, parent, false);
        else
            view = LayoutInflater.from(context).inflate(R.layout.frag_chat_message_right, parent, false);
        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
        Message m = chats.get(position);
        holder.message.setText(m.getText());

        String dateStr = DateUtils.formatDateTime(context, m.getTimestamp(), DateUtils.FORMAT_ABBREV_MONTH |
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        holder.timestamp.setText(dateStr);
        holder.sender.setText(m.getSenderUsername());
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chats.get(position).getSenderID().equals(currentUser.getUid()))
            return MSG_TYPE_RIGHT;
        else
            return MSG_TYPE_LEFT;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView message;
        TextView timestamp;
        TextView sender;

        ViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chat_message);
            timestamp = itemView.findViewById(R.id.chat_timestamp);
            sender = itemView.findViewById(R.id.chat_user);
        }
    }
}
