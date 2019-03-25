/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/



//import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.HttpCamera;
import edu.wpi.cscore.UsbCamera; //Keep for future evelopment
//import edu.wpi.cscore.VideoSource;// Keep for pipeline
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.vision.VisionPipeline;
//import edu.wpi.first.vision.VisionThread;// Keep for pipeline
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


import org.opencv.core.Mat;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
//import org.opencv.imgproc.Imgproc; // for rectangle example
//import org.opencv.core.Point; // required for rectangle example
//import org.opencv.core.Scalar; // required for rectangle example

public final class Main {

  public static class CameraConfig {
    public String name;
    public String path;
  }
  

  public static int team=CameraData.Rp3.teamName;
  public static CameraServer inst = CameraServer.getInstance();
  //public static HttpCamera camera0 =new HttpCamera(CameraData.Rp3.cameraStreamName,CameraData.Limelight_Front.path);
 // public static HttpCamera camera1 =new HttpCamera(CameraData.Rp3.cameraStreamName,CameraData.Limelight_Back.path);
 // static String camerasType="HTTP Cameras";


               // Get the UsbCamera from CameraServer
               static UsbCamera camera0 = CameraServer.getInstance().startAutomaticCapture(0); // **** GRIP
               static UsbCamera camera1 = CameraServer.getInstance().startAutomaticCapture(1); // **** GRIP
               static String camerasType="USB Cameras";
               // Get a CvSink. This will capture Mats from the camera
               static CvSink cvSink0 = CameraServer.getInstance().getVideo(camera0); // **** GRIP
               static CvSink cvSink1 = CameraServer.getInstance().getVideo(camera1); // **** GRIP
               // Setup a CvSource. This will send images back to the Dashboard
               static CvSource outputStream = CameraServer.getInstance().putVideo(CameraData.Rp3.cameraStreamName,CameraData.Rp3.UsbCamWidth, CameraData.Rp3.UsbCamHeight); // **** GRIP
               // Mats are very memory expensive. Lets reuse this Mat.
               static Mat mat = new Mat(); // **** GRIP
               static boolean isCvSink0=true;
               //static Scalar scalar=new Scalar(255, 0, 0);// required for rectangle example
                
  public static boolean cameraSelection=true;
  public static boolean cameraSelectionOld=cameraSelection;
 

  private Main() {
  }


  /**
   * Start running the camera
   */
  public static void startCamera() {
    SmartDashboard.putString(CameraData.Rp3.camerasTypeName, camerasType);
    
    camera0.setResolution(CameraData.Rp3.UsbCamWidth, CameraData.Rp3.UsbCamHeight);
    camera1.setResolution(CameraData.Rp3.UsbCamWidth, CameraData.Rp3.UsbCamHeight);
    camera0.setFPS(CameraData.Rp3.fps);
    camera1.setFPS(CameraData.Rp3.fps);
    outputStream.setFPS(CameraData.Rp3.fps);
    
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
    SmartDashboard.putString(CameraData.Rp3.projectNameID, CameraData.Rp3.projectName);
    SmartDashboard.putBoolean(CameraData.Rp3.cameraSwitchID, CameraData.Rp3.isFront);
    SmartDashboard.putString(CameraData.Rp3.cameraSelectID, CameraData.Limelight_Front.name);

    // Shuffelboard special message
    SmartDashboard.putString("Project Name","Limelight Switch V1");

    // start NetworkTables
    NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
     // ntinst.startClientTeam(team); //use for roborio
     ntinst.startClient("robotics01"); // use with non roborio networktables server

      //Start cameras
      startCamera();

   /* if (cameras.size() >= 1) { //keep for pipeline
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

    
                // Set the resolution and frames per second
                

    // loop forever
    for (;;) {
      cameraSelection=SmartDashboard.getBoolean(CameraData.Rp3.cameraSwitchID, CameraData.Rp3.isFront);
      if(cameraSelection != cameraSelectionOld){
        if(cameraSelection==CameraData.Rp3.isFront){
          SmartDashboard.putString(CameraData.Rp3.cameraSelectID, CameraData.Limelight_Front.name);
          isCvSink0=(cvSink0.grabFrame(mat)==0);
          //scalar=new Scalar(0,255,0); // required for rectangle example
          // i If there is an error notify the output.
          if (isCvSink0) {
            // Send the output the error.
            outputStream.notifyError(cvSink0.getError());
          }
        }else{
          SmartDashboard.putString(CameraData.Rp3.cameraSelectID, CameraData.Limelight_Back.name);
          isCvSink0=(cvSink1.grabFrame(mat)==0);
         // scalar=new Scalar(0,0,255);// required for rectangle example
          //  If there is an error notify the output.
          if (isCvSink0) {
            // Send the output the error.
            outputStream.notifyError(cvSink1.getError());
          
        }
        }
      }
      if(cameraSelection){
        isCvSink0=(cvSink0.grabFrame(mat)==0);

      }else{
        isCvSink0=(cvSink1.grabFrame(mat)==0);

      }
      if (!isCvSink0){ // skip on error
        // Place image processing here for both cameras

        // Put a rectangle on the image
        //Imgproc.rectangle(mat, new Point(25, 50), new Point(100, 100), scalar, 2);
        // Give the output stream a new image to display
        outputStream.putFrame(mat); 
      } else {
        isCvSink0=(cvSink0.grabFrame(mat)==0);
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
