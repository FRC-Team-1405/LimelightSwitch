/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/




public final class CameraData {
  // Limelight - front
  public static class Limelight_Front{
    static final String name= "Front Camera";
    static final String path="http://limelight-front.local:5800";
    
  }
  
  // Limelight - back
  public static class Limelight_Back{
    static final String name= "Rear Camera";
    static final String path="http://limelight-Back.local:5800";
    
  }
  // RP3
  public static class Rp3{
    static final String projectNameID= "Project";
    static final String cameraStreamName= "Camera Stream";
    static final String name= "Limelight Switched";
    static final int teamName=1405;
    static final String projectName="Limelight Switch V2";
    static final boolean isFront= true;
    static final long  threadSleepTime= 100;
    static final String cameraSwitchID="Camera Switch";
    static final String cameraSelectID="Camera";
    
  }

}
