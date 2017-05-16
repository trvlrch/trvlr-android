package ch.trvlr.trvlr.adapter;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import ch.trvlr.trvlr.model.Message;
import ch.trvlr.trvlr.R;

public class MessageAdapter extends BaseAdapter {
    private List<Message> messages;
    private AppCompatActivity context;

    /**
     * Constructor for MessageAdapter
     *
     * @param context AppCompatActivity
     * @param messages List
     */
    public MessageAdapter(AppCompatActivity context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    /**
     * Get number of messages
     *
     * @return int
     */
    @Override
    public int getCount() {
        return messages.size();
    }

    /**
     * Get the message by index
     * @param i int
     * @return Message
     */
    @Override
    public Message getItem(int i) {
        return messages.get(i);
    }

    /**
     * Get the message id by index
     *
     * @param i int
     * @return long
     */
    @Override
    public long getItemId(int i) {
        return getItem(i).getId();
    }

    /**
     * Create the view for a message by index
     *
     * @param i int
     * @param view View
     * @param viewGroup ViewGroup
     * @return
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        Message message = getItem(i);
        LayoutInflater vi = LayoutInflater.from(viewGroup.getContext());

        if (view == null) {
            view = vi.inflate(R.layout.chat_bubble, null);
            holder = createViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }

        setAlignment(holder, message.isMyMessage());
        holder.textMessage.setText(message.getText());
        holder.textAuthor.setText(message.getAuthor());
        holder.textTime.setText(new SimpleDateFormat("HH:mm").format(message.getTimestamp()));

        return view;
    }

    /**
     * Add a message
     *
     * @param message Message
     */
    public void add(Message message) {
        this.messages.add(message);
    }


    private void setAlignment(ViewHolder holder, boolean isMe) {
        if (isMe) {
            // Set 9 patch image as background and align chat bubble to the right
            holder.contentWithBG.setBackgroundResource(R.drawable.bubble_green);
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            // Align text in chat bubble and set text color to white
            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.textMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.textMessage.setLayoutParams(layoutParams);
            holder.textMessage.setTextColor(ContextCompat.getColor(context, R.color.white));

            // Hide author and change height for a more appropriate padding
            holder.textAuthor.setVisibility(View.INVISIBLE);
            layoutParams = (LinearLayout.LayoutParams) holder.textAuthor.getLayoutParams();
            layoutParams.height = 10;
            holder.textAuthor.setLayoutParams(layoutParams);

            // Change color of time
            holder.textTime.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            // Set 9 patch image as background and align chat bubble to the left
            holder.contentWithBG.setBackgroundResource(R.drawable.bubble_gray);
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            // Align text in chat bubble and set text color to black
            RelativeLayout.LayoutParams lp =
                    (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.textMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.textMessage.setLayoutParams(layoutParams);
            holder.textMessage.setTextColor(ContextCompat.getColor(context, R.color.black_text));

            // Hide author and make sure the textView has enough space for the text
            holder.textAuthor.setVisibility(View.VISIBLE);
            layoutParams = (LinearLayout.LayoutParams) holder.textAuthor.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.textAuthor.setLayoutParams(layoutParams);

            // Change color of time
            holder.textTime.setTextColor(Color.GRAY);
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.textMessage = (TextView) v.findViewById(R.id.textMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.textAuthor = (TextView) v.findViewById(R.id.textAuthor);
        holder.textTime = (TextView) v.findViewById(R.id.textTime);
        return holder;
    }

    private class ViewHolder {
        public TextView textMessage;
        public TextView textAuthor;
        public TextView textTime;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }
}
