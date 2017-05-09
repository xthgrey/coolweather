package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.LogUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by XTH on 2017/5/5.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;//进度条
    private TextView titleText;//对应 choose_area 中的 title_text
    private Button backButton;//对应 choose_area 中的 back_button
    private ListView listView;//对应 choose_area 中的 list_view
    private ArrayAdapter<String> adapter;//字符串数组适配器
    private List<String> dataList = new ArrayList<>();//字符串列表
    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//县列表
    private Province selectedProvince;//选中的省
    private City selectedCity;//选中的市
    private int currentLevel;//当前选中的级别？？不知道在哪里初始化了

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.d("ChooseAreaFragment.onCreateView.start");
        View view = inflater.inflate(R.layout.choose_area, container, false);//动态加载布局文件
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);//将字符串列表载入适配器？？传进去的datalist不知道是什么
        listView.setAdapter(adapter);//将适配器载入列表视图
        LogUtil.d("ChooseAreaFragment.onCreateView.end");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtil.d("ChooseAreaFragment.onCreateView.start");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.d("ChooseAreaFragment.onCreateView.onItemClick.start");
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);//选中的省份
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);//选中的城市
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
                LogUtil.d("ChooseAreaFragment.onCreateView.onItemClick.end");
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d("ChooseAreaFragment.onCreateView.setOnClickListener.start");
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
                LogUtil.d("ChooseAreaFragment.onCreateView.setOnClickListener.end");
            }
        });
        queryProvinces();
    }
    private void queryProvinces(){
        /*
         * 处理布局
         */
        LogUtil.d("ChooseAreaFragment.queryProvinces.start");
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        /*数据处理*/
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0){
            dataList.clear();//清掉原来列表内容
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());//加入省名数据
            }
            adapter.notifyDataSetChanged();//改变适配器内容
            listView.setSelection(0);//取消列表视图选项的选中
            currentLevel = LEVEL_PROVINCE;//当前选中级别为省级
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromService(address,"province");
        }
        LogUtil.d("ChooseAreaFragment.queryProvinces.end");
    }
    private void queryCities(){
        LogUtil.d("ChooseAreaFragment.queryCities.start");
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0){
            dataList.clear();//清掉原来列表内容
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();//改变适配器内容
            listView.setSelection(0);//取消列表视图选项的选中
            currentLevel = LEVEL_CITY;//当前选中级别为省级
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromService(address,"city");
        }
        LogUtil.d("ChooseAreaFragment.queryCities.end");
    }
    private void queryCounties(){
        LogUtil.d("ChooseAreaFragment.queryCounties.start");
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();//清掉原来列表内容
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();//改变适配器内容
            listView.setSelection(0);//取消列表视图选项的选中
            currentLevel = LEVEL_COUNTY;//当前选中级别为省级
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/"+ cityCode;
            queryFromService(address,"county");
        }
        LogUtil.d("ChooseAreaFragment.queryCounties.end");
    }
    /*根据传入的地址和类型从服务器查询数据？？为什么要回到主线程*/
    private void queryFromService(String adress,final String type){
        LogUtil.d("ChooseAreaFragment.queryFromService.start");
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(adress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.d("ChooseAreaFragment.queryFromService.onFailure.start");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.d("ChooseAreaFragment.queryFromService.onFailure.run.start");
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                        LogUtil.d("ChooseAreaFragment.queryFromService.onFailure.run.end");
                    }
                });
                LogUtil.d("ChooseAreaFragment.queryFromService.onFailure.end");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LogUtil.d("ChooseAreaFragment.queryFromService.onResponse.start");
                String responseText = response.body().string();//返回请求的内容
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LogUtil.d("ChooseAreaFragment.queryFromService.onResponse.run.start");
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                            LogUtil.d("ChooseAreaFragment.queryFromService.onResponse.run.end");
                        }
                    });
                }
                LogUtil.d("ChooseAreaFragment.queryFromService.onResponse.end");
            }
        });
    }
    /**
     * 显示进度对话框*/

    private void showProgressDialog(){
        LogUtil.d("ChooseAreaFragment.showProgressDialog.start");
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);//触摸其他区域进度条不消失
        }
        progressDialog.show();
        LogUtil.d("ChooseAreaFragment.showProgressDialog.end");
    }
    /**
     * 关闭进度条*/

    private void closeProgressDialog(){
        LogUtil.d("ChooseAreaFragment.closeProgressDialog.start");
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        LogUtil.d("ChooseAreaFragment.closeProgressDialog.end");
    }
}
