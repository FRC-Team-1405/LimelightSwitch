/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.HttpCamera;
//import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSource;// Keep for pipeline
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.vision.VisionPipeline;
//import edu.wpi.first.vision.VisionThread;// Keep for pipeline
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.opencv.core.Mat;

/*
   JSON format:
   {
       "team": <team number>,
       "ntmode": <"client" or "server", "client" if unspecified>
       "cameras": [
           {
               "name": <camera name>
               "path": <path, e.g. "/dev/video0">
               "pixel format": <"MJPEG", "YUYV", etc>   // optional
               "width": <video mode width>              // optional
               "height": <video mode height>            // optional
               "fps": <video mode fps>                  // optional
               "brightness": <percentage brightness>    // optional
               "white balance": <"auto", "hold", value> // optional
               "exposure": <"auto", "hold", value>      // optional
               "properties": [                          // optional
                   {
                       "name": <property name>
                       "value": <property value>
                   }
               ],
               "stream": {                              // optional
                   "properties": [
                       {
                           "name": <stream property name>
                           "value": <stream property value>
                       }
                   ]
               }
           }
       ]
   }
 */

public final class Main {
  private static String configFile = "/boot/frc.json";

  //@SuppressWarnings("MemberName")
  public static class CameraConfig {
    public String name;
    public String path;
    public JsonObject config;
    public JsonElement streamConfig;
  }

  public static int team=CameraData.Rp3.teamName;
  //public static boolean server;
  public static List<CameraConfig> cameraConfigs = new ArrayList<>();
  public static CameraServer inst = CameraServer.getInstance();
  public static HttpCamera cameraFront =new HttpCamera(CameraData.Rp3.cameraStreamName,CameraData.Limelight_Front.path);
  public static HttpCamera cameraBack =new HttpCamera(CameraData.Rp3.cameraStreamName,CameraData.Limelight_Back.path);
  public static HttpCamera camera=cameraFront;
  public static MjpegServer cameraServer = inst.startAutomaticCapture(camera);
  public static boolean cameraSelection=true;
  public static boolean cameraSelectionOld=cameraSelection;


 

  private Main() {
  }

  /**
   * Report parse error.
   */
  public static void parseError(String str) {
    System.err.println("config error in '" + configFile + "': " + str);
  }

  /**
   * Read single camera configuration.
   */
  public static boolean readCameraConfig(JsonObject config) {
    CameraConfig cam = new CameraConfig();

    // name
    JsonElement nameElement = config.get("name");
    if (nameElement == null) {
      parseError("could not read camera name");
      return false;
    }
   // cam.name = nameElement.getAsString(); default
    cam.name = "Team 1405- dev_video0";


    // path
     JsonElement pathElement = config.get("path");
    if (pathElement == null) {
      parseError("camera '" + cam.name + "': could not read path");
      return false;
    }
    
    cam.path = pathElement.getAsString();
    // Display cam.path in shuffelboard
    cam.path="http://limelight-front.local:5800/";

    // stream properties
    cam.streamConfig = config.get("stream");

    cam.config = config;

    cameraConfigs.add(cam);
    return true;
  }

  /**
   * Read configuration file.
   */
  //@SuppressWarnings("PMD.CyclomaticComplexity")
  public static boolean readConfig() {
    // parse file
    JsonElement top;
    try {
      top = new JsonParser().parse(Files.newBufferedReader(Paths.get(configFile)));
    } catch (IOException ex) {
      System.err.println("could not open '" + configFile + "': " + ex);
      return false;
    }

    // top level must be an object
    if (!top.isJsonObject()) {
      parseError("must be JSON object");
      return false;
    }
    JsonObject obj = top.getAsJsonObject();
    /*
    // team number
    JsonElement teamElement = obj.get("team");
    if (teamElement == null) {
      parseError("could not read team number");
      return false;
    }
    team = teamElement.getAsInt();
    */
    // ntmode (optional)
    /*if (obj.has("ntmode")) {
      String str = obj.get("ntmode").getAsString();
      if ("client".equalsIgnoreCase(str)) {
        server = false;
      } else if ("server".equalsIgnoreCase(str)) {
        server = true;
      } else {
        parseError("could not understand ntmode value '" + str + "'");
      }
    }*/

    // cameras
    JsonElement camerasElement = obj.get("cameras");
    if (camerasElement == null) {
      parseError("could not read cameras");
      return false;
    }
    JsonArray cameras = camerasElement.getAsJsonArray();
    for (JsonElement camera : cameras) {
      if (!readCameraConfig(camera.getAsJsonObject())) {
        return false;
      }
    }

    return true;
  }

