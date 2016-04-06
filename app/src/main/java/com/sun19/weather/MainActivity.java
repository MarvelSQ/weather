package com.sun19.weather;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends ActionBarActivity {

    final int start = 100;
    float sangle,eangle,gangle;
    String httpUrl;
    String httpArg;
    Button check;
    RelativeLayout backcontainer;
    LinearLayout forecontainer,topbar;
    ImageView ivbg;
    EditText etcity;
    Bitmap bgimg;
    TextView city, date, temp, weather, sunrise, sunset;
    String scity, sdate, stemp, sweather, ssunrise, ssunset;
    weather getweather = new weather();
    ValueAnimator animator;
    DrawView view;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initop();
        weather getweather = new weather();
        getweather.execute(httpUrl + "?" + httpArg);
    }

    public void init() {
        httpUrl = "http://apis.baidu.com/heweather/weather/free";
        httpArg = "city=上海";
        backcontainer = (RelativeLayout) findViewById(R.id.backcontainer);
        forecontainer = (LinearLayout) findViewById(R.id.forecontainer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        forecontainer.setAlpha(0);
        bgimg= BitmapFactory.decodeStream(getClass().getResourceAsStream("/res/drawable/album.jpg"));
        view=new DrawView(this);
        backcontainer.addView(view,0);
        check = (Button) findViewById(R.id.btncheck);
        city = (TextView) findViewById(R.id.tvcity);
        date = (TextView) findViewById(R.id.tvtime);
        temp = (TextView) findViewById(R.id.tvtemp);
        weather = (TextView) findViewById(R.id.tvweather);
        sunrise = (TextView) findViewById(R.id.tvsunrise);
        sunset = (TextView) findViewById(R.id.tvsunset);
        etcity = (EditText) findViewById(R.id.etcity);
        topbar= (LinearLayout) findViewById(R.id.status_bar);
        topbar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            topbar.setVisibility(View.VISIBLE);
        }else {
            topbar.setVisibility(View.GONE);
        }
    }

    public void initop() {
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etcity.getText().toString() == null || etcity.getText().toString().length() == 0) {
                    Intent i=new Intent("sun.flowerpot.start");
                    startActivity(i);
                } else {
                    httpArg = "city=" + etcity.getText();
                }
                weather getweather = new weather();
                getweather.execute(httpUrl + "?" + httpArg);
            }
        });
    }

    public void measure(String rise,String set){
        Time tm=new Time(); // or Time t=new Time("GMT+8"); ����Time Zone���ϡ�
        tm.setToNow(); // ȡ��ϵͳʱ�䡣
        int year = tm.year;
        int month = tm.month;
        int date = tm.monthDay;
        int hour = tm.hour; // 0-23
        int minute = tm.minute;
        int second = tm.second;
        int t=hour*60+minute;
        String[] time=null;
        for(int i=0;i<rise.length();i++){
            time=rise.split(":");
        }
        int h1=Integer.parseInt(time[0]);
        int m1=Integer.parseInt(time[1]);
        int t1=h1*60+m1;
        for(int i=0;i<set.length();i++){
            time=set.split(":");
        }
        int h2=Integer.parseInt(time[0]);
        int m2=Integer.parseInt(time[1]);
        int t2=h2*60+m2;
        sangle=270-((t-t1)*360/(24*60));
        eangle=(t2-t1)*360/(24*60);
    }

    public class weather extends AsyncTask {
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            etcity.setText(null);
            city.setText(scity);
            date.setText(sdate);
            temp.setText(stemp + "\u2103");
            weather.setText(sweather);
            measure(ssunrise, ssunset);
            sunrise.setText(ssunrise);
            sunset.setText(ssunset);
            view.setMinimumHeight(500);
            view.setMinimumWidth(300);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "change", Toast.LENGTH_LONG).show();
                }
            });
            //backcontainer.removeView(view);


            animator = ValueAnimator.ofInt(0, 100);
            animator.setDuration(500);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer i = (Integer) animator.getAnimatedValue();
                    forecontainer.setAlpha((float) (i * 0.01));
                    gangle = (float) (i * 0.01 * eangle);
                    view.invalidate();
//                    city.setAlpha((float) (i*0.1));
//                    date.setAlpha((float) (i*0.1));
//                    temp.setAlpha((float) (i*0.1));
//                    weather.setAlpha((float) (i*0.1));
//                    sunrise.setAlpha((float) (i*0.1));
//                    sunset.setAlpha((float) (i*0.1));
                }
            });
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                URL url = new URL(params[0].toString());
                URLConnection connection = url.openConnection();
                connection.addRequestProperty("apikey", "d60bb6d3fdedab92dc8f4b8f613b398f");
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    builder.append(line);
                    System.out.println(line);
                }
                JSONObject roof = new JSONObject(builder.toString());
                JSONArray main = roof.getJSONArray("HeWeather data service 3.0");
                JSONObject root = main.getJSONObject(0);
                JSONObject basic = root.getJSONObject("basic");
                scity = basic.getString("city");
                JSONObject update = basic.getJSONObject("update");
                sdate = update.getString("loc");
                JSONObject now = root.getJSONObject("now");
                stemp = now.getString("tmp");
                JSONObject cond = now.getJSONObject("cond");
                sweather = cond.getString("txt");
                JSONArray daily = root.getJSONArray("daily_forecast");
                JSONObject first = daily.getJSONObject(0);
                JSONObject astro = first.getJSONObject("astro");
                ssunrise = astro.getString("sr");
                ssunset = astro.getString("ss");
                br.close();
                isr.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DrawView extends View {

        public DrawView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint p = new Paint();
            p.setColor(Color.RED);
            p.setAntiAlias(true);
            BitmapShader bs=new BitmapShader(bgimg, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            p.setShader(bs);
            RectF oval2 = new RectF(-600, -200, 1400, 1800);
            System.out.println(sangle+":"+eangle);
            canvas.drawArc(oval2, sangle, gangle, true, p);

        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
