package com.example.lululab;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.example.lululab.Adapter.MessageAdapter;
import com.example.lululab.Model.Message;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import android.Manifest;
public class ChatActivity extends AppCompatActivity {
    OkHttpClient client;
    RecyclerView chat_view; // 챗봇과 유저의 채팅을 볼 수 있는 view
    TextView text_LuLuChat; // 처음 LuLuChat에 입장할 때 보이는 가운데 문자열 (LuLuChat)
    EditText temp_msg; // 메세지 창에 아무것도 입력하지 않았을 때 보이는 문자열
    ImageButton btn_send; // 메세지 전송 버튼
    ImageButton btn_camera; // 카메라 촬영 버튼
    ImageButton btn_questionlist; // 질문 리스트를 열람할 수 있는 버튼
    Button chat_item_button;
    List<Message> messageList;
    MessageAdapter ViewRecommendCosmetics; // 카메라 촬영 후 챗봇이 보내는 "추천 상품 보기" 버튼
    List<CSVRecord> cosmetics;
    Map<String, String> mymap = new HashMap<>(); // LangChain parameter와 화장품 카테고리를 확실피 매핑해주기 위함, 아래에 추가 설명

    private boolean isRelatedQuestionGenerated = false; // 추천 질문 리스트가 새로 업데이트 되었음을 알려주는 변수

    // 초기 추천 질문 리스트, 이후 사용자가 질문을 하면 질문과 관련된 리스트로 새로 업데이트
    private String[] questionList = {
            "피부에 좋은 음식은 뭐에요?",
            "피부에 좋은 생활 습관은 뭐에요?",
            "여드름은 왜 생기는거에요?",
            "두드러기는 왜 생기는거에요?",
            "좁쌀 여드름이 생기는 원인이 뭐에요?",
            "피부가 너무 건조한데 해결방법에는 무엇이 있나요?",
            "모공각화증은 무엇인가요?",
            "좁쌀 여드름은 어떻게 해결하나요?",
            "각질 제거는 어떻게 하는게 좋나요?",
            "여름철에 자주 나타나는 피부 트러블은 무엇이 있을까요?",
            "건조한 피부를 가지고 있는데, 적절한 보습 방법이 있을까요?",
            "여드름을 예방하고 치료하기 위해 어떤 스킨케어 방법을 추천하시나요?",
            "피부 알러지의 주요 증상과 대처법은 무엇인가요?",
            "햇빛에 노출되면서 생기는 피부 손상을 예방하기 위해 해야 할 일은 무엇인가요?",
            "민감성 피부를 가지고 있는데, 피부를 진정시키는 방법은 무엇인가요?",
            "고령화로 인해 생기는 주름을 완화시키기 위해 사용할 수 있는 스킨케어 제품은 무엇인가요?",
            "피부가 민감해서 화장품을 선택할 때 주의해야 할 점은 무엇인가요?",
            "피부에 나타나는 멍과 피부색의 변화는 어떤 원인으로 인해 생기나요?",
            "피부 건강을 유지하기 위해 꾸준히 실천해야 할 스킨케어 습관은 무엇인가요?"
    };
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int REQUEST_IMAGE_CAPTURE = 1; // 카메라 설정 변수
    private String MY_SECRET_KEY; // ChatGPT API Key
    private String student_id; // 사용자의 학번 (연락처로 대체 가능)
    private int cameraFlag = 0; // 피부 분석 요청 시 사용자의 피부 분석 데이터가 있는 경우 피부 분석 결과를 알려주고 없는 경우 카메라 사용을 요청하기 위한 변수
    static JSONObject usersData; // 사용자의 정보가 담겨있는 JSON 파일 데이터

    //카메라 권한 허용
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int PERMISSIONS_REQUEST = 100;

    public ChatActivity() throws JSONException {}
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        MY_SECRET_KEY = getResources().getString(R.string.openai_secret_key); // 챗봇을 불러오기 위해 API_KEY를 할당합니다.
        usersData = getUsersDataFromAsset("users_data.json"); // 이후, 사용자의 피부타입을 챗봇이 미리 알도록 사용자의 데이터를 불러옵니다.

