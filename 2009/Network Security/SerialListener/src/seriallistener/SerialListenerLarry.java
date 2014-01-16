/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package seriallistener;

/**
 *
 * @author Administrator
 */
// derived from SUN's examples in the javax.comm package
import java.io.*;
import java.util.*;
//import javax.comm.*; // for SUN's serial/parallel port libraries
import gnu.io.*; // for rxtxSerial library
import java.nio.ByteBuffer;

public class SerialListenerLarry implements Runnable, SerialPortEventListener {
   static CommPortIdentifier portId;
   static CommPortIdentifier saveportId;
   static Enumeration        portList;
   InputStream           inputStream;
   SerialPort           serialPort;
   Thread           readThread;

   static String        messageString = "Hello, world!";
   static OutputStream      outputStream;
   static boolean        outputBufferEmptyFlag = false;

   static String channel1 = "";
   static String channel2 = "";

   public SerialListenerLarry()
   {
      boolean           portFound = false;
      String           defaultPort;
      
      // determine the name of the serial port on several operating systems
      String osname = System.getProperty("os.name","").toLowerCase();
      if ( osname.startsWith("windows") ) {
         // windows
         defaultPort = "COM1";
      } else if (osname.startsWith("linux")) {
         // linux
        defaultPort = "/dev/ttyUSB0";
      } else if ( osname.startsWith("mac") ) {
         // mac
         defaultPort = "????";
      } else {
         System.out.println("Sorry, your operating system is not supported");
         return;
      }
          
      //if (args.length > 0) {
     //    defaultPort = args[0];
     // } 

      System.out.println("Set default port to "+defaultPort);
      
		// parse ports and if the default port is found, initialized the reader
      portList = CommPortIdentifier.getPortIdentifiers();
      while (portList.hasMoreElements()) {
         portId = (CommPortIdentifier) portList.nextElement();
         if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
            if (portId.getName().equals(defaultPort)) {
               System.out.println("Found port: "+defaultPort);
               portFound = true;
               // init reader thread
               SerialListenerLarry reader = new SerialListenerLarry();
            } 
         } 
         
      } 
      if (!portFound) {
         System.out.println("port " + defaultPort + " not found.");
      } 
      
   } 

   public void initwritetoport() {
      // initwritetoport() assumes that the port has already been opened and
      //    initialized by "public nulltest()"

      try {
         // get the outputstream
         outputStream = serialPort.getOutputStream();
      } catch (IOException e) {}

      try {
         // activate the OUTPUT_BUFFER_EMPTY notifier
         serialPort.notifyOnOutputEmpty(true);
      } catch (Exception e) {
         System.out.println("Error setting event notification");
         System.out.println(e.toString());
         System.exit(-1);
      }
      
   }

   public void writetoport() {
      System.out.println("Writing \""+messageString+"\" to "+serialPort.getName());
      try {
         // write string to serial port
         outputStream.write(messageString.getBytes());
      } catch (IOException e) {}
   }


   public void run() {
      // first thing in the thread, we initialize the write operation
      //initwritetoport();
      try {
         while (true) {
             // initalize serial port
      try {
         serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
      } catch (PortInUseException e) {}
   
      try {
         inputStream = serialPort.getInputStream();
      } catch (IOException e) {}
   
      try {
         serialPort.addEventListener(this);
      } catch (TooManyListenersException e) {}
      
      // activate the DATA_AVAILABLE notifier
      serialPort.notifyOnDataAvailable(true);
   
      try {
         // set port parameters
         serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, 
                     SerialPort.STOPBITS_1, 
                     SerialPort.PARITY_NONE);
      } catch (UnsupportedCommOperationException e) {}
      
      // start the read thread
      readThread = new Thread(this);
      readThread.start();
         }
      } catch (Exception e) {}
   } 

   public void serialEvent(SerialPortEvent event) {

      String binaryString;
      byte toIntTmpArry1[] = new byte[4];
      int intVal;
      float volt;


      switch (event.getEventType()) {
      case SerialPortEvent.BI:
      case SerialPortEvent.OE:
      case SerialPortEvent.FE:
      case SerialPortEvent.PE:
      case SerialPortEvent.CD:
      case SerialPortEvent.CTS:
      case SerialPortEvent.DSR:
      case SerialPortEvent.RI:
      case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
         break;
      case SerialPortEvent.DATA_AVAILABLE:
         // we get here if data has been received
         byte[] readBuffer = new byte[4];
         try {
            // read data
				while (inputStream.available() >= 4)
				{
            	int numBytes = inputStream.read(readBuffer);
            	// print data

					String byte1 = format(readBuffer[0]);
					String byte2 = format(readBuffer[1]);
					String byte3 = format(readBuffer[2]);
					String byte4 = format(readBuffer[3]);

                    binaryString =  byte2 + " | " + byte3 + " | " + byte4;


                    double  voltval;
                    long anUnsignedInt = 0;
                    
                    int firstByte = 0;
                    int secondByte = 0;
                    int thirdByte = 0;
                    int fourthByte = 0;

                    
                    firstByte = (0x000000FF & ((int)readBuffer[1]));
                    secondByte = (0x000000FF & ((int)readBuffer[2]));
                    thirdByte = (0x000000FF & ((int)readBuffer[3]));
                    //fourthByte = (0x000000FF & ((int)buf[index+3]));
                    anUnsignedInt  = ((long) (firstByte << 16
                        | secondByte << 8
                        | thirdByte))
                       & 0xFFFFFFFFL;

                    String binaryString2 = Long.toBinaryString(anUnsignedInt);

                    if(byte2.substring(0,1).equals("1"))  /* MSBit = sign : 1 = positive, 0 = negative */
                    {
                           anUnsignedInt = anUnsignedInt - 0x800000;
                    }
                    else
                    {
                    /* must subtract data FIRST */
                            anUnsignedInt =  0 - (0x800000 - anUnsignedInt);
                    }


                    voltval = (double) (anUnsignedInt * (2.9802325940409414817025043609744e-7));

                    if (byte1.equals("01000001"))
                    {
                        channel1 = "Channel 1 : Bin: " + binaryString + " Int Bin: " + binaryString2 + " Volatge: "+ voltval;
                        System.out.print("\f");
                        System.out.println(channel1);
                        System.out.println(anUnsignedInt);
                    }
                    else
                    {
                        channel2 = "Channel 2 : Bin: " + binaryString + " Voltage: "+ voltval;
                        //System.out.println(channel1 + "\t" + channel2);
                    }

                    /*
					byte toIntTmpArry[] = new byte[4];
					toIntTmpArry = readBuffer;
					toIntTmpArry[0] = 0x00;
					System.out.println(bytesToInt(toIntTmpArry));
                    */
					
					
				}
         } catch (IOException e) {}
   
         break;
      }
   }
	
	public String format(byte b)
	{
		String binaryString = Integer.toBinaryString(b & 0xFF);

		while(binaryString.length() < 8) binaryString = "0" + binaryString;

		return binaryString;
	}

	int bytesToInt(byte[] intBytes){
		ByteBuffer bb = ByteBuffer.wrap(intBytes);
		return bb.getInt();
	}

}

