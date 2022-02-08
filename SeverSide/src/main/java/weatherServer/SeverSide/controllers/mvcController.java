package weatherServer.SeverSide.controllers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import weatherServer.SeverSide.Constants;
import weatherServer.SeverSide.model.cityModel;
import weatherServer.SeverSide.model.weatherModel;

@Controller

public class mvcController {
    private static final Logger logger = Logger.getLogger(mvcController.class.getName());
    @PostMapping("/show")
    public String showName(@RequestBody MultiValueMap<String,String> form, Model model){
        List<weatherModel> list = new ArrayList<weatherModel>();
        cityModel cM= new cityModel();
        cM.setName(form.getFirst("cityName"));
        model.addAttribute("cityName",form.getFirst("cityName"));
        model.addAttribute("city",cM);
        URI myUri=URI.create(Constants.BaseURL+"?q="+cM.getName().replace(" ","+")+"&appid="+Constants.APIKey+"&units=metric");
        RequestEntity<Void> req = RequestEntity
                                        .get(myUri)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .build();
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp=template.exchange(req,String.class);
        logger.log(Level.INFO,">>>request from openweather");
        try(InputStream is = new ByteArrayInputStream(resp.getBody().getBytes(StandardCharsets.UTF_8))){
            JsonReader reader = Json.createReader(new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8)));
            JsonObject data = reader.readObject();
            JsonArray weatherFromWeb=data.getJsonArray("weather");
            String Temp = data.getJsonObject("main").get("temp").toString();
            String timeStamp = Constants.getTimeStamp();
            String longitude = data.getJsonObject("coord").get("lon").toString();
            String Latitude = data.getJsonObject("coord").get("lat").toString();

            for(JsonValue j:weatherFromWeb){
                list.add(weatherModel.create(j.asJsonObject(), Temp,"metric",cM.getName(),timeStamp,longitude,Latitude));
            }
            model.addAttribute("weather",list.get(0));
        }catch(Exception e){
            logger.log(Level.INFO,">>>%s".formatted(e));
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }

        return "showItem";
    }

    @GetMapping(path = "/api/weather/{cityName}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findCityJson(@PathVariable String cityName){
        List<weatherModel> list = new ArrayList<weatherModel>();
        String cName=cityName.toLowerCase();
        URI myUri=URI.create(Constants.BaseURL+"?q="+cName.replace(" ","+")+"&appid="+Constants.APIKey+"&units=metric");
        RequestEntity<Void> req = RequestEntity
                                        .get(myUri)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .build();
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp=template.exchange(req,String.class);
        try(InputStream is = new ByteArrayInputStream(resp.getBody().getBytes(StandardCharsets.UTF_8))){
            JsonReader reader = Json.createReader(new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8)));
            JsonObject data = reader.readObject();
            JsonArray weatherFromWeb=data.getJsonArray("weather");
            String Temp = data.getJsonObject("main").get("temp").toString();
            String timeStamp = Constants.getTimeStamp();
            String longitude = data.getJsonObject("coord").get("lon").toString();
            String Latitude = data.getJsonObject("coord").get("lat").toString();
            for(JsonValue j:weatherFromWeb){
                list.add(weatherModel.create(j.asJsonObject(), Temp,"metric",cName,timeStamp,longitude,Latitude));
            }
            return ResponseEntity.ok(list.get(0).toJson().toString());
        }
        catch(Exception e){
            logger.log(Level.INFO,">>>%s".formatted(e));
          
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("error"); 
        
    }

}
