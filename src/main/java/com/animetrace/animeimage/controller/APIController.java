package com.animetrace.animeimage.controller;

import com.animetrace.animeimage.Limit.LimitRequest;
import com.animetrace.animeimage.Utils.Utils;
import com.animetrace.animeimage.service.RedisService;
import com.sun.xml.internal.fastinfoset.util.StringArray;
import org.aspectj.apache.bcel.util.ClassPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Set;


//主API
@RestController
public class APIController {
    public APIController(){

    }
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @GetMapping("/")
    public void Index(HttpServletResponse response) throws IOException {
        Utils.return_Json("API STATUS OK", 200, response);
        return;
    }

    //API
    @RequestMapping("/api")
    @LimitRequest(
            time = 5000L,
            mode = 3,
            count = 3,
            content = "抱歉，您每分钟只可以进行5次搜索！<br>お手数ですが、1分間に5回までしか検索できませんので、ご了承ください。\n"

    )
    public void API(HttpServletRequest request,HttpServletResponse response) throws IOException {
        int type=0; // type 1: json type 0: direct
        String class_name = null;
        String server = null;
        String chart = null;
        String params[] = Utils.getCleanValue(request.getParameter("type"),request.getParameter("class"),request.getParameter("s"));
        if (params[0] != null && params[0].equals("json")){
            type = 1;
        }
        if (params[1] == null){
            class_name = "tuzi";
        }else{
            class_name = params[1];
        }
        if (params[2] == null){
            server = "nahida";
        }else{
            server = params[2];
        }
        chart = class_name+"_"+server;
        try{
            if (!redisTemplate.hasKey(chart)){
                Utils.return_Json("未找到对应类别或服务器号",404,response);
                return;
            }
        }catch (Exception e){
            Utils.return_Json("redis集群异常",500,response);
            return;
        }
        String randomResult = (String) redisTemplate.opsForSet().randomMember(chart);
        int storage = 0;
        if (!randomResult.startsWith("http")){
            storage = 1;
        }
        if (type==1){
            if (storage==0){
                Utils.return_Json(randomResult,200,response,"imgUrl");
                return;
            }else{

            }
        }else{
            if (storage==0){
                response.sendRedirect(randomResult);
                return;
            }else{

            }
        }


    }
    @RequestMapping("/reset")
    public void Reset(HttpServletRequest request,HttpServletResponse response) throws IOException {
        String params[] = Utils.getCleanValue(request.getParameter("keys"));
        if (params[0] == null || !params[0].trim().equals("passwords")){
            Utils.return_Json("Token Error", 403,response);
            return;
        }
        org.springframework.core.io.Resource[] resources = (new PathMatchingResourcePatternResolver()).getResources("files" + "/*");
        String temp = null;
        BufferedReader br = null;
        Set<String> keys = null;
        try{
            keys = redisTemplate.keys("*");
        }catch (Exception e){
            Utils.return_Json("redis集群异常",500,response);
            return;
        }
        for (String key : keys){
            redisTemplate.delete(key);

        }
        for(int i=0;i<resources.length;i++){
            ArrayList<String> result = new ArrayList<>();

            try{
                br = new BufferedReader(new FileReader(resources[i].getFile()));
                temp = null;
                while((temp = br.readLine())!=null){
                    result.add(temp);

                }


            }catch (Exception e){
                if (br != null){
                    br.close();
                }
            }

            Object[] arrayResult = result.toArray();
            for (int j=0;j<arrayResult.length;j++){
                if (arrayResult[j]==null){
                    System.out.println(arrayResult.length);
                }
            }
            redisTemplate.opsForSet().add(resources[i].getFilename().split("\\.")[0], arrayResult);
        }
        Utils.return_Json("所有数据执行完毕",200,response);

    }

    @RequestMapping("/file")
    public void GetImage(HttpServletRequest request,HttpServletResponse response) throws IOException {
        String[] params = Utils.getCleanValue(request.getParameter("path"));
        if (params[0]==null){
            Utils.return_Json("无path输入",403,response);
            return;
        }
        String path = null;
        path = "photoStorage/"+params[0];
        ClassPathResource classPathResource = new ClassPathResource(path);
        String[] temp = classPathResource.getPath().split("/");
        if (!temp[0].equals("photoStorage")){
            Utils.return_Json("违规操作已记录",403,response);
            return;
        }
        File file = null;
        try{
            file = classPathResource.getFile();
            if (!file.exists()){
                Utils.return_Json("文件不存在",403,response);
                return;
            }
        }catch (Exception e){
            Utils.return_Json("文件不存在",403,response);
            return;
        }

//        if (!file.getName().split("\\.")[0].equals("jpg") && !file.getName().split("\\.")[0].equals("png")){
//            Utils.return_Json("图片类型违规",403,response);
//            return;
//        }
        BufferedImage bf = null;
        OutputStream os = null;
        bf = ImageIO.read(new FileInputStream(file));
        response.setContentType("image/png");
        os = response.getOutputStream();
        if (bf != null) {
            ImageIO.write(bf, "png", os);
        }else{
            Utils.return_Json("图片输出错误",500,response);
        }
        if (os != null) {
            os.flush();
            os.close();
        }
        return;
    }
}
