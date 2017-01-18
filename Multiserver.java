import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

//***********************************************
//ClientServer thread listens on a socket to parse 
//requests and sends it to Main Thread
//***********************************************

class ClientServerThread implements Runnable
{
  Socket cli = null;
  private InputStream in = null;
  //private OutputStream out = null;
  int index;
  Multiserver server =null;
  
  Path p = Paths.get("").toAbsolutePath();
  String fp = p.toString();
  
  ClientServerThread(int index,Socket cli, Multiserver server)
  {
	  this.cli = cli;
	  this.index = index;
	  this.server = server;
	  
  }	

  
  
//***********************************
// Opens a new input stream to listen 
//***********************************
  public void open()
  {

	  try{	
		in = cli.getInputStream();
	     }
	  catch(Exception e)
	  {
		  System.out.println("Cannot accept connection"+ e);
	  }
	  
  }
 

//***********************************
// Reads data from the socket 
//***********************************
   public String read() throws SocketException,IOException
  {  
  	String s ="";
  	
  	BufferedReader br = new BufferedReader(new InputStreamReader(in));
		s=br.readLine();  	
		return s;
  }
 

//***********************************
// Reads a file from the client 
//***********************************
  public String readFileFromClient()
  {   
	  int n;
	  String ret = null;
	  try { 
		    
		    DataInputStream din = new DataInputStream(in);
		    byte buf[] = new byte[8192];
		   
		    //Get length of the file
		    long fileSize = din.readLong();
		    
		    //Get the name of the file
		    String filename = din.readUTF();
		    File f = new File(fp+"\\ServerDB\\"+filename);
		    FileOutputStream fos = new FileOutputStream(f);
		    System.out.println(filename);
  		    
		    //Read file
		    while ((fileSize>0) && (  (n= din.read(buf, 0, (int)Math.min(buf.length, fileSize) )))   !=-1) 
			{
			  fos.write(buf,0,n);
			  fileSize -= n;
			}
		    fos.flush();
		    fos.close();
		    
		    //Return file path
		    ret = f.getAbsolutePath();
	      } 
	  catch (IOException e) 
	  {
		   e.printStackTrace();
		   
	  }
	  
	  	return ret;
  }

  public void run()
  {
	  
	  open();
	  
	  boolean done = false;
   	  while(!done)
   	  {
   		   String s=""; 
	       try {
	    	    
	    	    s = read();
		        
		        if(s.equals("unicast-F"))
		        {   
		        	String receiver = read();
		        	String file = readFileFromClient();
		        	if(file.isEmpty())
		        	{
		        		System.out.println("File not created");
		        	}
		        	else
		        	{
		             s = "unicast-F "+file+" "+receiver;
		            
		        	}
		        
		        }	
		        else if(s.equals("broadcast-F"))
		        {
		        	String file = readFileFromClient();
		        	if(file.isEmpty())
		        	{
		        		System.out.println("File not created");
		        	}
		            else
		        	  {
		        		s = "broadcast-F "+file;
		        	  }
		        }
		        
		    	
	        	else if(s.equals("quit"))
	 	         {
	 	    	   done = true;
	 	    	 }
		        
		        server.handle(s, index);
	           } 
	       catch (SocketException se)
	          {
	    	    System.out.println("Client"+index+"disconnected suddenly, closing socket");
				//server.handle("quit", index);
				done = true;
	          } 
	       catch (IOException e) 
	           {
				e.printStackTrace();
			   }  
	       
	       
  	
   	}
   	  
   
   	  
	  
  }
  
}



