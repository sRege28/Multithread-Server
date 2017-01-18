import java.io.*;
import org.apache.commons.io.IOUtils;
import java.util.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

//*********************************************************
//Listens on Server for incoming messages and files
//*********************************************************
class ServerListener implements Runnable
{
  Socket ser = null;
  InputStream in = null;
  String fp = "C:\\Users\\Sudeep\\workspace\\CN_chatclient\\Client-side\\";
  Path p = Paths.get("").toAbsolutePath();

  
//******************************************************
//Makes a folder to store incoming files from the server 
//******************************************************  
  public void makeFolder()
  {   
	  fp = p.toString()+"\\Client-side\\";
	  File dir = new File(fp);
	  String filename;
	  File[] list = dir.listFiles(new FileFilter(){
		  @Override
		  public boolean accept(File f)
		  {
			  return f.isDirectory();
		  }  
	  });
	  filename = "Client"+list.length;
	  fp += filename;
	  System.out.println("Client folder: "+fp);
      boolean t = new File(fp).mkdir();
  }  
  
  ServerListener(Socket sock)
  {
	 
	 try {
		 ser = sock;
		 in = ser.getInputStream();
		 makeFolder();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		 e.printStackTrace();
	}
	  	  
  }
 
//***********************************
//Saves file from server 
//***********************************
  public void saveFile()
  {  
	  try
	  {
		DataInputStream din = new DataInputStream(in);
		byte buf[] = new byte[8192];
		
	    long fileSize = din.readLong();		  
		String filename = din.readUTF();
		File f = new File(fp+"\\"+filename);
		FileOutputStream fos = new FileOutputStream(f);
		int n;
		
		while ((fileSize>0) && (  (n= din.read(buf, 0, (int)Math.min(buf.length, fileSize) )))   !=-1) 
		{
		  fos.write(buf,0,n);
		  fileSize -= n;
		}
	    fos.flush();
	    fos.close();
	    System.out.println("Saved file "+filename+" in "+fp);  
	  }
	  catch(Exception e)
	  {}
  }

//***********************************
//Listens to server for responses 
//***********************************
  public void run()
  { 
	if(in!=null){ 
	 InputStreamReader isr = new InputStreamReader(in);
	 BufferedReader br = new BufferedReader(isr);
	 boolean done = false;
	 while(!done){
	  try {
		if(br.ready())
		  {
			 String msg =br.readLine(); 
			 if(msg.equals("file"))
			 {   
				 System.out.println("incoming file");
				 saveFile();
			 }
			 else
			  System.out.println(msg); 
		  }
	  
	     } catch (IOException e) 
	  	 {
		   e.printStackTrace();
	  	 }
	 }
	}
	 
	  
	  
  }

}

public class Client {
   
  private static String ipAddress = "127.0.0.1";	
  private static int port = 9091;	
  private static Socket sock = null;
  private static OutputStream out = null;
  private static InputStream in = null;
   
//************************
//Connects to the server 
//************************	
	public static void connect()
	{
		try {
			 sock = new Socket(ipAddress,port);
			 out = sock.getOutputStream();
			 in = sock.getInputStream();		
			} 
		catch (IOException e) 
		{
		  System.out.println("Cannot connect to Server");
		  e.printStackTrace();
	    }
	}
	
//***********************************
// Write messages to server 
//***********************************
	public static void writeToServer(String str)
	{
		PrintWriter toServer = new PrintWriter(out,true);
	    toServer.write(str+"\n");
	    toServer.flush();
	}

//************************
//Writes files to server 
//************************
	public static void writeFileToServer(String filepath) throws FileNotFoundException, IOException
	{   
		File file = new File(filepath);
		long length = file.length();
		byte buf[] = new byte[8192];
		FileInputStream fis = new FileInputStream(file);
		System.out.println("Sending file "+file.getName()+"of length "+file.length());
		
		DataOutputStream dout = new DataOutputStream(out);
		long fileSize = file.length();
		int n;
		// Send the length of the file
		dout.writeLong(file.length());
		//Send the name of the file
		dout.writeUTF(file.getName());
		while ((fileSize>0) && (  (n= fis.read(buf, 0, (int)Math.min(buf.length, fileSize) )))   !=-1) 
				{
				  dout.write(buf,0,n);
				  fileSize -= n;
				}
				dout.flush();
	}

	public static void close()
	{
		writeToServer("quit");
		try
		{
			Thread.sleep(3000);	
			out.close();
			in.close();
			sock.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException
	{   
		connect();
	   	System.out.println("Connected to server with IP address "+ sock.getInetAddress() + " and port no "+ sock.getPort());
	   	Thread list = new Thread( new ServerListener(sock));
	   	list.start();
	   	Scanner sc = new Scanner(System.in);
	    boolean quit = false;
	   	while(!quit){	
		   	
		    String str = sc.nextLine();
		    writeToServer(str);
		    if(str.equals("unicast-F"))
		    {   
		    	System.out.println("Enter recipient:");
		    	String receiver = sc.nextLine();
		    	System.out.println("Enter the file path:");
		      	String fp = sc.nextLine();
		    	try{
		    		writeToServer(receiver);
		    	    writeFileToServer(fp);
		    	   }
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
		    	}
		    }
		    else if(str.equals("broadcast-F"))
		    {
		    	System.out.println("Enter the file path:");
		      	String fp = sc.nextLine();
		    	try{
		    	    writeFileToServer(fp);
		    	   }
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
		    	}
		    }
		    if(str.equals("quit"))
		    {
		    	quit = true;
		    	System.out.println("Exiting loop...");
				    	break;
		    }		    
				    
		}
			close();
			sc.close();
	}
	
}