  /**
   * Start running the camera
   */
  public static VideoSource startCamera(CameraConfig config) {
    System.out.println("Starting camera '" + config.name + "' on " + config.path);
    CameraServer inst = CameraServer.getInstance();
    MjpegServer server = inst.startAutomaticCapture(camera);

    Gson gson = new GsonBuilder().create();

    camera.setConfigJson(gson.toJson(config.config));
    camera.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen);

    if (config.streamConfig != null) {
      server.setConfigJson(gson.toJson(config.streamConfig));
    }

    return camera;
    
  }

  /**
   * Example pipeline.
   */
  public static class MyPipeline implements VisionPipeline {
    public int val;

    @Override
    public void process(Mat mat) {
      val += 1;
    }
  }

  /**
   * Main.
   */
  public static void main(String... args) {
   /* System.out.println(args[0]);
    if (args.length > 0) {
      configFile = args[0];
    }
    */
    SmartDashboard.putString(CameraData.Rp3.projectNameID, CameraData.Rp3.projectName);
    SmartDashboard.putBoolean(CameraData.Rp3.cameraSwitchID, CameraData.Rp3.isFront);
    SmartDashboard.putString(CameraData.Rp3.cameraSelectID, CameraData.Limelight_Front.name);
    
    // read configuration
    if (!readConfig()) {
      return;
    }

    // Shuffelboard special message
    //SmartDashboard.putString("Project Name","Limelight Switch V1");

    // start NetworkTables
    NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
    /*if (server) {
      System.out.println("Setting up NetworkTables server");
      ntinst.startServer();
    } else {*/
      System.out.println("Setting up NetworkTables client for team " + team);
      ntinst.startClientTeam(team);
   // }

    // start cameras
    List<VideoSource> cameras = new ArrayList<>();
    for (CameraConfig cameraConfig : cameraConfigs) {
      cameras.add(startCamera(cameraConfig));
    }

    // start image processing on camera 0 if present
   /* if (cameras.size() >= 1) {
      VisionThread visionThread = new VisionThread(cameras.get(0),
              new MyPipeline(), pipeline -> {
        // do something with pipeline results
      });
      /* something like this for GRIP:
      VisionThread visionThread = new VisionThread(cameras.get(0),
              new GripPipeline(), pipeline -> {
        ...
      });
       
      visionThread.start();
    }*/

    // loop forever
    for (;;) {
      cameraSelection=SmartDashboard.getBoolean(CameraData.Rp3.cameraSwitchID, CameraData.Rp3.isFront);
      if(cameraSelection != cameraSelectionOld){
        if(cameraSelection==CameraData.Rp3.isFront){
          SmartDashboard.putString(CameraData.Rp3.cameraSelectID, CameraData.Limelight_Front.name);
          camera=cameraFront;
        }else{
          SmartDashboard.putString(CameraData.Rp3.cameraSelectID, CameraData.Limelight_Back.name);
          camera=cameraBack;
        }
        
        cameraServer = inst.startAutomaticCapture(camera);
        SmartDashboard.putStringArray("Array", camera.getUrls());
      }
      cameraSelectionOld=cameraSelection;
    
      try {
        Thread.sleep(CameraData.Rp3.threadSleepTime);
      } catch (InterruptedException ex) {
        return;
      }
    }
  }
}
