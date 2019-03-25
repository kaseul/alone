package alone.klp.kr.hs.mirim.alone;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;

import alone.klp.kr.hs.mirim.alone.model.LibraryItem;
import alone.klp.kr.hs.mirim.alone.model.Member;

import static alone.klp.kr.hs.mirim.alone.CommunityActivity.communityAdapter;
import static alone.klp.kr.hs.mirim.alone.LibraryActivity.adapter;
import static alone.klp.kr.hs.mirim.alone.SignInActivity.var;

public class MainActivity extends TabActivity {

    private RelativeLayout layout;
    public static EditText editSearch;
    private Button btn_search;
    private ArrayList<LibraryItem> lib_list;
    private ArrayList<Member> com_items;
    private ArrayList<LibraryItem> lib_search;
    private ArrayList<Member> com_search;

    public static InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        lib_list = new ArrayList<LibraryItem>();
        com_items = new ArrayList<Member>();
        lib_search = new ArrayList<>();
        com_search = new ArrayList<>();

        layout = findViewById(R.id.layout_main);
        editSearch = findViewById(R.id.edit_search);
        btn_search = findViewById(R.id.btn_search);

        // 화면 터치 시 키보드 내리기
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
            }
        });

        final TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.getTabWidget().setDividerDrawable(null);

        Intent library_intent = new Intent(this, LibraryActivity.class);
        library_intent.putExtra("library_list", lib_list);
        library_intent.putExtra("library_search", lib_search);

        Intent community_intent = new Intent(this, CommunityActivity.class);
        community_intent.putExtra("community_list", com_items);
        community_intent.putExtra("community_search", com_search);

        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("LIBRARY").setContent(library_intent));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("COMMUNITY").setContent(community_intent));

        TextView tabTitle = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        tabTitle.setTextColor(getResources().getColor(R.color.colorTabSelected));

        tabTitle = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        tabTitle.setTextColor(getResources().getColor(R.color.colorTab));

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for(int i = 0; i < tabHost.getTabWidget().getTabCount(); i++) {
                    TextView tabTitle = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                    tabTitle.setTextColor(getResources().getColor(R.color.colorTab));
                }

                // 선택된 탭 색 바꾸기
                TextView tabTitle = (TextView) tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).findViewById(android.R.id.title);
                tabTitle.setTextColor(getResources().getColor(R.color.colorTabSelected));
            }
        });

        editSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // input창에 문자를 입력할때마다 호출된다.
                // search 메소드를 호출한다.
                String text = editSearch.getText().toString();
                search(text);
            }

        });

    }

    public static int dpToPx(Context context, int dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float)dpValue * density);
    }

    // 검색을 수행하는 메소드
    public void search(String charText) {
        if(var.isLibrary) {
            // 문자 입력시마다 리스트를 지우고 새로 뿌려준다.
            lib_list.clear();

            // 문자 입력이 없을때는 모든 데이터를 보여준다.
            if (charText.length() == 0) {
                lib_list.addAll(lib_search);
            }
            // 문자 입력을 할때..
            else {
                // 리스트의 모든 데이터를 검색한다.
                for (int i = 0; i < lib_search.size(); i++) {
                    // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
                    if (lib_search.get(i).content.toLowerCase().contains(charText.toLowerCase()) || matchString(lib_search.get(i).content, charText)) {
                        // 검색된 데이터를 리스트에 추가한다.
                        lib_list.add(lib_search.get(i));
                    } else if (lib_search.get(i).title.toLowerCase().contains(charText.toLowerCase()) || matchString(lib_search.get(i).title, charText)) {
                        lib_list.add(lib_search.get(i));
                    }
                }
            }
            // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
            adapter.notifyDataSetChanged();
        } else {
            // 문자 입력시마다 리스트를 지우고 새로 뿌려준다.
            com_items.clear();

            // 문자 입력이 없을때는 모든 데이터를 보여준다.
            if (charText.length() == 0) {
                com_items.addAll(com_search);
            }
            // 문자 입력을 할때..
            else
            {
                // 리스트의 모든 데이터를 검색한다.
                for(int i = 0; i < com_search.size(); i++)
                {
                    // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
                    if (com_search.get(i).getText().toLowerCase().contains(charText.toLowerCase()) || matchString(com_search.get(i).getText(),charText))
                    {
                        // 검색된 데이터를 리스트에 추가한다.
                        com_items.add(com_search.get(i));
                    }
                }
            }
            // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
            communityAdapter.setCommunityAdapter(com_items);
        }
    }

    private static final char HANGUL_BEGIN_UNICODE = 44032; // 가
    private static final char HANGUL_LAST_UNICODE = 55203; // 힣
    private static final char HANGUL_BASE_UNIT = 588;//각자음 마다 가지는 글자수
    //자음
    private static final char[] INITIAL_SOUND = { 'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' };


    /**
     * 해당 문자가 INITIAL_SOUND인지 검사.
     * @param searchar
     * @return
     */
    private static boolean isInitialSound(char searchar){
        for(char c:INITIAL_SOUND){
            if(c == searchar){
                return true;
            }
        }
        return false;
    }

    /**
     * 해당 문자의 자음을 얻는다.
     *
     * @param c 검사할 문자
     * @return
     */
    private static char getInitialSound(char c) {
        int hanBegin = (c - HANGUL_BEGIN_UNICODE);
        int index = hanBegin / HANGUL_BASE_UNIT;
        return INITIAL_SOUND[index];
    }

    /**
     * 해당 문자가 한글인지 검사
     * @param c 문자 하나
     * @return
     */
    private static boolean isHangul(char c) {
        return HANGUL_BEGIN_UNICODE <= c && c <= HANGUL_LAST_UNICODE;
    }

    /** * 검색을 한다. 초성 검색 완벽 지원함.
     * @param value : 검색 대상 ex> 초성검색합니다
     * @param search : 검색어 ex> ㅅ검ㅅ합ㄴ
     * @return 매칭 되는거 찾으면 true 못찾으면 false. */
    public static boolean matchString(String value, String search){
        int t = 0;
        int seof = value.length() - search.length();
        int slen = search.length();
        if(seof < 0)
            return false; //검색어가 더 길면 false를 리턴한다.
        for(int i = 0;i <= seof;i++){
            t = 0;
            while(t < slen){
                if(isInitialSound(search.charAt(t))==true && isHangul(value.charAt(i+t))){
                    //만약 현재 char이 초성이고 value가 한글이면
                    if(getInitialSound(value.charAt(i+t))==search.charAt(t))
                        //각각의 초성끼리 같은지 비교한다
                        t++;
                    else
                        break;
                } else {
                    //char이 초성이 아니라면
                    if(value.charAt(i+t)==search.charAt(t))
                        //그냥 같은지 비교한다.
                        t++;
                    else
                        break;
                }
            }
            if(t == slen)
                return true; //모두 일치한 결과를 찾으면 true를 리턴한다.
        }
        return false; //일치하는 것을 찾지 못했으면 false를 리턴한다.
    }
}
