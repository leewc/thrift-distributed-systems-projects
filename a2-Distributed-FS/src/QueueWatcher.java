//started by Coordinator to monitor the requests queue
//let's just do one for now because more threads and runnables on top of Thrift's mutlithreading
//  is a scary beast I have not tamed.
import java.util.*;
import java.nio.ByteBuffer;

class QueueWatcher extends Thread {
    private static int instance = 0;
    private final Queue<Request> requests; //synchronized
    private final Set<Request> subscriptions; //NOT synchronized, ServerHandler threads wait here.
    private final Coordinator coordinatorInstance;

    public QueueWatcher(Queue<Request> requests, Set<Request> subscriptions, Coordinator coordinatorInstance) {
	this.requests = requests;
	this.subscriptions = subscriptions;
	this.coordinatorInstance = coordinatorInstance;
	setName("QueueWatcher Thread: " + (instance++));
    }

    @Override
    public void run() {
	while(true) {
	    try {
		Request req = null;
		System.out.println("Watcher : entering synch");
		synchronized (requests) {
		    while(requests.isEmpty())
			requests.wait();

		    req = requests.remove();
		    subscriptions.add(req);
		}
	    }
	    catch(Exception e) {
		e.printStackTrace();
		break;
	    }
	}
    }
    /*
    private void process(Request req) throws Exception {
	if(req.type.equals("read")) {

	    subscriptions.put(req, new ReadResponse(req.origin, success, result));

	    ByteBuffer result = coordinatorInstance.read(req.filename);
	    Boolean success = (result != null) ? true : false;

	    System.out.println("Status of read: " + success + ". Putting read Response");
	    
	    
	}

	else if(req.type.equals("write")) {
	    //type cast to access member variable
	    WriteRequest wreq = (WriteRequest) req;
	    Boolean success = coordinatorInstance.write(wreq.filename, wreq.contents);
	    System.out.println("Status of write: " + success + ". Putting write Response");
	    response.put(req, new WriteResponse(req.origin, success));
	}

	else 
	    throw new Exception("Unknown Request");
    }
    */
}