        /*
        LangChain을 통해 제품을 추천할 때 category 와 detail을 구별하지 못하는 문제가 발생.
        예를 들어, 클렌징은 category 열에 있는데 detail에서 탐색을 한 후 만족하는 클렌징이 없다고 답함.
        따라서, GPT Function으로 parameter를 뽑은 후 langchain에 입력할 때에 prompt에 [detail]==~~을 입력해서 범위를 지정해주는 방식으로 해결
        */
        mymap.put("화장품","화장품");
        mymap.put("선케어","[category]==선케어"); mymap.put("선스틱","[detail]==선스틱"); mymap.put("선로션","[detail]==선로션"); mymap.put("선스프레이","[detail]==선스프레이"); mymap.put("선쿠션","[detail]==선쿠션"); mymap.put("선크림","[detail]==선크림"); mymap.put("선파우더","[detail]==선파우더");
        mymap.put("스크럽","[detail]==스크럽"); mymap.put("필링","[detail]==필링");
        mymap.put("아이크림","[detail]==아이크림");
        mymap.put("에센스","[detail]==에센스"); mymap.put("세럼","[detail]==세럼"); mymap.put("앰플","[detail]==앰플");
        mymap.put("크림","[category]==크림"); mymap.put("로션","[detail]==로션"); mymap.put("밤","[detail]==밤"); mymap.put("보습크림","[detail]==보습크림"); mymap.put("스팟젤","[detail]==스팟젤"); mymap.put("에멀젼","[detail]==에멀젼");
        mymap.put("클렌징","[category]==클렌징"); mymap.put("립&아이 리무버","[detail]==립&아이 리무버"); mymap.put("클렌징 밀크","[detail]==클렌징 밀크"); mymap.put("클렌징 밤","[detail]==클렌징 밤"); mymap.put("클렌징 비누","[detail]==클렌징 비누"); mymap.put("클렌징 오일","[detail]==클렌징 오일"); mymap.put("클렌징 워터","[detail]==클렌징 워터"); mymap.put("클렌징 젤","[detail]==클렌징 젤"); mymap.put("클렌징 크림","[detail]==클렌징 크림"); mymap.put("클렌징 티슈","[detail]==클렌징 티슈"); mymap.put("클렌징 파우더","[detail]==클렌징 파우더"); mymap.put("클렌징 패드","[detail]==클렌징 패드"); mymap.put("클렌징 폼","[detail]==클렌징 폼");
        mymap.put("토너","[detail]==토너"); mymap.put("스킨","[detail]==스킨");
        mymap.put("팩","[category]==팩"); mymap.put("마스크팩","[detail]==마스크팩"); mymap.put("모델링팩","[detail]==모델링팩"); mymap.put("슬리핑팩","[detail]==슬리핑팩"); mymap.put("시트팩","[detail]==시트팩"); mymap.put("워시오프팩","[detail]==워시오프팩"); mymap.put("코팩","[detail]==코팩"); mymap.put("패치","[detail]==패치"); mymap.put("필오프팩","[detail]==필오프팩");

        // 챗봇 답변 시간 조절, 너무 오래 기다리면 사용자가 불편하다고 생각
        client = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        checkPermissions();

        // 아래 변수들은 위에서 어떤 역할을 했는지 설명하였음
        chat_view = findViewById(R.id.recycler_view);
        text_LuLuChat = findViewById(R.id.tv_welcome);
        temp_msg = findViewById(R.id.et_msg);
        EditText etMsg = findViewById(R.id.et_msg);
        btn_send = findViewById(R.id.btn_send);
        btn_camera = findViewById(R.id.btn_camera);
        btn_questionlist = findViewById(R.id.btn_list);
        View chatItemView = getLayoutInflater().inflate(R.layout.chat_item, null);
        chat_item_button = chatItemView.findViewById(R.id.chat_item_view_button);
        chat_view.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        chat_view.setLayoutManager(manager);
        student_id = MainActivity.studentId;
        messageList = new ArrayList<>();
        ViewRecommendCosmetics = new MessageAdapter(messageList);
        chat_view.setAdapter(ViewRecommendCosmetics);

