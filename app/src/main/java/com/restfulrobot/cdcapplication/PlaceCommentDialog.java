package com.restfulrobot.cdcapplication;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.restfulrobot.cdcapplication.objects.PlaceItem;

public class PlaceCommentDialog extends DialogFragment {

    private Button commitBtn;
    private EditText commentEditText;
    private String previousComment;
    private boolean activeItemFlag;
    private PlaceItem placeItem;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle("Информация");
        View v = inflater.inflate(R.layout.place_comment_dialog, null);
        commentEditText = (EditText) v.findViewById(R.id.commentEditText);
        commentEditText.setText(placeItem.getComment());
        commitBtn = (Button) v.findViewById(R.id.commitBtn);
        commitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (commentEditText.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Пустой комментарий недопустим!", Toast.LENGTH_SHORT).show();
                } else {
                    String comment = commentEditText.getText().toString();
                    placeItem.setComment(comment);
                    ((MainActivity) getActivity()).changeCommentQuery(placeItem, activeItemFlag);
                }
            }
        });

        return v;
    }

    // принимаем текущую выбранную точку для дальнейшей обработки, флаг показывает выбран ли текщий элемент в списке
    public void setPlaceItemForChanging(PlaceItem selectedPlaceItem, boolean activeItemFlag) {
        this.activeItemFlag = activeItemFlag;
        this.placeItem = selectedPlaceItem;
    }
}
