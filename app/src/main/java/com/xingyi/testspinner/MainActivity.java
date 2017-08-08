package com.xingyi.testspinner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class MainActivity extends Activity {

    String province, city;//最终选择结果放在这两个变量******************************************
   //因为spinner在加载视图的时候会自动调用点击响应事件，这两个变量在那个时候就已经初始化了

    HashMap<String, String> provinceHash = new HashMap<>();
    String[] provinceString = new String[34];

    HashMap<String, String> cityHash = new HashMap<>();
    String[] cityString;

    String file;

    String cityNo = null;// 最重要的参数，选中的城市的cityNo

    private ArrayAdapter<String> provinceAdapter;
    private ArrayAdapter<String> cityAdapter;
    Spinner provinceSpinner;
    Spinner citySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        provinceSpinner = findViewById(R.id.spinnerprovince);
        citySpinner = findViewById(R.id.spinnercity);

        file = readFile(); // 读取txt文件
        getProvinces(file); // 得到省的列表

        // 设置spinner，不用管什么作用
        provinceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, provinceString);
        provinceAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// 设置下拉风格
        provinceSpinner.setAdapter(provinceAdapter); // 将adapter 添加到spinner中
        provinceSpinner.setOnItemSelectedListener(new ProvinceSelectedListener(MainActivity.this));// 添加监听
        provinceSpinner.setVisibility(View.VISIBLE);// 设置默认值

    }

    public void click(View v) {
        Toast.makeText(this,   province +"  "+ city, Toast.LENGTH_SHORT).show();
    }

    public String readFile() {

        /*
         * 读取文件中数据的方法
         */

        InputStream myFile = null;
        myFile = getResources().openRawResource(R.raw.ub_city);
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(myFile, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("debug", e.toString());
        }
        String temp;
        String str = "";
        try {
            while ((temp = br.readLine()) != null) {
                str = str + temp;
                // Log.i("zhiyinqing", "断点3"+temp);
            }
            br.close();
            myFile.close();
            return str;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "error";
        }
    }

    public void getProvinces(String jsonData) {

        /*
         * 从json字符串中得到省的信息
         */

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < 34; i++) {
                // 获取了34个省市区信息
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String guid = jsonObject.getString("guid");
                String cityName = jsonObject.getString("cityName");
                // Log.i("zhiyinqing", i+guid+cityName);
                provinceHash.put(cityName, guid);
                provinceString[i] = cityName;

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String[] getCitys(String guid, String jsonData) {
        /*
         * 此方法用于查找一个省下的所有城市
         */
        // 初始化hashmap
        cityHash.clear();
        // 暂时存放城市的数组
        String[] cityBuffer = new String[21];
        int j = 0;

        // 解析数据
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonData);
            int length = jsonArray.length();
            int i = 33;
            int continuous = 0;// 这个变量用于判断是否连续几次没有找到，如果是，则该省信息全部找到了
            boolean isFind = false;

            while (i < length) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String fGuid = jsonObject.getString("fGuid");
                String cityName = jsonObject.getString("cityName");
                String cityNo = jsonObject.getString("cityNo");
                if (fGuid.equals(guid)) {
                    isFind = true;
                    cityHash.put(cityName, cityNo);
                    cityBuffer[j] = cityName;
                    j++;
                    // Log.i("zhiyinqing", cityName);
                } else {
                    if (isFind == true) {
                        continuous += 1;

                        if (continuous > 5) {
                            Log.i("zhiyinqing", "城市数:" + j);
                            break;
                        }
                    }
                }
                i++;
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String[] citys = new String[j];
        for (int i = 0; i < j; i++) {
            citys[i] = cityBuffer[i];
        }
        return citys;
    }

    class ProvinceSelectedListener implements AdapterView.OnItemSelectedListener {

        Context context;

        // 省被选择的监听器

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {

            String provinceName = provinceString[arg2];
            province = provinceName;
            String guid = provinceHash.get(provinceName);
            cityString = getCitys(guid, file);

            // 省被选中后，初始化城市Spinner
            cityAdapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item, cityString);
            cityAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// 设置下拉风格
            citySpinner.setAdapter(cityAdapter); // 将adapter 添加到spinner中
            citySpinner.setOnItemSelectedListener(new CitySelectedListener());// 添加监听
            citySpinner.setVisibility(View.VISIBLE);// 设置默认

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

        public ProvinceSelectedListener(Context context) {
            this.context = context;
        }

    }

    class CitySelectedListener implements AdapterView.OnItemSelectedListener {

        // 城市被点击的监听事件

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            String cityName = cityString[arg2];
            city = cityName;
            if (cityName.equals("") || cityName == null) {
                cityName = cityString[0];
                cityNo = cityHash.get(cityName);

            } else {
                cityNo = cityHash.get(cityName);
                Log.i("zhiyinqing", "cityNo" + cityNo);
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    }
}