        // send 버튼을 통해 챗봇에 메세지를 전송 가능, 전송 후 addToChat을 통해 메세지를 띄우고, API를 호출함.
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String question = temp_msg.getText().toString().trim();
                temp_msg.setText("");
                addToChat(question, Message.SENT_BY_ME, null);
                callAPI(question);
                text_LuLuChat.setVisibility(View.GONE);
            }
        });

        // 사용자가 새로운 메세지를 전송할 때마다 이전에 있던 메세지를 20 height만큼 올림
        chat_view.addItemDecoration(new RecyclerViewDecoration(20));

        /*
         챗봇이 피부 결과 전송 후 "추천 상품 보기" 버튼 클릭 시 이벤트
         사용자의 피부타입과 피부질환을 고려한 화장품을 추천해줌
         */
        ViewRecommendCosmetics.setOnButtonClickListener(new MessageAdapter.OnButtonClickListener() {
            @Override
            public void onButtonClick(int position) throws JSONException {
                JSONObject userData = usersData.getJSONObject(student_id); // 사용자의 피부타입 확인을 위한 데이터 로드
                String userskintype = userData.getString("skintype"); // 사용자의 피부타입을 받아옴
                List<String> lowestScoreCondition = getLowestScoreCondition(userData); // lowestScoreCondition 메소드를 통해 가장 낮은 점수의 피부질환 2개를 추출
                addToChat("...", Message.SENT_BY_BOT, null);

                JSONObject object = new JSONObject();
                // 피부타입과 피부질환을 반영한 prompt 질의 생성 후 Langchain을 통한 제품 추천
                try {
                    object.put("question", "Recommend 'three' random '"+ "화장품" +"' which contain '" + userskintype + "' skin type and 'must contain' skin concerns 'BOTH' '" + lowestScoreCondition.get(0) + "' 'AND' '" + lowestScoreCondition.get(1) + "'.sample(3). Print it");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Flask를 통해 recommend 함수를 호출해 LangChain과 연결할 수 있음
                RequestBody body = RequestBody.create(object.toString(), JSON);
                Request request = new Request.Builder()
                        .url("http://121.138.183.119:31002/recommend")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    // LangChain으로부터 제품 추천을 받아오는 것에 실패한 경우
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        addResponse("Failed to load response due to " + e.getMessage());
                    }

                    // LangChain으로부터 제품 추천을 받아오는 것에 성공한 경우, result에 제품 3개를 받아서 출력합니다.
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            JSONObject jsonObject;
                            try {
                                jsonObject = new JSONObject(response.body().string());
                                String result = jsonObject.getString("result");
                                addResponse(result.trim());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            addResponse("Failed to load response due to " + response.body().string());
                        }
                    }
                });
            }
        });

        /*
        메세지 창에 사용자가 긴 텍스트를 입력하고 있는 경우, 질문 리스트 버튼과 카메라 버튼에 의해 텍스트가
        가려지는 현상이 발생하므로, 아무 텍스트나 입력되는 경우 질문 리스트와 카메라 버튼을 일시적으로 없애주는 메소드
         */
        etMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    btn_camera.setVisibility(View.GONE);
                    btn_questionlist.setVisibility(View.GONE);
                } else {
                    btn_camera.setVisibility(View.VISIBLE);
                    btn_questionlist.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // 사용자의 피부 사진을 찍을 수 있는 카메라 기능
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        // 질문 리스트 버튼 클릭 시 추천 질문을 보여주는 기능
        btn_questionlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRelatedQuestionGenerated && !relatedQuestions.isEmpty()) {
                    showRelatedQuestionList(relatedQuestions);
                } else {
                    showQuestionListDialog();
                }
            }
        });
    }

    // json 파일 데이터를 usersData에 할당하는 메소드
    private JSONObject getUsersDataFromAsset(String filename) {
        AssetManager assetManager = getAssets();
        try {
            InputStream is = assetManager.open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return new JSONObject(sb.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 초기 추천 질문 리스트를 보여주는 다이얼로그 창
    private void showQuestionListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("추천 질문");
        List<String> selectedQuestions = new ArrayList<>();
        Random random = new Random();
        Set<Integer> selectedIndices = new HashSet<>();
        while (selectedIndices.size() < 3 && selectedIndices.size() < questionList.length) {
            int randomIndex = random.nextInt(questionList.length);
            if (!selectedIndices.contains(randomIndex)) {
                selectedIndices.add(randomIndex);
                selectedQuestions.add(questionList[randomIndex]);
            }
        }

        // 질문 리스트 중 하나를 선택하면, 그 질문에 해당하는 메세지가 보내지고 API를 호출합니다.
        builder.setItems(selectedQuestions.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                String selectedQuestion = selectedQuestions.get(position);
                userQuestions.add(selectedQuestion); //GPT에 보낼 prompt에 추가
                addToChat(selectedQuestion, Message.SENT_BY_ME, null);
                callAPI(selectedQuestion);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    // 사용자의 데이터를 보고 가장 낮은 점수의 피부 질환 2개를 반환
    private static List<String> getLowestScoreCondition(JSONObject userData) {
        if (userData == null) {
            return null;
        }

        String[] conditions = {"모공", "피지", "붉은기", "트러블", "주름", "색소침착", "다크써클", "탄력", "생기"};
        Map<String, Double> conditionScores = new HashMap<>();
        try {
            for (String condition : conditions) {
                if (!userData.has(condition)) {
                    return null;
                }
                conditionScores.put(condition, userData.getDouble(condition));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (conditionScores.isEmpty()) {
            return null;
        }

        return conditionScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 카메라 권한 체크
    private boolean checkPermissions() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST);
                return false;
            }
        }
        return true;
    }

    // 권한이 거부된 경우, 사용자에게 설명을 제공하거나 기능을 비활성화합니다.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // 카메라 촬영 후 체크 버튼을 누르면, 서버에 찍은 사진이 저장되고, 서버로부터 받은 피부 결과를 1초 후에 보여줌
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            View chatItemView = getLayoutInflater().inflate(R.layout.chat_item, null);
            ImageView newImageView = chatItemView.findViewById(R.id.newImageView);
            newImageView.setImageBitmap(bitmap);

            // 서버에 저장하기 위해 찍은 사진을 파일로 변환하고 저장
            String fileName = "upload" + System.currentTimeMillis() + ".jpg";
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 촬영한 사진을 챗봇에게 전송
            String url = file.getAbsolutePath();
            addToChat(null, Message.SENT_BY_ME, url);
            text_LuLuChat.setVisibility(View.GONE);
            uploadImage(url);

            // 서버로 부터 받은 피부 결과를 보여줌 (피부타입, 피부 질환 2종)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String student_id = MainActivity.studentId;
                    if (usersData.has(student_id)) {
                        try {
                            JSONObject userData = usersData.getJSONObject(student_id);
                            String skinType = userData.getString("skintype");
                            List<String> lowestScoreCondition = getLowestScoreCondition(userData);
                            String message = "Skin Type: " + skinType + ", Lowest Score Condition: " + lowestScoreCondition;
                            addToChat(message, Message.SENT_BY_SYSTEM, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 1000);
        }
    }

    // ChatBot 화면에 메세지를 띄웁니다.
    void addToChat(String message, String sentBy, String imageUrl) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy, imageUrl));
                ViewRecommendCosmetics.notifyDataSetChanged();
                chat_view.smoothScrollToPosition(ViewRecommendCosmetics.getItemCount());
            }
        });
    }

    // 챗봇이 보낸 메세지에서 '...'을 지우고 답변을 추가합니다.
    void addResponse(String response) {
        messageList.remove(messageList.size() - 1);
        addToChat(response, Message.SENT_BY_BOT, null);
    }

    private List<String> userQuestions = new ArrayList<>(); //챗봇에 보낼 메세지를 저장합니다. (기억하기 위한 용도)

    // ChatGPT API를 통해 답변을 받아오는 함수
    void callAPI(String question) {
        userQuestions.add(question);
        text_LuLuChat.setVisibility(View.GONE);
        addToChat("...", Message.SENT_BY_BOT, null);
        JSONArray arr = new JSONArray();
        JSONObject baseAi = new JSONObject();
        try {
            // AI 속성설정
            baseAi.put("role", "system");
            baseAi.put("content", "You are a helpful and kind AI Assistant.");

            // array로 담아서 한번에 전송
            arr.put(baseAi);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role","user");
            userMsg.put("content",userQuestions.get(userQuestions.size() - 1));
            arr.put(userMsg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        JSONArray functionsArray = new JSONArray();

        // GPT에 함수에 대한 정보 전송 (1. 피부 분석 메타데이터, 2. 화장품 추천 메타데이터, 3. 카메라 촬영 메타데이터)
        functionsArray.put(skin_metadata);
        functionsArray.put(recommend_cos_metadata);
        functionsArray.put(camera_metadata);
        JSONObject object = new JSONObject();

        // GPT option 설정
        try {
            object.put("model", "gpt-3.5-turbo-0613");// gpt-3.5-turbo-0613이나 gpt-4-0613버전만 사용 가능
            object.put("messages", arr);
            object.put("temperature", 0);
            object.put("max_tokens", 300);
            object.put("functions", functionsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + MY_SECRET_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        JSONObject messageObject = jsonArray.getJSONObject(0).getJSONObject("message");

                        // 만약 챗봇이 사용자의 메세지를 보고 함수를 사용하기로 결정하는 경우
                        if (messageObject.has("function_call"))
                        {
                            // 사용할 함수 이름과 변수를 가져옴, 함수마다 필요한 인자의 개수가 다르기 때문에 함수에 맞게 인자를 전달해줍니다.
                            String function_name = jsonArray.getJSONObject(0).getJSONObject("message").getJSONObject("function_call").getString("name");
                            String params_str =   jsonArray.getJSONObject(0).getJSONObject("message").getJSONObject("function_call").getString("arguments");
                            JSONObject params = new JSONObject(params_str);
                            switch (function_name) {
                                // 피부 분석인 경우, 피부 타입과 피부 질환 2종
                                case "skin":
                                    skin(params.getString("skin_type"), params.getString("skin_con1"), params.getString("skin_con2"), params.getString("category"));
                                    break;
                                // 제품 추천인 경우
                                case "recommend_cos":
                                    recommend_cos(params.getString("symbol"));
                                    break;

                                case "camera_rec":
                                    camera_rec(params.getString("cam"));
                                    break;
                                default:
                                    throw new IllegalArgumentException("Invalid function name: " + function_name);
                            }
                        }
                        //함수를 사용하지 않을 때는 단순히 챗봇의 답변을 받아옵니다.
                        else {
                            String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                            addResponse(result.trim());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //generateRelatedQuestions 호출
                                generateRelatedQuestions(userQuestions.get(userQuestions.size() - 1));
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().string());
                }
            }
        });
    }

    // 촬영한 이미지를 서버에 업로드하는 메소드
    void uploadImage(String filePath) {
        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(), requestFile)
                .build();

        // Flask의 upload 함수로 이미지를 서버에 저장할 수 있음
        String serverUrl = "http://121.138.183.119:31002/upload";

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // 연결에 성공하면, 성공적으로 이미지를 서버에 저장할 수 있음
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChatActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            cameraFlag = 1;
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChatActivity.this, "Image upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // 추천 질문 리스트, 사용자의 질문에 관련된 추천 질문을 이 리스트에 저장합니다.
    private List<String> relatedQuestions = new ArrayList<>();

    // GPT를 통해 추천 질문을 생성하는 함수, prompt engineering을 통해 관련 질문을 받아옵니다.
    private void generateRelatedQuestions(String seedQuestion) {
        temp_msg = findViewById(R.id.et_msg);
        temp_msg.setEnabled(false);
        btn_questionlist.setEnabled(false);
        JSONObject data = new JSONObject();
        try {
            // AI 속성 설정
            JSONObject baseAi = new JSONObject();
            baseAi.put("role", "system");
            baseAi.put("content", "You are a helpful and kind AI Assistant.");

            // 사용자의 메세지를 받아 prompt를 재정의해 GPT에게 전달
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", seedQuestion + "가 피부, 화장품과 조금이라도 관련된 내용이면"
                    + seedQuestion + "에 질문할 수 있는 다른 질문 3개를 각각 한 줄 씩 의문문으로 작성해줘");

            // 메시지 배열 생성
            JSONArray messages = new JSONArray();
            messages.put(baseAi);
            messages.put(userMsg);

            data.put("model", "gpt-3.5-turbo");
            data.put("messages", messages);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(data.toString(), JSON);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + MY_SECRET_KEY)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            // 챗봇으로부터 3개의 질문을 받는 것에 성공하면, 전처리 후 질문 리스트를 업데이트합니다.
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        relatedQuestions = new ArrayList<>();
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");

                        String[] questions = result.split("\n");

                        // 각 문자열에서 제일 앞에 있는 숫자와 컴마 제거, 챗봇으로부터 온 질문 3개를 전처리하는 과정
                        for (int j = 0; j < questions.length; j++) {
                            String question = questions[j];
                            int index = question.indexOf(".");
                            if (index != -1 && index + 1 < question.length()) {
                                questions[j] = question.substring(index + 1).trim();
                            }
                        }

                        // 전처리한 질문들은 추천 질문 리스트에 추가
                        relatedQuestions.addAll(Arrays.asList(questions));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                temp_msg.setEnabled(true);
                                btn_questionlist.setEnabled(true);
                                if (relatedQuestions.size() >= 3) {
                                    isRelatedQuestionGenerated = true;
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    temp_msg.setEnabled(true);
                }
            }
        });
    }

    // 사용자가 마지막으로 했던 말과 관련된 추천 질문 목록을 표시하는 다이얼로그
    private void showRelatedQuestionList(List<String> relatedQuestions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("추천 질문");
        final CharSequence[] items = relatedQuestions.toArray(new CharSequence[relatedQuestions.size()]);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            // 추천 질문 중 하나를 클릭하면, 그 질문이 사용자의 메세지로써 보내지고 API를 호출합니다.
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedQuestion = relatedQuestions.get(which);
                userQuestions.add(selectedQuestion);
                addToChat(selectedQuestion, Message.SENT_BY_ME, null);
                callAPI(selectedQuestion);
            }
        });

        // 추천 질문 리스트를 닫는 버튼
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    // 챗봇 관련 함수

    // required에 들어가는 요소
    String[] camera_array = new String[] {"cam"};
    String[] skin_array = new String[] {"skin_type", "skin_cond1", "skin_cond2", "category"};
    String[] rec_cos_array = new String[] {"symbol"};
    String[] skintype = new String[] {"OS","OR","NR","NS","DR","DS","OR-A","DR-A","NS-A","NR-A","DS-A","OS-A"};
    String[] skinconcern = new String[] {"붉은기","주름","색소침착","트러블","다크서클","피지","탄력","모공"};
    String[] categories = new String[] {"화장품","선케어","선로션","선스틱","선스프레이","선쿠션","선크림","선파우더","스크럽","필링","아이크림","에센스","세럼","앰플","크림","로션","밤","보습크림","수딩젤","수분크림","스팟젤","에멀젼","클렌징","립&아이 리무버","클렌징 밀크","클렌징 밤","클렌징 비누","클렌징 오일"," 클렌징 워터","클렌징 젤","클렌징 크림","클렌징 티슈","클렌징 파우더","클렌징 패드", "클렌징 폼","토너","스킨","팩","마스크팩","모델링팩","슬리핑팩","시트팩","워시오프팩","코팩","패치","필오프팩"};

    //피부 분석을 요청하는 맥락의 질문을 답변하는 함수에 대한 metadata
    JSONObject camera_metadata = new JSONObject()
            .put("name", "camera_rec") // 함수 이름
            .put("description", "Works when the user wants skin 'analysis'.  e.g : '내 피부 어때?', '내 얼굴 어때?', '내 얼굴 좀 좋아진 것 같지 않아?'")
            .put("parameters", new JSONObject()
                    .put("type", "object")
                    .put("properties", new JSONObject()
                            .put("cam", new JSONObject()
                                    .put("type", "string")
                                    .put("description", "any word")
                            )
                    )
                    .put("required", new JSONArray(camera_array)) //필요한 parameter에 대한 정보
            );

    //화장품을 추천하는 함수에 대한 metadata
    JSONObject skin_metadata = new JSONObject()
            .put("name", "skin")
            .put("description", "Recommend cosmetics depending on user`s skin type and exactly two skin concerns.  " +
                    "e.g : '붉은기와 트러블에 효과적인 화장품을 추천해줘.'")
            .put("parameters", new JSONObject()
                    .put("type", "object")
                    .put("properties", new JSONObject()
                            .put("st", new JSONObject()
                                    .put("type", "string")
                                    //enum형식으로 피부 타입을 추출(문장 안에서 피부 타입에 대한 정보를 skintype안에 있는 OS,OR등으로 한정하여 추출)
                                    .put("enum", new JSONArray(skintype))
                                    .put("description", "skin type")
                            )
                            .put("sc1", new JSONObject() //피부 질환 1(Skin Concern 1)
                                    .put("type", "string")
                                    .put("enum", new JSONArray(skinconcern))
                                    .put("description", "First skin concern")
                            )
                            .put("sc2", new JSONObject() //피부 질환 2
                                    .put("type", "string")
                                    .put("enum", new JSONArray(skinconcern))
                                    .put("description", "Second skin concern")
                            )
                            .put("category", new JSONObject() //카테고리에 대한 정보
                                    .put("type", "string")
                                    .put("enum", new JSONArray(categories))
                                    .put("description", "category or detail")
                            )
                    )
                    .put("required", new JSONArray(skin_array))
            );

    // 일반적, 간접적인 제품 추천에 대한 질문을 다루는 함수에 대한 metadata
    JSONObject recommend_cos_metadata = new JSONObject()
            .put("name", "recommend_cos")
            .put("description", "Recommend cosmetics when user wants it.  e.g : '요즘 너무 건조해', '화장품 추천해줘'")
            .put("parameters", new JSONObject()
                    .put("type", "object")
                    .put("properties", new JSONObject()
                            .put("symbol", new JSONObject()
                                    .put("type", "string")
                                    .put("description", "Any word")
                            )
                    )
                    .put("required", new JSONArray(rec_cos_array))
            );

    // 화장품을 추천하는 함수. GPT가 추출한 parameter를 토대로 Langchain 실행
    public Void skin(String skin_type, String skin_con1, String skin_con2, String category) throws JSONException {
        JSONObject userData = usersData.getJSONObject(student_id);
        String userskintype = userData.getString("skintype");
        JSONObject object = new JSONObject();

        // parameter로 받은 정보로 3개의 랜덤 화장품 추천
        try {
            object.put("question", "Recommend 'three' random '"+ mymap.get(category) +"' ['name'] which contain '"
                    + userskintype + "' skin type and 'must contain' skin concerns 'BOTH' '" + skin_con1 + "' 'AND' '"
                    + skin_con2 + "'.sample(3). Print it with INDEX. Do not Repeat PROMPT.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url("http://121.138.183.119:31002/recommend")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }

            // LangChain으로부터 화장품을 받아오는 것에 성공하면, 데이터 전처리 후 결과 출력
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        String res = jsonObject.getString("result");
                        addResponse(res.trim());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // generateRelatedQuestions 호출
                                generateRelatedQuestions(userQuestions.get(userQuestions.size() - 1));
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().string());
                }
            }
        });
        return null;
    }

    //일반적, 간접적인 화장품을 Langchain을 통해 추천하는 함수
    public Void recommend_cos(String symbol) throws JSONException {
        JSONObject userData = usersData.getJSONObject(student_id);
        String userskintype = userData.getString("skintype");
        JSONObject object = new JSONObject();
        try {
            object.put("question", "Recommend 'three' random '"+ "cosmetics" +"' 'name' which contain '"
                    + userskintype + "' skin type and related to " + userQuestions.get(userQuestions.size() - 1)
                    + " .sample(3). Print it");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url("http://121.138.183.119:31002/recommend")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        String result = jsonObject.getString("result");

                        addResponse(result.trim());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //generateRelatedQuestions 호출
                                generateRelatedQuestions(userQuestions.get(userQuestions.size() - 1));
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().string());
                }
            }
        });
        return null;
    }

    // 피부 분석을 원하는 맥락의 질문을 할 경우 사용자에 대한 피부 분석 결과가 없다면 카메라 사용 추천을, 있다면 피부 분석을 해주는 GPT를 호출하는 함수
    public String camera_rec(String cam) throws JSONException {
        String resp;
        JSONObject userData = usersData.getJSONObject(student_id);
        //피부 분석을 하지 않았을 경우(사진을 찍지 않은 경우)
        if(cameraFlag == 0)
        {
            resp = "자세한 분석을 위해서 우측 하단의 카메라 버튼을 통해 사진을 찍어 보내주세요!";
            addResponse(resp);
        }
        //피부를 분석하는 GPT 호출
        else
        {
            text_LuLuChat.setVisibility(View.GONE);
            JSONArray arr = new JSONArray();
            JSONObject baseAi = new JSONObject();
            try {
                // AI 속성설정
                baseAi.put("role", "system");
                baseAi.put("content", "You are a helpful and kind AI Assistant.");
                arr.put(baseAi);
                JSONObject userMsg = new JSONObject();
                userMsg.put("role","user");

                // Prompt Engineering - 피부 분석 방법 및 기준을 챗봇에게 Prompt로 전달
                userMsg.put("content",userData + "피부 분석요청이 오면 내 피부를 분석해주는데 다음 데이터를 기반으로 분석해줘. 피부 타입은  O : 지성 D : 건성 N : 정상 S : 민감성 R : 저항성 A : " +
                        "알러지성으로 나뉘어. 예를 들면 지성-민감성인 사람은 OS로 표시해. 또 피부를 각 항목에 따라 수치화해서 점수로 표현하는데 점수가 0에 가까울수록 피부가 안좋아. 그 항목들에는 모공, 피지, 붉은기, 여드름, 주름, 색소침착," +
                        " 다크써클, 탄력, 생기, 화사함이 있어. 점수는 0점부터 10점까지야.  예를 들어 여드름이 1점인 사람은 여드름이 심각한 피부를 가지고 있어. 주름이 9점인 사람은 주름이 거의 없다는 뜻이야. 점수가 5점보다 낮으면 심각한 " +
                        "수준이라서 따로 관리가 필요하고 9점 이상인 항목은 따로 관리가 필요가 없는 항목이야. 5점 초과 9점 미만인 항목은 평범한 편이야. 5점 미만인 항목은 심각한 편이야. 점수가 높은 항목보다는 점수가 낮은 항목이 더 중요해. 점수표는 한번만 보여주고 " +
                        "문장을 짧게 요약해줘");
                arr.put(userMsg);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            JSONObject object = new JSONObject();
            try {
                object.put("model", "gpt-3.5-turbo-0613");
                object.put("messages", arr);
                object.put("temperature", 0);
                object.put("max_tokens", 512);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(object.toString(), JSON);
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + MY_SECRET_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    addResponse("Failed to load response due to " + e.getMessage());
                }

                // 제품 추천과 마찬가지로, 피부 분석 결과도 사용자가 편하게 볼 수 있도록 전처리 한 후 사용자게에 보여줌
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject;
                        try {

                            jsonObject = new JSONObject(response.body().string());
                            JSONArray jsonArray = jsonObject.getJSONArray("choices");
                            String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addResponse(result.trim());
                                    generateRelatedQuestions(userQuestions.get(userQuestions.size() - 1));
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        addResponse("Failed to load response due to " + response.body().string());
                    }
                }
            });
        }
        return null;
    }
}