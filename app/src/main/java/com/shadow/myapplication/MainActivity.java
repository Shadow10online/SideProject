package com.shadow.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    PublishSubject<Integer> subject = PublishSubject.create();
    Disposable disposable;
    private  WebView webview;

    class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            Log.d("test:","get"+html);

            // process the html as needed by the app
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textView);
        webview = (WebView) findViewById(R.id.wv_test);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);



/* Register a new JavaScript interface called HTMLOUT */
        webview.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        webview.setWebViewClient(new WebViewClient());

/* WebViewClient must be set BEFORE calling loadUrl! */
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url)
            {
        /* This call inject JavaScript into the page which just finished loading. */
                view.loadUrl("javascript:window.HTMLOUT.processHTML(document.getElementsByTagName('html')[0].innerHTML);");
            }
        });
        webview.loadUrl("http://www.google.com");
    }

    public void onSend(View view){
        webview.loadUrl("javascript:(function(){"+
                "l=document.getElementById('tsbb');"+
                "e=document.createEvent('HTMLEvents');"+
                "e.initEvent('click',true,true);"+
                "l.dispatchEvent(e);"+
                "})()");
    }

    public void onDisconnect(View view){
        webview.loadUrl("javascript:(function(){"+
                "l=document.getElementById('lst-ib');"+
                "e=document.createEvent('TextEvent');"+
                "e.initTextEvent('textInput', true, true, null, \"New text\", 9, \"en-US\");"+
                "l.dispatchEvent(e);"+
                "})()");
    }

    public void onConnect(View view){
        Log.d("test","get:accept");
        disposable = subject.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                tv.setText("set" + integer);
                Log.d("test","get:accept");
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
                Log.d("test","get:"+throwable.getMessage());
            }
        });
    }

    public void onError(View view){
        Log.d("test","onError"+subject.hasObservers());
        if(subject.hasObservers()){
            subject.onError(new Exception("test"));
        }

    }
}
