//Thrift imports
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.server.*;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
 
class SortMerge extends Thread { 
    Task task;
    public SortMerge(Task task) {
        this.task = task;
    }

    @Override
    public void run(){
		try{
			if(task instanceof SortTask){
				sort((SortTask)task);
			} else{
				merge((MergeTask)task);
			}
		} catch(TException e){
			e.printStackTrace();
		}
    }

    public boolean sort(SortTask task) throws TException {
	//got to wrap it up because of IOExceptions.
	Writer wr = null;
	try {
	    System.out.println("Sorting task -> " + task );
	    List<Integer> data = readFileToIntList(task);
	    Collections.sort(data); //magic of abstractions!
	    wr = new BufferedWriter(new FileWriter(new File(task.output)));
	    
	    for(Integer i: data){
		wr.write(i);
		wr.write(" ");
	    }
	    wr.close(); //this should be in the finally block but nested try-catches on the top level is disgraceful
	    return true;
	}
	catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }
    
    public boolean merge(MergeTask task) throws TException {
	Writer wr = null;
	try {
	    wr = new BufferedWriter(new FileWriter(new File(task.output)));
	    Scanner sc1 = new Scanner(new File(task.f1));
	    Scanner sc2 = new Scanner(new File(task.f2));

	    if(sc1.hasNextInt() && sc2.hasNextInt()){
		int a = sc1.nextInt();
		int b = sc2.nextInt();
		while (sc1.hasNextInt() && sc2.hasNextInt()) {
		    if(a < b){
			wr.write(a);
			a = sc1.nextInt();
		    } else { 
			wr.write(b);
			b = sc2.nextInt();
		    }
		    wr.write(" "); //since strings are immutable
		}
	    }
       
	    //write remaining numbers
	    if(sc1.hasNextInt()){
		while(sc1.hasNextInt()){
		    int c = sc1.nextInt();
		    wr.write(c);
		    wr.write(" ");
		}
	    }else{
		while(sc2.hasNextInt()){
		    int c = sc2.nextInt();
		    wr.write(c);
		    wr.write(" ");
		}
	    }
	    wr.close();
	    return true;
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }

    /* Open file as a stream.
       Jump to the startChunk offset
       Read uptil the endChunk.
       Split by space.
       Convert to int arrayList.
    */
    private List<Integer> readFileToIntList(SortTask t) throws Exception {
	int len = (int) (t.endOffset - t.startOffset + 1); //should never overflow since chunksize isn't going to be that big, I think.
	List<Integer> output = new ArrayList<>((int) len);

	BufferedReader rdr = new BufferedReader(new FileReader(t.filename));
	//advance reader to specific position in file
	rdr.skip(t.startOffset);
	//allocate memory and read into buffer.
	char[] buf = new char[(int) len];
	rdr.read(buf, 0, (int) len);

	//Split space delimted String and parse as integers
	String[] split = (new String(buf)).split(" ");
	for(String s : split)	    
	    output.add(Integer.parseInt(s.trim())); //trim because of EOF. Hidden, but length is +1.
	
	System.out.println("RESULT of READFILETOINTLIST: " + output.toString());
	return output;
    }


}