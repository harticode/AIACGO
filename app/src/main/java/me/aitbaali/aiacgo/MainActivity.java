package me.aitbaali.aiacgo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER =  2;



    private ListView mcardListView;
    private CardAdapter mCardAdapter;
    private FloatingActionButton madd;
    private ImageButton mPhotoPickerButton;
    private TextView mEmptyStateTextView;
    private View loadingIndicator;


    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNoteDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;

    private List<Card> cardnote;
    private String mUsername;
    private String phoneNum;
    private String date;
    private String place;
    private String time;
    private String price;
    private String photoUrl;
    private SharedPreferences sharedPref;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar pb;


    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    final boolean[] PhotoisSet = {false};

    ConnectivityManager cm;
    NetworkInfo activeNetwork;
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        // Initialize references to views
        mcardListView = (ListView) findViewById(R.id.messageListView);
        madd = (FloatingActionButton) findViewById(R.id.floatingActionButton2);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        loadingIndicator = findViewById(R.id.loading_indicator);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);



        cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText("No Internet Connection");

            //Todo : this is an illusion or is it? just fixed the illusion
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.i(TAG, "onRefresh called from Disconnected");
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                        }
                    }, 1300L);
                }
            });
        }else {

            //Initialize Firebase
            mFirebaseDatabase = FirebaseDatabase.getInstance();

            mFirebaseStorage = FirebaseStorage.getInstance();
            mPhotosStorageReference = mFirebaseStorage.getReference().child("photos");
            mNoteDatabaseReference = mFirebaseDatabase.getReference().child("note");

            mFirebaseAuth = FirebaseAuth.getInstance();
            sharedPref = getSharedPreferences("userinfo", this.MODE_PRIVATE);



            // Initialize Note ListView and its adapter
            cardnote = new ArrayList<>();
            mCardAdapter = new CardAdapter(this, R.layout.activityadddate, cardnote);
            mcardListView.setAdapter(mCardAdapter);

            //Todo : check Authentication
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        onSignedInInitialize(firebaseUser.getPhoneNumber());
                        if (sharedPref.getString("signIN", "").equals("true")) {

                            //todo : setusername
                            AlertDialog.Builder mBuilderuser = new AlertDialog.Builder(MainActivity.this);
                            mBuilderuser.setCancelable(false);
                            View mViewuser = getLayoutInflater().inflate(R.layout.addusername, null);

                            final EditText username = (EditText) mViewuser.findViewById(R.id.usernametxt);
                            Button usernameset = (Button) mViewuser.findViewById(R.id.setuser);
                            mPhotoPickerButton = (ImageButton) mViewuser.findViewById(R.id.photoPickerButton);
                            pb = (ProgressBar) mViewuser.findViewById(R.id.pb);


                            mBuilderuser.setView(mViewuser);
                            final AlertDialog mdialoguser = mBuilderuser.create();
                            mdialoguser.show();


                            // ImagePickerButton shows an image picker to upload a image for a message
                            mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setType("image/jpeg");
                                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                                }
                            });



                            usernameset.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!username.getText().toString().isEmpty()) {
                                        if (PhotoisSet[0]) {

                                            //todo : nevercomeback here;
                                            SharedPreferences.Editor SignedEditor = sharedPref.edit();
                                            SignedEditor.putString("signIN", "false");
                                            SignedEditor.apply();

                                            //todo : save data in the phone
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putString("infoUsername", username.getText().toString());
                                            editor.apply();
                                            mdialoguser.dismiss();
                                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Set your Profile Pictures please ", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "Fill in blanks - إملأ كل شيء", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }else {
                            //do nothing
                        }
                    } else {
                        loadingIndicator.setVisibility(View.GONE);
                        onSignedOutCleanup();
                        //if signed in
                        SharedPreferences.Editor SignedEditor = sharedPref.edit();
                        SignedEditor.putString("signIN", "true");
                        SignedEditor.apply();
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setLogo(R.drawable.hold)
                                        .setIsSmartLockEnabled(false)
                                        .setAvailableProviders(Arrays.asList(
                                                new AuthUI.IdpConfig.PhoneBuilder().build()))
                                        .build(),
                                RC_SIGN_IN);
                    }
                }
            };


            //todo : Etape 2

            madd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                    View mView = getLayoutInflater().inflate(R.layout.addcard, null);
                    final EditText Vplace = (EditText) mView.findViewById(R.id.place);
                    final TextView Vdate = (TextView) mView.findViewById(R.id.date);
                    final TextView Vtime = (TextView) mView.findViewById(R.id.time);
                    Spinner Vsp = (Spinner) mView.findViewById(R.id.spinner);
                    Button msave = (Button) mView.findViewById(R.id.save);
                    mBuilder.setView(mView);
                    final AlertDialog mdialog = mBuilder.create();
                    mdialog.show();

                    final String[] gdate = new String[1];
                    final String[] gtime = new String[1];
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this,
                            R.array.options, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    Vsp.setAdapter(adapter);
                    Vsp.setOnItemSelectedListener(MainActivity.this);


                    //todo : setdate
                    Vdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Calendar cal = Calendar.getInstance();
                            int year = cal.get(Calendar.YEAR);
                            int month = cal.get(Calendar.MONTH);
                            //int monthnow = cal.get(Calendar.MONTH);
                            int day = cal.get(Calendar.DAY_OF_MONTH);
                            //final int today = cal.get(Calendar.DAY_OF_MONTH);

                            DatePickerDialog dialog = new DatePickerDialog(
                                    MainActivity.this,
                                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                    mDateSetListener,
                                    year, month, day);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.show();
                        }
                    });

                    mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            month = month + 1;
                            Calendar cl = Calendar.getInstance();
                            int monthnow = cl.get(Calendar.MONTH);
                            final int today = cl.get(Calendar.DAY_OF_MONTH);
                            if (monthnow + 1 < month) {
                                String mth = moth(month);
                                gdate[0] = day + " " + mth;
                                Vdate.setText(gdate[0]);
                                Vdate.setTextColor(Color.BLACK);
                                date = gdate[0];
                            } else if (day >= today && monthnow + 1 == month) {
                                String mth = moth(month);
                                gdate[0] = day + " " + mth;
                                Vdate.setText(gdate[0]);
                                Vdate.setTextColor(Color.BLACK);
                                date = gdate[0];
                            } else {
                                Toast.makeText(MainActivity.this, "Change the date to a Newer one", Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    //todo : settime
                    Vtime.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Calendar c = Calendar.getInstance();
                            int hour = c.get(Calendar.HOUR_OF_DAY);
                            int minute = c.get(Calendar.MINUTE);
                            TimePickerDialog tdialog =
                                    new TimePickerDialog(MainActivity.this, mTimeSetListener,
                                            hour, minute, true);
                            tdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            tdialog.show();
                        }
                    });
                    mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            gtime[0] = hourOfDay + ":" + minute;
                            Vtime.setText(gtime[0]);
                            Vtime.setTextColor(Color.BLACK);
                            time = gtime[0];
                        }
                    };


                    //todo : save to database
                    msave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            place = Vplace.getText().toString();
                            Calendar cal = Calendar.getInstance();
                            int year = cal.get(Calendar.YEAR);
                            if (!place.isEmpty() && price != null && date != null && time != null) {

                                //
                                DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                                Date dates = null;
                                try {
                                    dates = dateFormat.parse(gdate[0]+ " " + year);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                long unixTime = (long)dates.getTime()/1000;
                                User user = new User(sharedPref.getString("infoUsername",
                                        "doesNotexist"), phoneNum, sharedPref.getString("infoUserPhoto", ""));
                                Card card = new Card(date, place, price, time, user, unixTime);
                                mNoteDatabaseReference.push().setValue(card);
                                mdialog.dismiss();

                            } else {
                                Toast.makeText(MainActivity.this, "Fill in blanks - إملأ كل شيء", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });

            //Todo : this is an illusion or is it? just fixed the illusion
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.i(TAG, "onRefresh called from MainActivity");
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dettacheDatabaseReadListener();
                            mCardAdapter.clear();
                            attacheDatabaseReadListener();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }, 1300L);
                    //startActivity(new Intent(MainActivity.this, MainActivity.class));

                }
             });



            mcardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String name = cardnote.get(position).getUser().getNameuser();
                    final String phoneNum = cardnote.get(position).getUser().getPhoneNumber();
                    String profilPicCall = cardnote.get(position).getUser().getProfilePic();

                    AlertDialog.Builder mBuildercall = new AlertDialog.Builder(MainActivity.this);
                    View mViewcall = getLayoutInflater().inflate(R.layout.calltheuser, null);

                    TextView usernamecall = (TextView) mViewcall.findViewById(R.id.textuser);
                    ImageButton profilecall = (ImageButton) mViewcall.findViewById(R.id.callbtn);
                    ImageButton profileSms = (ImageButton) mViewcall.findViewById(R.id.smsbtn);
                    ImageView closecall = (ImageView) mViewcall.findViewById(R.id.close);
                    CircleImageView profilcall = (CircleImageView) mViewcall.findViewById(R.id.photouser);

                    usernamecall.setText(name);
                    Glide.with(profilcall.getContext())
                            .load(profilPicCall)
                            .into(profilcall);

                    mBuildercall.setView(mViewcall);
                    final AlertDialog mdialogcall = mBuildercall.create();
                    mdialogcall.show();

                    profilecall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNum, null));
                            startActivity(intent);
                        }
                    });

                    profileSms.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNum, null)));
                        }
                    });

                    closecall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mdialogcall.dismiss();
                        }
                    });
                }
            });


            //Todo : improvise adapt overcome
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if(cardnote.isEmpty()){
                        loadingIndicator.setVisibility(View.GONE);
                        mEmptyStateTextView.setText("No offers Available for Now, add yours :)");
                    }
                }
            }, 6000L);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
                if(resultCode == RESULT_OK){
                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show();

                }else if(resultCode == RESULT_CANCELED){
                    Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }else if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
                Uri selectedImageUri = data.getData();
                StorageReference photoRef = mPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

                photoRef.putFile(selectedImageUri).addOnSuccessListener(this,
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                PhotoisSet[0] = true;
                                //todo : save Photo URl in the phone
                                SharedPreferences.Editor photoEditor = sharedPref.edit();
                                photoEditor.putString("infoUserPhoto", downloadUri.toString());
                                photoEditor.apply();
                                pb.setProgress(100);
                                Toast.makeText(MainActivity.this, "Done Uploading", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        pb.setProgress(progress);
                        Toast.makeText(MainActivity.this, "progress = " + progress, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }


    private String moth(int month) {
        String monthString;
        switch (month) {
            case 1:  monthString = "Jan";
                break;
            case 2:  monthString = "Feb";
                break;
            case 3:  monthString = "Mar";
                break;
            case 4:  monthString = "Apr";
                break;
            case 5:  monthString = "May";
                break;
            case 6:  monthString = "Jun";
                break;
            case 7:  monthString = "Jul";
                break;
            case 8:  monthString = "Aug";
                break;
            case 9:  monthString = "Sep";
                break;
            case 10: monthString = "Oct";
                break;
            case 11: monthString = "Nov";
                break;
            case 12: monthString = "Dec";
                break;
            default: monthString = "Invalid month";
                break;
        }
        return monthString;
    }


    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        //if signed out
        SharedPreferences.Editor SignedEditor = sharedPref.edit();
        SignedEditor.putString("signIN", "true");
        SignedEditor.apply();
        mCardAdapter.clear();
    }

    private void onSignedInInitialize(String phone) {
        phoneNum = phone;
        attacheDatabaseReadListener();
    }

    private void attacheDatabaseReadListener() {
        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Card cardNoteData = dataSnapshot.getValue(Card.class);
                    //todo: to filter the old ones
                    if(cardNoteData!=null){
                        if(cardNoteData.getTimeUnix()>=(System.currentTimeMillis() / 1000L)- 86400){
                            mCardAdapter.add(cardNoteData);
                            //Todo : sort Data by UnixTime
                            Collections.sort(cardnote);
                            //Hide Loading and text
                            loadingIndicator.setVisibility(View.GONE);
                            mEmptyStateTextView.setText("");
                        }
                   }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mNoteDatabaseReference.addChildEventListener(mChildEventListener);
        }

    }


    private void dettacheDatabaseReadListener(){
        if(mChildEventListener != null) {
            mNoteDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:{
                AuthUI.getInstance().signOut(this);
                return true;}
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
            if(!isConnected){
            }else {
            if(mChildEventListener != null) {
                mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            }
            dettacheDatabaseReadListener();
            mCardAdapter.clear();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isConnected){
        }else {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
            attacheDatabaseReadListener();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!isConnected){
        }else {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        price = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        price = null;
    }
}
