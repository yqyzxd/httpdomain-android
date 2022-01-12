package com.wind.http.domain;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * created by wind on 2020/4/16:3:54 PM
 */
public class DomainManager {
    private static final String SP_FILE_DOMAIN = "sp_file_domain";
    private static final String DOMAIN_KEY = "domain";
    private static final String PREF_KEY_DOMAIN = "pref_key_domain";
    private List<String> domainList = new ArrayList<>();
    private int index = 0;

    private DomainManager(Context context,String assetsFileName) {
        //读取assets下的domains.json
        BufferedReader bufferedReader = null;
        try {
            InputStream ips = context.getAssets().open(assetsFileName);
            bufferedReader = new BufferedReader(new InputStreamReader(ips));
            String line;
            StringBuilder sBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                sBuilder.append(line);
            }
            domainList.addAll(parse(sBuilder.toString()));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //读取保存在pref中的domain
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_FILE_DOMAIN, Context.MODE_PRIVATE);
        String prefDomains = sharedPreferences.getString(PREF_KEY_DOMAIN, "");

        if (!TextUtils.isEmpty(prefDomains)) {
            domainList.addAll(parse(prefDomains));
        }


      /*  for (String domain:domainList){
            System.out.println("saved domain:"+domain);
        }*/
    }



    private List<String> parse(String json) {
        List<String> domains = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String domain = jsonObject.getString(DOMAIN_KEY);
                domains.add(domain);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return domains;
    }

    private static DomainManager sInstance = null;

    public static void init(Context context) {
        init(context,"domains.json");
    }

    /**
     * 最好在Application onCreate中进行初始化
     * @param context
     * @param assetsFileName
     */
    public static void init(Context context,String assetsFileName) {
        if (sInstance == null) {
            synchronized (DomainManager.class) {
                if (sInstance == null) {
                    sInstance = new DomainManager(context.getApplicationContext(),assetsFileName);
                }
            }

        }
    }
    public static DomainManager getInstance(){
        return sInstance;
    }
    public synchronized String getDomain() {
        if (index >= domainList.size()) {
            return "";
        }
        return domainList.get(index);
    }


    public static boolean save(Context context, List<String> domains) {

        if (context==null || domains == null || domains.isEmpty()) {
            return false;
        }

        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("[");
        for (int i = 0; i < domains.size(); i++) {
            String domain = domains.get(i);
            sBuilder.append("{")
                    .append(DOMAIN_KEY)
                    .append(":")
                    .append("\"")
                    .append(domain)
                    .append("\"")
                    .append("}");
            if (i < domains.size() - 1) {
                sBuilder.append(",");
            }
        }
        sBuilder.append("]");
        String newDomain = sBuilder.toString();
       // System.out.println("newDomain:" + newDomain);
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_FILE_DOMAIN, Context.MODE_PRIVATE);
        String prefDomain = sharedPreferences.getString(PREF_KEY_DOMAIN, "");
        if (!newDomain.equals(prefDomain)) {
            sharedPreferences.edit().clear();
            sharedPreferences.edit()
                    .putString(PREF_KEY_DOMAIN, newDomain)
                    .apply();

        }
        return true;

    }


}
