package alone.klp.kr.hs.mirim.alone.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import alone.klp.kr.hs.mirim.alone.CommunityActivity;
import alone.klp.kr.hs.mirim.alone.CommunityDetailActivity;
import alone.klp.kr.hs.mirim.alone.CreateMemoActivity;
import alone.klp.kr.hs.mirim.alone.MainActivity;
import alone.klp.kr.hs.mirim.alone.R;
import alone.klp.kr.hs.mirim.alone.adapter.CommunityAdapter;
import alone.klp.kr.hs.mirim.alone.model.LibraryItem;
import alone.klp.kr.hs.mirim.alone.model.Member;

/*import static alone.klp.kr.hs.mirim.alone.MainActivity.editSearch;*/
import static alone.klp.kr.hs.mirim.alone.MainActivity.imm;
import static alone.klp.kr.hs.mirim.alone.SignInActivity.var;

public class CommunityFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    // Firebase 인스턴스 변수
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ChildEventListener mChild;

    // 구글
    private GoogleApiClient mGoogleApiClient;

    private ArrayList<Member> items;
    private ArrayList<Member> searchlist;
    private ArrayList<String> keys;
    public CommunityAdapter communityAdapter;
    private ListView listView;

    private SimpleDateFormat format = new SimpleDateFormat("YYYY년 MM월 dd일");

    private RelativeLayout layout;
    private Button btn_add;

    // 사용자 이름과 이메일, 사진
    private String mUsername;
    private String mEmail;
    private String mPhotoUrl;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }//구글 인증이 안되었을 떄

    public CommunityFragment() {
        var.isLibrary = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        items = new ArrayList<>();
        searchlist = new ArrayList<Member>();
        communityAdapter = new CommunityAdapter(items);
        ((MainActivity) getActivity()).com_items = items;
        ((MainActivity) getActivity()).com_search = searchlist;
        ((MainActivity) getActivity()).communityAdapter = communityAdapter;
//        items = (ArrayList<Member>) getActivity().getIntent().getSerializableExtra("community_list");
//        searchlist = (ArrayList<Member>) getActivity().getIntent().getSerializableExtra("community_search");
        keys = new ArrayList<>();
        listView = view.findViewById(R.id.listview);
        btn_add = view.findViewById(R.id.btn_add);
        layout = view.findViewById(R.id.layout_community);
        /*layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
            }
        });*/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), CommunityDetailActivity.class);
                intent.putExtra("mUsername", mUsername);
                intent.putExtra("mEmail", mEmail);
                intent.putExtra("mPhotoUrl", mPhotoUrl);
                intent.putExtra("PostName", items.get(position).getName());
                intent.putExtra("PostEmail", items.get(position).getEmail());
                intent.putExtra("PostDate", format.format(items.get(position).getDate()));
                intent.putExtra("PostPhotoUrl",items.get(position).getPhotoUrl());
                intent.putExtra("PostText", items.get(position).getText());
                intent.putExtra("PostKey", keys.get(keys.size() - position - 1));
                startActivity(intent);
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mUsername = mFirebaseUser.getDisplayName();
        mEmail = mFirebaseUser.getEmail();
        mEmail = "@" + mEmail.substring(0, mEmail.indexOf('@')); // 이메일 '@asdf' 형태로 나타내기
        if (mFirebaseUser.getPhotoUrl() != null) {
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppInvite.API)
                .build();

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateMemoActivity.class);
                intent.putExtra("mUsername", mUsername);
                intent.putExtra("mEmail", mEmail);
                intent.putExtra("mPhotoUrl", mPhotoUrl);

                startActivity(intent);
            }//onClick
        });//btn_input.setOnClickListener

        // Firebase Remote Config 초기화
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Firebase Remote Config 설정
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();

        // 인터넷 연결이 안 되었을 때 기본 값 정의
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("message_length", 10L);

        // 설정과 기본 값 설정
        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        items.clear();
        searchlist.clear();
        keys.clear();
        getDatabase();
        var.isLibrary = false;
    }

    //함수 getDatabase()
    private void getDatabase() {
        mDatabase = FirebaseDatabase.getInstance();

        mReference = mDatabase.getReference("messages");

        mChild = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Member member = dataSnapshot.getValue(Member.class);
                keys.add(dataSnapshot.getKey());
                createMemberList(member);
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
        };//mchild
        mReference.addChildEventListener(mChild);
    }//getDatabase()

    private void  createMemberList(Member member){
        items.add(member);
        searchlist.add(member);
        Log.d("값 확인", member.getText() + " " + items.size());
        communityAdapter = new CommunityAdapter(items);
        listView.setAdapter(communityAdapter);
    }//createMemberList

}
