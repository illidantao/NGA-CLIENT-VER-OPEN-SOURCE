package com.noname.listener;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

import sp.phone.common.PhoneConfiguration;
import sp.phone.util.StringUtils;
import com.noname.gson.parse.NonameReadBody;
import com.noname.gson.parse.NonameReadResponse;
import sp.phone.util.FunctionUtils;
import sp.phone.common.PhoneConfiguration;
import sp.phone.util.StringUtils;

public class MyListenerForNonameReply implements OnClickListener {
    int mPosition;
    Context mcontext;
    NonameReadResponse mData;
    private View button;
    private long lastTimestamp = 0;

    public MyListenerForNonameReply(int inPosition, Context incontext, NonameReadResponse data) {
        mPosition = inPosition;
        mcontext = incontext;
        mData = data;
    }

    @Override
    public void onClick(View v) {

        if (System.currentTimeMillis() - this.lastTimestamp <= 3000) {
            return;
        } else {
            this.lastTimestamp = System.currentTimeMillis();
        }

        this.button = v;
        this.button.setEnabled(false);

        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPostExecute(Void result) {
                MyListenerForNonameReply.this.button.setEnabled(true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                Intent intent = new Intent();
                StringBuffer postPrefix = new StringBuffer();
                String mention = null;

                final String quote_regex = "\\[quote\\]([\\s\\S])*\\[/quote\\]";
                final String replay_regex = "\\[b\\]Reply to \\[pid=\\d+,\\d+,\\d+\\]Reply\\[/pid\\] Post by .+?\\[/b\\]";
                NonameReadBody row = mData.data.posts[mPosition];
                String content = row.content;
                final String name = row.hip;
                content = content.replaceAll(quote_regex, "");
                content = content.replaceAll(replay_regex, "");
                final long longposttime = row.ptime;
                String postTime = "";
                if (longposttime != 0) {
                    postTime = StringUtils.timeStamp2Date1(String.valueOf(longposttime));
                }
                final String tidStr = String.valueOf(mData.data.tid);
                content = FunctionUtils.checkContent(content);
                content = StringUtils.unEscapeHtml(content);
                mention = name;
                postPrefix.append("[quote]");
                postPrefix.append("[b]Post by [hip]");
                postPrefix.append(name);
                postPrefix.append("[/hip] (");
                postPrefix.append(postTime);
                postPrefix.append("):[/b]\n");
                postPrefix.append(content);
                postPrefix.append("[/quote]\n");
                if (!StringUtils.isEmpty(mention))
                    intent.putExtra("mention", mention);
                intent.putExtra("prefix",
                        StringUtils.removeBrTag(postPrefix.toString()));
                if (tidStr != null)
                    intent.putExtra("tid", tidStr);
                intent.putExtra("action", "reply");
                intent.setClass(
                        mcontext,
                        PhoneConfiguration.getInstance().nonamePostActivityClass);
                mcontext.startActivity(intent);
                return null;
            }
        }).execute();
    }
}
