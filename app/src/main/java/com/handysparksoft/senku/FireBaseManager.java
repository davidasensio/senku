package com.handysparksoft.senku;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by davasens on 3/14/2015.
 */
public class FireBaseManager {
    private final static String FIREBASE_URL_SENKU = "https://senku.firebaseio.com/";
    private final static String FIREBASE_URL_SENKU_SCORES = "https://senku.firebaseio.com/scores/";
    Firebase rootRef;
    private Context context;

    public void initFireBase(Context context) {
        this.context = context;
        Firebase.setAndroidContext(context);
        init();
    }

    private void init() {
        rootRef = new Firebase(FIREBASE_URL_SENKU);

        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }


    public void storeUserScoreInFireBase(String userAccount, String maxScore) {
        if (rootRef == null) {
            init();
        }
        rootRef.child("user/scores/"+userAccount).setValue(maxScore);
        //rootRef.updateChildren();
    }

    public void getUsersScoreFromFireBase(Context context) {
        this.context = context;
        final Context ctx = this.context;

        Firebase scoresRef = new Firebase(FIREBASE_URL_SENKU);

        //Query queryRef = scoresRef.orderByValue();
        scoresRef.child("user/scores/test").setValue("0");
        scoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            //scoresRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<String> scores = new ArrayList<String>();

                Object object = snapshot.getValue();
                if (object != null) {
                    Map<String, Object> scoresMap = (Map<String, Object>) snapshot.child("user/scores/").getValue();
                    Iterator<String> it = scoresMap.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        String value = (String)scoresMap.get(key);

                        //if (key != "test") {
                        scores.add(value +" - " +key);
                        //}
                    }
                    Collections.sort(scores);
                    Collections.reverse(scores);
                }
                System.out.println(object);

                Bundle bundle = new Bundle();
                bundle.putSerializable("array_list", scores);

                Intent intent = new Intent(ctx, ScoreScreen.class);
                intent.putExtras(bundle);
                ctx.startActivity(intent);

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        scoresRef.child("user/scores/test").removeValue();
    }


}