//*********************************************************
//Main thread handling client connections, requests and
// responses
//*********************************************************
public class Multiserver
{
   private static ServerSocket serv = null;
   private static int sno = 9091;
   private List<Socket> clients = new ArrayList<Socket>();
   private final Object lock = new Object();
   

//***********************************
// Initiates a new ServerSocket
//***********************************
   public void initiate()
   {
	   try {
		    serv = new ServerSocket(sno);
		    
	       } 
	   catch (IOException e) 
	       {		
		     e.printStackTrace();
	       }
	   
   }
   
   
//************************************************
// Listens for incoming connections and creates
// new CSThreads to handle their requests   
//************************************************
   public void listen()
   {   
	   int index = -1;
	   try {
		     Socket sock = serv.accept();
		     synchronized(lock)
		     {
		      clients.add(sock);
		       index = clients.indexOf(sock);
		     }
		     if(index!=-1)
		     {
		      System.out.println("Adding new client Client"+index);
		      blockcast(index,"Adding new client Client"+index);
		      unicast(index,"Your Client ID is "+index);
		      Thread client = new Thread( new ClientServerThread(index,sock,this));
			  client.start();
		     }
	       } 
	   catch (IOException e) 
	   {
	     e.printStackTrace();
	   }
	   
   }
   
   
//***********************************
// Closes socket and removes client
// from the List   
//***********************************   
   public void closeConnection(int index) throws Exception
   {
 	  synchronized(lock)
 	  {
 		 
 		  Socket client = clients.get(index);
 		  if(!client.isClosed())
 		  {
 			  client.close();
 		  }		  		  
 		  clients.remove(index);
 	  }	  
 	  
 	  
   }
   
   
   
//*********************************************************
//Handles the actual requests from the ClientServer Thread
// and sends data to be unicasted or broadcasted
//********************************************************* 
   public void handle(String s, int sender)
   {  
	   String inp[] = s.split(" ");
	   String command = inp[0];
	   String msg ="Client"+sender+": ";
	   int recvr;
	   
	   switch(command)
	   {
	   case "broadcast":
		   for(int i = 1; i< inp.length; i++)
		   {
			   msg+=" "+inp[i];
		   }
		   
	       broadcast(msg);
	       break;
	       
	   case "unicast":
		   
		   try{
		      recvr = Integer.parseInt(inp[1]);
		      for(int i = 2; i< inp.length; i++)
			   {
				   msg+=" "+inp[i];
			   }
		       unicast(recvr,msg);
		      }
		   catch(NumberFormatException NFE)
		   {
			   recvr = sender;
			   msg = "Enter a valid client to unicast to.";
			   System.out.println(NFE);
			   unicast(recvr, msg);
		   }
		   break;
		 
	   case "unicast-F":
		   
		   try
		   {   
			 recvr = Integer.parseInt(inp[2]);
			 String path = inp[1];
			 System.out.println(path);
			 unicast(recvr, "file");
			 unicastFile(recvr, path);
		   }
		   catch(NumberFormatException NFE)
		   {
			   recvr = sender;
			   msg = "Enter a valid client to unicast to.";
			   unicast(recvr, msg);
		   }
		   catch(FileNotFoundException FNFE)
		   {
			   FNFE.printStackTrace();
		   }  
		   catch(IOException IOE)
		   {
			   unicast(sender, "File cannot be sent");
		   }
		   break;
		   
	   case "broadcast-F":
		   try
		   {
			 String path = inp[1];
			 broadcast("file");
			 broadcastFile(path);
		   }
		   catch(NumberFormatException NFE)
		   {
			   recvr = sender;
			   msg = "Enter a valid client to broadcast to.";
			   unicast(recvr, msg);
		   }
		   catch(FileNotFoundException FNFE)
		   {
			   FNFE.printStackTrace();
		   }  
		   catch(IOException IOE)
		   {
			   unicast(sender, "File cannot be sent");
		   }
		   break;
	   case "blockcast":
		   try{
			     recvr = Integer.parseInt(inp[1]);
			      for(int i = 2; i< inp.length; i++)
				   {
					   msg+=" "+inp[i];
				   }
				   blockcast(recvr, msg);
			      }
			   catch(Exception NFE)
			   {
				   recvr = sender;
				   msg = "Enter a valid client to unicast to.";
				   System.out.println(NFE);
				   unicast(recvr, msg);
			   }	   
		   break;
	   case "quit":
		   try {
			closeConnection(sender);
			broadcast("Client "+sender+" has left");
		} catch (Exception e) {
			
			e.printStackTrace();
			unicast(sender, e.toString());
		}
		   break;
	   default:
		   unicast(sender, "Incorrect or wrong command: "+command);
		   
	   }
	   
	   
   }
//*********************************************************
// Unicasts given message to intended recipient
//*********************************************************    
   public void unicast(int index, String msg)
   {
 	  if(index < clients.size())
 	  {
 		  Socket recv = clients.get(index);
 		  
 		  try {
 				OutputStream out = recv.getOutputStream();
 				PrintWriter toCli = new PrintWriter(out, true);
 				toCli.println(msg);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
 	  } 
 	  else
 	  {
 		  System.out.println("No such client");
 		  
 	  }
   }
 //*********************************************************
// Broadcasts given message to all clients
//*********************************************************   
  public void broadcast(String msg)
  {
    for(Socket s : clients)
	 { 
		try {
			OutputStream out = s.getOutputStream();
			PrintWriter toCli = new PrintWriter(out, true);
			toCli.println(msg);
		    } 
		catch (IOException e)
		   {
			// TODO Auto-generated catch block
			e.printStackTrace();
		   }  
	 }
  }
//*********************************************************
//Blockcasts given message to intended recipient
//*********************************************************   
  public void blockcast(int index, String msg)
  {
	  
	  for(int i =0; i< clients.size();i++)
	  { 
		if(i== index)
				continue;
		else{
			 Socket s = clients.get(i);
			try {
				OutputStream out = s.getOutputStream();
				PrintWriter toCli = new PrintWriter(out, true);
				toCli.println(msg);
				} catch (IOException e) {
			e.printStackTrace();
		}  
		} 
		  
	  }
  }
//*********************************************************
//Unicasts given file to intended recipient
//*********************************************************  
  public void unicastFile(int index, String filepath) throws FileNotFoundException, IOException
  {
	  if(index < clients.size())
	  {
		  Socket recv = clients.get(index);
		  OutputStream out = recv.getOutputStream();
		  DataOutputStream dout = new DataOutputStream(out);
		  byte buf[] = new byte[8192];
		  File file = new File(filepath);
		  String filename = file.getName();		  
		  long fileSize = file.length();
		  FileInputStream fis = new FileInputStream(file);
		  int n;
		  
		  
		  dout.writeLong(fileSize);
		  
		  dout.writeUTF(filename);
		  
		  while ((fileSize>0) && (  (n= fis.read(buf, 0, (int)Math.min(buf.length, fileSize) )))   !=-1) 
			{
			  dout.write(buf,0,n);
			  fileSize -= n;
			}
			dout.flush();
		    
	  }	  
  }

//*********************************************************
//Broadcasts given file to intended recipient
//*********************************************************  
  public void broadcastFile(String filepath) throws FileNotFoundException, IOException
  {
	  for(int i = 0; i < clients.size(); i++)
	  {
		  unicastFile(i, filepath);
	  }
  
  }
 
  
   public static void main(String args[])
   { 
	  Multiserver server; 
	  server = new Multiserver();
	   
	   server.initiate();
	   while(true)
	   {
		   server.listen();
	   }
	   
	   
   }	
	
	
	
	
	
	
}
