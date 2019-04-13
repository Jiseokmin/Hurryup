package kr.study.hurryup;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class StreamingActivity extends AppCompatActivity {

    private XWalkView xWalkWebView;
    TextView textView_response;
    Button btn_toggle;
    String IP_ADDRESS;
    int PORT_NUMBER = 8888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);

        IP_ADDRESS = "192.168.0.24"; //"192.168.30.33"; // 192.168.0.24
        //final String IP_ADDRESS;
        //final String IP_ADDRESS = "hurryup";
        //final int PORT_NUMBER = 8888;

        /*
        try {
            InetAddress address = InetAddress.getByName("http://hurryup:8080/");
            IP_ADDRESS = address.getHostAddress();
            textView_response.setText(IP_ADDRESS);
        } catch (UnknownHostException e) {
            textView_response.setText("주소를 찾을 수 없습니다.");
        }*/
        // http://raspberrypi:8080/stream/video.mjpeg
        // xWalkWebView.loadUrl("http://192.168.0.23:8080/stream/video.mjpeg");

        xWalkWebView = findViewById(R.id.xwalkWebView);
        xWalkWebView.getSettings().setLoadWithOverviewMode(true);
        xWalkWebView.getSettings().setUseWideViewPort(true);
        xWalkWebView.loadUrl("http://"+IP_ADDRESS+":8080/stream/video.mjpeg");

        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

        textView_response = findViewById(R.id.textView_responseText);
        btn_toggle = findViewById(R.id.btn_toggle);



        //connect 버튼 클릭
        btn_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyClientTask myClientTask = new MyClientTask(IP_ADDRESS, PORT_NUMBER, "toggle camera");
                myClientTask.execute();
            }
        });
    }

    public class MyClientTask extends AsyncTask<Void, Void, Void> {
        String dstAddress;
        int dstPort;
        String response = "";
        String myMessage;

        //constructor
        MyClientTask(String address, int port, String message){
            dstAddress = address;
            dstPort = port;
            myMessage = message;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket;
            try {
                socket = new Socket(dstAddress, dstPort);
                InputStream inputStream = socket.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                //송신
                OutputStream out = socket.getOutputStream();
                out.write(myMessage.getBytes());

                //수신
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                socket.close();
                response = "서버의 응답: " + byteArrayOutputStream.toString("UTF-8");

            } catch (UnknownHostException e) {
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textView_response.setText(response);
            super.onPostExecute(result);
        }
    }

    //기타 기본 동작들 추가
    @Override
    protected void onPause() {
        super.onPause();
        if (xWalkWebView != null) {
            xWalkWebView.pauseTimers();
            xWalkWebView.onHide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (xWalkWebView != null) {
            xWalkWebView.resumeTimers();
            xWalkWebView.onShow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (xWalkWebView != null) {
            xWalkWebView.onDestroy();
        }
    }
}