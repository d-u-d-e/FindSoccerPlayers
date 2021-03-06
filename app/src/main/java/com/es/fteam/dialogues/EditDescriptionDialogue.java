package com.es.fteam.dialogues;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.es.fteam.R;

public class EditDescriptionDialogue extends DialogFragment {

    public interface onDescriptionListener {
        void onDescriptionSet(String desc);
    }

    private EditText editText;
    private onDescriptionListener mListener;
    private String prevDesc;
    private String title;

    public EditDescriptionDialogue(String title, onDescriptionListener listener, String prevDesc){
        mListener = listener;
        this.prevDesc = prevDesc;
        this.title = title;
    }

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_description_dialogue, null);
        editText = view.findViewById(R.id.ET_description);
        editText.setText(prevDesc);

        builder.setView(view).setTitle(title)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDescriptionSet(editText.getText().toString());
                    }
                });

        return builder.create();
    }
}