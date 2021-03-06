package com.ming.slove.mvnew.tab3.addfollow;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ming.slove.mvnew.R;
import com.ming.slove.mvnew.api.other.OtherApi;
import com.ming.slove.mvnew.app.APPS;
import com.ming.slove.mvnew.model.bean.A1Provice;
import com.ming.slove.mvnew.model.bean.A2City;
import com.ming.slove.mvnew.model.bean.A3County;
import com.ming.slove.mvnew.model.bean.A4Town;
import com.ming.slove.mvnew.model.bean.A5Village;
import com.ming.slove.mvnew.model.bean.Result;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowR2Fragment extends Fragment {

    AppCompatActivity mActivity;
    A1Provice a1Provice;
    A2City a2City;
    A3County a3County;
    A4Town a4Town;
    A5Village a5Village;
    @Bind(R.id.spprovince)
    Spinner spprovince;
    @Bind(R.id.spcity)
    Spinner spcity;
    @Bind(R.id.spcounty)
    Spinner spcounty;
    @Bind(R.id.sptown)
    Spinner sptown;
    @Bind(R.id.spvillage)
    Spinner spvillage;
    @Bind(R.id.line1)
    View line1;
    @Bind(R.id.line2)
    View line2;
    @Bind(R.id.line3)
    View line3;
    @Bind(R.id.line4)
    View line4;
    @Bind(R.id.follow_vallige_btn)
    Button followValligeBtn;

    private String selected_province_name = null;
    private String selected_city_name = null;
    private String selected_country_name = null;
    private String selected_town_name = null;
    private String selected_village_name = null;
    private String vid = null;

    private String auth;

    public FollowR2Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab3_r2, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (AppCompatActivity) getActivity();
        auth = Hawk.get(APPS.USER_AUTH);

        line1.setVisibility(View.GONE);
        spcity.setVisibility(View.GONE);
        line2.setVisibility(View.GONE);
        spcounty.setVisibility(View.GONE);
        line3.setVisibility(View.GONE);
        sptown.setVisibility(View.GONE);
        line4.setVisibility(View.GONE);
        spvillage.setVisibility(View.GONE);
        followValligeBtn.setVisibility(View.GONE);

        //加载省份基本数据
        GetProvinceList();
    }


    /**
     * 获取省列表
     */
    private void GetProvinceList() {
        OtherApi.getService().getCall_Add1(auth)
                .enqueue(new Callback<A1Provice>() {
                    @Override
                    public void onResponse(Call<A1Provice> call, Response<A1Provice> response) {
                        if (response.isSuccessful()) {
                            a1Provice = response.body();
                            if (a1Provice != null) {
                                if (a1Provice.getErr() == 0) {
                                    loadProvinceSpinner();//更新省的spinner adapter
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<A1Provice> call, Throwable t) {

                    }
                });
    }

    /**
     * 省列表的加载与选取
     */
    private void loadProvinceSpinner() {
        //绑定适配器和值
        A1Provice.DataEntity defaultProvince = new A1Provice.DataEntity();
        defaultProvince.setProvice_id("0");
        defaultProvince.setProvice_name("请点击此处选择所在省份");
        ArrayList<A1Provice.DataEntity> provinceList = new ArrayList<>();
        provinceList.add(defaultProvince);
        provinceList.addAll(a1Provice.getData());

        ProvinceListAdapter provinceAdapter = new ProvinceListAdapter(mActivity, provinceList);
        spprovince.setAdapter(provinceAdapter);
        spprovince.setSelection(0, true);
        spprovince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                {
                    if (position != 0) {
                        //加载下一级（城市）
                        String provinceid = ((A1Provice.DataEntity) spprovince.getSelectedItem()).getProvice_id();
                        GetCityList(provinceid);
                        parent.setVisibility(View.VISIBLE);
                        line1.setVisibility(View.VISIBLE);
                        spcity.setVisibility(View.VISIBLE);

                        line2.setVisibility(View.GONE);
                        spcounty.setVisibility(View.GONE);

                        line3.setVisibility(View.GONE);
                        sptown.setVisibility(View.GONE);

                        line4.setVisibility(View.GONE);
                        spvillage.setVisibility(View.GONE);

                        selected_province_name = ((A1Provice.DataEntity) spprovince.getSelectedItem()).getProvice_name();
                    } else {
                        line1.setVisibility(View.GONE);
                        spcity.setVisibility(View.GONE);

                        line2.setVisibility(View.GONE);
                        spcounty.setVisibility(View.GONE);

                        line3.setVisibility(View.GONE);
                        sptown.setVisibility(View.GONE);

                        line4.setVisibility(View.GONE);
                        spvillage.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    /**
     * 获取市级列表
     *
     * @param provinceid 省份id
     */
    private void GetCityList(String provinceid) {
        OtherApi.getService().getCall_Add2(auth, provinceid)
                .enqueue(new Callback<A2City>() {
                    @Override
                    public void onResponse(Call<A2City> call, Response<A2City> response) {
                        if (response.isSuccessful()) {
                            a2City = response.body();
                            if (a2City != null) {
                                if (a2City.getErr() == 0) {
                                    loadCitySpinner();//更新市的spinner adapter
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<A2City> call, Throwable t) {

                    }
                });
    }

    /**
     * 市列表的加载与选取
     */
    private void loadCitySpinner() {
        //绑定适配器和值
        A2City.DataEntity defaultCity = new A2City.DataEntity();
        defaultCity.setCity_id("0");
        defaultCity.setCity_name("请点击此处选择所在城市");
        ArrayList<A2City.DataEntity> cityList = new ArrayList<>();
        cityList.add(defaultCity);
        cityList.addAll(a2City.getData());

        CityListAdapter cityAdapter = new CityListAdapter(mActivity, cityList);
        spcity.setAdapter(cityAdapter);
        spcity.setSelection(0, true);
        spcity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    //加载下一级（城市）
                    String cityid = ((A2City.DataEntity) spcity.getSelectedItem()).getCity_id();
                    GetCountryList(cityid);
                    parent.setVisibility(View.VISIBLE);
                    line1.setVisibility(View.VISIBLE);
                    spcity.setVisibility(View.VISIBLE);

                    line2.setVisibility(View.VISIBLE);
                    spcounty.setVisibility(View.VISIBLE);

                    line3.setVisibility(View.GONE);
                    sptown.setVisibility(View.GONE);

                    line4.setVisibility(View.GONE);
                    spvillage.setVisibility(View.GONE);

                    selected_city_name = ((A2City.DataEntity) spcity.getSelectedItem()).getCity_name();

                } else {
                    line1.setVisibility(View.VISIBLE);
                    spcity.setVisibility(View.VISIBLE);

                    line2.setVisibility(View.GONE);
                    spcounty.setVisibility(View.GONE);

                    line3.setVisibility(View.GONE);
                    sptown.setVisibility(View.GONE);

                    line4.setVisibility(View.GONE);
                    spvillage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 获取县级列表
     *
     * @param cityid 市id
     */
    private void GetCountryList(String cityid) {
        OtherApi.getService().getCall_Add3(auth, cityid)
                .enqueue(new Callback<A3County>() {
                    @Override
                    public void onResponse(Call<A3County> call, Response<A3County> response) {
                        if (response.isSuccessful()) {
                            a3County = response.body();
                            if (a3County != null) {
                                if (a3County.getErr() == 0) {
                                    loadCountrySpinner();//更新县的spinner adapter
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<A3County> call, Throwable t) {

                    }
                });
    }

    /**
     * 县列表的加载与选取
     */
    private void loadCountrySpinner() {
        //绑定适配器和值
        A3County.DataEntity defaultCountry = new A3County.DataEntity();
        defaultCountry.setCounty_id("0");
        defaultCountry.setCounty_name("请点击此处选择所在县区");
        ArrayList<A3County.DataEntity> countryList = new ArrayList<>();
        countryList.add(defaultCountry);
        countryList.addAll(a3County.getData());

        CountryListAdapter countryAdapter = new CountryListAdapter(mActivity, countryList);
        spcounty.setAdapter(countryAdapter);
        spcounty.setSelection(0, true);
        spcounty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    //加载下一级（城市）
                    String countryid = ((A3County.DataEntity) spcounty.getSelectedItem()).getCounty_id();
                    GetTownList(countryid);
                    parent.setVisibility(View.VISIBLE);
                    line1.setVisibility(View.VISIBLE);
                    spcity.setVisibility(View.VISIBLE);

                    line2.setVisibility(View.VISIBLE);
                    spcounty.setVisibility(View.VISIBLE);

                    line3.setVisibility(View.VISIBLE);
                    sptown.setVisibility(View.VISIBLE);

                    line4.setVisibility(View.GONE);
                    spvillage.setVisibility(View.GONE);

                    selected_country_name = ((A3County.DataEntity) spcounty.getSelectedItem()).getCounty_name();
                } else {
                    line1.setVisibility(View.VISIBLE);
                    spcity.setVisibility(View.VISIBLE);

                    line2.setVisibility(View.VISIBLE);
                    spcounty.setVisibility(View.VISIBLE);

                    line3.setVisibility(View.GONE);
                    sptown.setVisibility(View.GONE);

                    line4.setVisibility(View.GONE);
                    spvillage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 获取镇级列表
     *
     * @param countryid 县级id
     */
    private void GetTownList(String countryid) {
        OtherApi.getService().getCall_Add4(auth, countryid)
                .enqueue(new Callback<A4Town>() {
                    @Override
                    public void onResponse(Call<A4Town> call, Response<A4Town> response) {
                        if (response.isSuccessful()) {
                            a4Town = response.body();
                            if (a4Town != null) {
                                if (a4Town.getErr() == 0) {
                                    loadTownSpinner();//更新镇的spinner adapter
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<A4Town> call, Throwable t) {

                    }
                });
    }

    /**
     * 镇列表的加载与选取
     */
    private void loadTownSpinner() {
        //绑定适配器和值
        A4Town.DataEntity defaultTown = new A4Town.DataEntity();
        defaultTown.setTown_id("0");
        defaultTown.setTown_name("请点击此处选择所在乡镇");
        ArrayList<A4Town.DataEntity> townList = new ArrayList<>();
        townList.add(defaultTown);
        townList.addAll(a4Town.getData());

        TownListAdapter townAdapter = new TownListAdapter(mActivity, townList);
        sptown.setAdapter(townAdapter);
        sptown.setSelection(0, true);
        sptown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    //加载下一级（城镇）
                    String townid = ((A4Town.DataEntity) sptown.getSelectedItem()).getTown_id();
                    GetVillageList(townid);
                    parent.setVisibility(View.VISIBLE);
                    line1.setVisibility(View.VISIBLE);
                    sptown.setVisibility(View.VISIBLE);

                    line2.setVisibility(View.VISIBLE);
                    spcounty.setVisibility(View.VISIBLE);

                    line3.setVisibility(View.VISIBLE);
                    sptown.setVisibility(View.VISIBLE);

                    line4.setVisibility(View.VISIBLE);
                    spvillage.setVisibility(View.VISIBLE);

                    selected_town_name = ((A4Town.DataEntity) sptown.getSelectedItem()).getTown_name();
                } else {
                    line1.setVisibility(View.VISIBLE);
                    sptown.setVisibility(View.VISIBLE);

                    line2.setVisibility(View.VISIBLE);
                    spcounty.setVisibility(View.VISIBLE);

                    line3.setVisibility(View.VISIBLE);
                    sptown.setVisibility(View.VISIBLE);

                    line4.setVisibility(View.GONE);
                    spvillage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 获取村级列表
     *
     * @param townid 省份id
     */
    private void GetVillageList(String townid) {
        OtherApi.getService().getCall_Add5(auth, townid)
                .enqueue(new Callback<A5Village>() {
                    @Override
                    public void onResponse(Call<A5Village> call, Response<A5Village> response) {
                        if (response.isSuccessful()) {
                            a5Village = response.body();
                            if (a5Village != null) {
                                if (a5Village.getErr() == 0) {
                                    loadVillageSpinner();//更新村的spinner adapter
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<A5Village> call, Throwable t) {

                    }
                });
    }

    /**
     * 村列表的加载与选取
     */
    private void loadVillageSpinner() {
        //绑定适配器和值
        A5Village.DataEntity defaultVillage = new A5Village.DataEntity();
        defaultVillage.setVillage_id("0");
        defaultVillage.setVillage_name("请点击此处选择所在村庄");
        ArrayList<A5Village.DataEntity> villageList = new ArrayList<>();
        villageList.add(defaultVillage);
        villageList.addAll(a5Village.getData());

        VillageListAdapter villageAdapter = new VillageListAdapter(mActivity, villageList);
        spvillage.setAdapter(villageAdapter);
        spvillage.setSelection(0, true);
        spvillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parent.setVisibility(View.VISIBLE);
                line1.setVisibility(View.VISIBLE);
                spvillage.setVisibility(View.VISIBLE);

                line2.setVisibility(View.VISIBLE);
                spcounty.setVisibility(View.VISIBLE);

                line3.setVisibility(View.VISIBLE);
                sptown.setVisibility(View.VISIBLE);

                line4.setVisibility(View.VISIBLE);
                spvillage.setVisibility(View.VISIBLE);

                followValligeBtn.setVisibility(View.VISIBLE);

                vid = ((A5Village.DataEntity) spvillage.getSelectedItem()).getVillage_id();
                selected_village_name = ((A5Village.DataEntity) spvillage.getSelectedItem()).getVillage_name();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @OnClick(R.id.follow_vallige_btn)
    public void onClick() {
        if (selected_province_name != null &&
                selected_city_name != null &&
                selected_country_name != null &&
                selected_town_name != null &&
                selected_village_name != null) {
            final String villageName = selected_province_name + selected_city_name + selected_country_name + selected_town_name + selected_village_name;
            OtherApi.getService().postCall_FollowVillage(auth, vid)
                    .enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {
                            if (response.isSuccessful()) {
                                Result result = response.body();
                                if (result != null) {
                                    if (result.getErr() == 0) {
                                        Toast.makeText(mActivity, result.getMsg(), Toast.LENGTH_SHORT).show();
                                        mActivity.setResult(Activity.RESULT_OK);
                                        mActivity.finish();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable t) {

                        }
                    });

        } else {
            Toast.makeText(mActivity, "请选择详细的地址！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    public class ProvinceListAdapter extends BaseAdapter {
        private List<A1Provice.DataEntity> mList;
        private Context mContext;

        public ProvinceListAdapter(Context pContext, List<A1Provice.DataEntity> pList) {
            this.mContext = pContext;
            this.mList = pList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_text_sp, null);
            if (convertView != null) {
                TextView tvProvincename = (TextView) convertView.findViewById(R.id.tv_pos_name);
                tvProvincename.setText(mList.get(position).getProvice_name());
                tvProvincename.setTextSize(18.0f);
            }
            return convertView;
        }
    }

    public class CityListAdapter extends BaseAdapter {
        private List<A2City.DataEntity> mList;
        private Context mContext;

        public CityListAdapter(Context pContext, List<A2City.DataEntity> pList) {
            this.mContext = pContext;
            this.mList = pList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_text_sp, null);
            if (convertView != null) {
                TextView tvCityname = (TextView) convertView.findViewById(R.id.tv_pos_name);
                tvCityname.setText(mList.get(position).getCity_name());
                tvCityname.setTextSize(18.0f);
            }
            return convertView;
        }
    }

    public class CountryListAdapter extends BaseAdapter {
        private List<A3County.DataEntity> mList;
        private Context mContext;

        public CountryListAdapter(Context pContext, List<A3County.DataEntity> pList) {
            this.mContext = pContext;
            this.mList = pList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_text_sp, null);
            if (convertView != null) {
                TextView tvCountryname = (TextView) convertView.findViewById(R.id.tv_pos_name);
                tvCountryname.setText(mList.get(position).getCounty_name());
                tvCountryname.setTextSize(18.0f);
            }
            return convertView;
        }
    }

    public class TownListAdapter extends BaseAdapter {
        private List<A4Town.DataEntity> mList;
        private Context mContext;

        public TownListAdapter(Context pContext, List<A4Town.DataEntity> pList) {
            this.mContext = pContext;
            this.mList = pList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_text_sp, null);
            if (convertView != null) {
                TextView tvTownname = (TextView) convertView.findViewById(R.id.tv_pos_name);
                tvTownname.setText(mList.get(position).getTown_name());
                tvTownname.setTextSize(18.0f);
            }
            return convertView;
        }
    }

    public class VillageListAdapter extends BaseAdapter {
        private List<A5Village.DataEntity> mList;
        private Context mContext;

        public VillageListAdapter(Context pContext, List<A5Village.DataEntity> pList) {
            this.mContext = pContext;
            this.mList = pList;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_text_sp, null);
            if (convertView != null) {
                TextView tvVillagename = (TextView) convertView.findViewById(R.id.tv_pos_name);
                tvVillagename.setText(mList.get(position).getVillage_name());
                tvVillagename.setTextSize(18.0f);
            }
            return convertView;
        }
    }
}
