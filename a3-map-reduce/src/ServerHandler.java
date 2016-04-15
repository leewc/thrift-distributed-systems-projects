//Thrift imports
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.server.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerHandler implements Server.Iface {
    
    Queue<Machine> computeNodes;
    Machine self;
    private Integer i_complete; // synchronized counter for completed tasks.
    private Integer i_unique;   // synchronized counter for unique intermediate files

    
    public ServerHandler(Integer port) throws Exception {
        computeNodes = new LinkedList<Machine>();
        
        //Create a Machine data type representing ourselves
        self = new Machine();
        self.ipAddress = InetAddress.getLocalHost().getHostName().toString();		
        self.port = port;   
    }
    
    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Usage: java ServerHandler <port>");
            return;
        }
        try {
            System.out.println("IP Address is " + InetAddress.getLocalHost().toString());
	    //port number used by this node.
            Integer port = Integer.parseInt(args[0]);
	    ServerHandler server = new ServerHandler(port);
            
	    //spin up server
            server.start();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean enroll(Machine machine) throws TException {
        computeNodes.add(machine);
        System.out.println("\n\nList of Compute Nodes : \n\n" + computeNodes.toString());
        return true;
    }

    @Override
    public String compute(String filename, int chunks) throws TException {
	System.out.println("Starting sort job on " + filename + " with " + chunks + " chunks.");

	//process the file by generating chunk metadata
	Queue<SortTask> mockList = new ConcurrentLinkedQueue<>();
	
	int totalTasks = mockList.size();
	for(int i=0; i<totalTasks; i++){
		SortTask task = mockList.poll();
		Machine current = computeNodes.remove();
		
		// Bring it to the back of the queue
		computeNodes.add(current);
		// Do a RPC call.
	}
	

	// Watches the queuefor all tasks for it all to complete.
	while(i_complete < totalTasks){
		Task task = null;
		if(mockList.isEmpty()){
			task = mockList.poll();
		}
		if(task != null){
			// Make a RPC call
		}
	}
	
	// Now merge.
	Queue<MergeTask> mockSortedList = new ConcurrentLinkedQueue<>();
	
	for(int i=0; i<totalTasks; i++){
		MergeTask task = mockSortedList.remove();
		Machine current = computeNodes.remove();
		
		// Bring it to the back of the queue
		computeNodes.add(current);
		// Do a RPC call.
	}
	
	while(i_complete > 1){
		Task task = null;
		if(mockSortedList.isEmpty()){
			task = mockSortedList.poll();
		}
		if(task != null){
			// Make a RPC call
		}
	}
	
		return "NULL";
    }


    @Override
    // RPC Called by the compute nodes when they have done their task
    public boolean announce() throws TException {
	synchronized(i_complete) {
	    i_complete++;
	}
	return true;
    }

    /* ---- PRIVATE HELPER FUNCTIONS ---- */
    private void chunkify(String filename, Integer chunks) {
	// get the file size and do math on the chunks
	// read the file

	// divide up integer stream into chunks
    }

    // reset state for next job
    private void cleanup() {
	i_complete = 0;
	i_unique = 0;
    }


    //Begin Thrift Server instance for a Node and listen for connections on our port
    private void start() throws TException {
        
        //Create Thrift server socket
        TServerTransport serverTransport = new TServerSocket(self.port);
        TTransportFactory factory = new TFramedTransport.Factory();
        Server.Processor processor = new Server.Processor<>(this);

        //Set Server Arguments
        TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverTransport);
        serverArgs.processor(processor); //Set handler
        serverArgs.transportFactory(factory); //Set FramedTransport (for performance)

        //Run server with multiple threads
        TServer server = new TThreadPoolServer(serverArgs);
        
	System.out.println("Server is listening ... ");
        server.serve();
    }
}
