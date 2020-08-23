import java.util.LinkedList; 
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;


//Author : Sina Barazandeh

public class Threadexample { 
    public static void main(String[] args) 
        throws InterruptedException, IOException 
    { 


        Scanner in = new Scanner(System.in); 
        System.out.println("#############################################");
        System.out.println("Please Enter The Number Of Processes ...");
        int n = in.nextInt(); 
        System.out.println("Number Of Processes : " + n);
        System.out.println("#############################################");
        System.out.println("Please Enter The Time Quantom In Mili Seconds ...");
        int tq = in.nextInt();
        System.out.println("Time Quantom : " + tq + " (ms)");
        System.out.println("#############################################");
        in.close();

        ArrayList<String> names = new ArrayList<>();
        ArrayList<Integer> arrivalTimes = new ArrayList<>();
        ArrayList<Integer> burstTimes = new ArrayList<>();

        try {
            File myObj = new File("table5.txt");
            Scanner myReader = new Scanner(myObj);
            for(int counter = 0;counter<n;counter++){
                String pname = myReader.nextLine();
                names.add(pname);
                String btime = myReader.nextLine();
                int btimenum = Integer.parseInt(btime);
                burstTimes.add(btimenum);
                String atime = myReader.nextLine();
                int atimenum = Integer.parseInt(atime);
                arrivalTimes.add(atimenum);

            }
        
            myReader.close();
        
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        System.out.println("Process Names : " + names);
        System.out.println("Arrival Times : " + arrivalTimes);
        System.out.println("Burst Times : " + burstTimes);


        ArrayList<Integer> cbtimes = burstTimes;
        ArrayList<Integer> catimes = arrivalTimes;


        final PC_RR pc = new PC_RR(tq, arrivalTimes, burstTimes);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pc.produce();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pc.consume();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        int[] processes = new int[n];

        for(int i = 0;i<n;i++){
            processes[i] = i+1;
        }

        PC_RR.compute("RR.txt",tq,processes,catimes,cbtimes); 

        final PC_SRTF srtf = new PC_SRTF(tq, arrivalTimes, burstTimes);

        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    srtf.produce();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t4 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    srtf.consume();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    
                    e.printStackTrace();
                }
            }
        });

        t3.start();
        t4.start();

        t3.join();
        t4.join();

        PC_SRTF.compute("RR.txt",processes,catimes,cbtimes); 

        final PC_SJF sjf = new PC_SJF(n,arrivalTimes, burstTimes);

        Thread t5 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sjf.produce();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t6 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sjf.consume();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        });

        t5.start();
        t6.start();

        t5.join();
        t6.join();

        PC_SJF.compute("RR.txt",processes,catimes,cbtimes); 
    }

    public static class PC_RR {

        LinkedList<Integer> pTime = new LinkedList<Integer>();
        LinkedList<Integer> pId = new LinkedList<Integer>();

        boolean ready = false;
        boolean done = false;

        ArrayList<Integer> arrivalTimes = new ArrayList<Integer>();
        ArrayList<Integer> burstTimes = new ArrayList<Integer>();
        int timeQuantum;

        public PC_RR(int timequantom, ArrayList<Integer> arrivTimes, ArrayList<Integer> remainTimes) {

            int size = arrivTimes.size();
            for(int i = 0;i<size;i++){
                int v1 = arrivTimes.get(i);
                arrivalTimes.add(v1);
                int v2 = remainTimes.get(i);
                burstTimes.add(v2);
            }
            this.timeQuantum = timequantom;

        }

        public void produce() throws InterruptedException {

            ArrayList<ArrayList<Integer>> results = schedule(timeQuantum, arrivalTimes, burstTimes);

            // System.out.println(results);

            ArrayList<Integer> ids = results.get(0);
            ArrayList<Integer> ts = results.get(1);


            int value = 0;
            int size = ids.size();
            while (value < size) {
                synchronized (this) {
                    while (ready)
                        wait();

                    // Find Which Process Should Run



                    int prId = ids.get(value);
                    pId.add(prId);
                    int pT = ts.get(value);
                    pTime.add(pT);
                    
                    System.out.println("RR Producer : PID : " + prId + " Time : " + pT );


                    value++;

                    ready = true;

                    notify();

                    Thread.sleep(500);
                }
            }
            done = true;
        }

        public void consume() throws InterruptedException, IOException {

            ArrayList<Integer> listIds = new ArrayList<Integer>();
            ArrayList<Integer> listTimes = new ArrayList<Integer>();

            BufferedWriter writer = new BufferedWriter(new FileWriter("RR_GC.txt", true));
            writer.append("PID   Time");
            writer.close();

            while (done == false) {
                synchronized (this) {
                    BufferedWriter writer2 = new BufferedWriter(new FileWriter("RR_GC.txt", true));

                    while (ready == false)
                        wait();


                    int prId = pId.removeFirst();
                    listIds.add(prId);
                    int pT = pTime.removeFirst();
                    listTimes.add(pT);

                    ready = false;

                    System.out.println("RR Consumer : PID : " + prId + " Time : " + pT );

                    writer2.append('\n' + " P" + prId + "    " + pT);
                    writer2.close();
                    notify();

                    Thread.sleep(500);
                }
            }

            
        }


        // Show The Chart on Table

        // static void ganttChart(ArrayList<Integer> ids,ArrayList<Integer> times){
        //     // Draw The Gantt Chart
        //         JFrame f;    

        //         int size = ids.size();
    
        //         f=new JFrame();    
        //         String data[][] = new String[size][2];
                
        //         for(int counter = 0;counter<size;size++){
        //             data[counter][0] = 'P' + Integer.toString(ids.get(counter));
        //             data[counter][1] = Integer.toString(times.get(counter));
        //         }

        //         String column[]={"Process","Time"};         
        //         JTable jt=new JTable(data,column);    
        //         jt.setBounds(30,40,200,300);          
        //         JScrollPane sp=new JScrollPane(jt);    
        //         f.add(sp);          
        //         f.setSize(300,400);    
        //         f.setVisible(true);  

        //         f.setVisible(false);
        //         return;
        // }

        static ArrayList<ArrayList<Integer>> schedule(int tq, ArrayList<Integer> atimes, ArrayList<Integer> btimes) {
            
            ArrayList<Integer> pruntimes = new ArrayList<Integer>();
            
            ArrayList<Integer> pids = new ArrayList<Integer>();
    
            ArrayList<Integer> patimes = atimes; 
            
            ArrayList<Integer> rtimes = btimes;
    
    
            
            int quantum = tq;
                        
            int n = patimes.size();
                    
            int arrival=0;
    
            int c = 0;

            while(true){

                c++;
                if(c<50)
                    System.out.println(rtimes);

                boolean done = true;
                for(int i=0;i<n;i++){ 
                    int rmtime = rtimes.get(i);

                    int atime = atimes.get(i);
    
                    if(rmtime>0){ 
                        done = false;
 
 
                        if(rmtime > quantum && atime<=arrival){ 
                            int rt = rmtime - quantum;
                            System.out.print(rt); 
                            rtimes.set(i, rt);
                            System.out.println("New Remaining Time: " + i);
                            
                            System.out.println(rt);
                            pruntimes.add(quantum);
                            pids.add(i);
    
                            arrival++; 
                        } 
                        else{ 
                            if(atime<=arrival){ 
                                arrival++; 
                                pruntimes.add(rmtime);
                                pids.add(i+1);
    
                                rtimes.set(i,0); 
                            }else{
                                arrival++;
                            } 
                        } 
                        
                    } 
                }
                if(done == true){
                    break;
                }
            }
            
            ArrayList<ArrayList<Integer>> results = new ArrayList<ArrayList<Integer>>(2);
    
    
            results.add(pids);
            results.add(pruntimes);
            
            return results;
    
        }

        static void compute(String filename,int quantum,int[] processes ,ArrayList<Integer> arrivalTimes ,ArrayList<Integer> burstTimes)
                throws IOException {

            int size = arrivalTimes.size();

            int[] btimes = new int[size];
            int[] atimes = new int[size];
 
            for(int i = 0;i<size;i++){

                btimes[i] = burstTimes.get(i).intValue();
                atimes[i] = arrivalTimes.get(i).intValue();
                

            }

            int n = arrivalTimes.size();


            findAvgTime(processes,n,btimes,quantum,atimes);             

        }
        public static void findWaitingTime(int process[],int wt_time[],int n ,int brusttime[],int quantum,int completion_time[],int arrival_time[]){ 
            // copy the value of brusttime array into wt_time array. 
            int rem_time[] = new int[n]; 
            
            for(int i=0;i<wt_time.length;i++){ 
                rem_time[i]= brusttime[i]; 
            } 
            int t=0; 
            int arrival=0; 
            // processing until the value of element of rem_time array is 0 
            while(true){ 

                boolean done = true; 
                for(int i=0;i<n;i++){ 
                    if(rem_time[i]>0){ 
                        done =false; 
                        if(rem_time[i]>quantum && arrival_time[i]<=arrival){ 
                            t +=quantum; 
                            rem_time[i]-=quantum; 
                            arrival++; 
                        } 
                        else{ 
                        if(arrival_time[i]<=arrival){ 
                            arrival++; 
                            t+=rem_time[i]; 
                            wt_time[i] = t-brusttime[i]; 
                            rem_time[i]=0; 
                            completion_time[i]=t; } else{arrival++;t++;}
                        } 
                    } 
                } 
                
                if(done==true)	 
                { 
                    break; 
                    } 
            }	 
        } 
        public static void findTurnAroundTime(int process[] ,int wt_time[],int n,int brusttime[],int tat_time[],int completion_time[],int arrival_time[]){ 
            for(int i=0;i<n;i++){ 
                tat_time[i]= completion_time[i]-arrival_time[i]; 
                wt_time[i] = tat_time[i]-brusttime[i]; 
                
                
            } 
            
        } 
        
        public static void findAvgTime(int process[],int n,int brusttime[],int quantum,int arrival_time[])
                throws IOException {
            int wt_time[] = new int[n]; 
            int tat_time[] = new int[n]; 
            int completion_time[] = new int[n]; 
            findWaitingTime(process,wt_time,n,brusttime,quantum,completion_time,arrival_time);	 
            findTurnAroundTime(process,wt_time,n,brusttime,tat_time,completion_time,arrival_time); 
            int total_wt = 0, total_tat = 0; 
                
            BufferedWriter writer = new BufferedWriter(new FileWriter("RR_AVG.txt", true));
                    
            writer.append("Processes " +" Arrival Time\t"+ " Burst time " +" completion time"+ 
            " Turn Around Time " + " Waiting time");

            System.out.println("Processes " +" Arrival Time\t"+ " Burst time " +" completion time"+ 
                    " Turn Around Time " + " Waiting time"); 
            for (int i=0; i<n; i++) 
            { 
                total_wt = total_wt + wt_time[i]; 
                total_tat = total_tat + tat_time[i]; 

                String s = " " + (i+1) + "\t\t"+ arrival_time[i]+"\t\t"+ + brusttime[i] +"\t " +completion_time[i]+"\t\t"
                +tat_time[i] +"\t\t " + wt_time[i];

                writer.append('\n' + s);

                System.out.println(" " + (i+1) + "\t\t"+ arrival_time[i]+"\t\t"+ + brusttime[i] +"\t " +completion_time[i]+"\t\t"
                                    +tat_time[i] +"\t\t " + wt_time[i]); 
            } 
            
            writer.append("\nAverage waiting time = " + 
            (float)total_wt / (float)n);

            writer.append("\nAverage turn around time = " + 
            (float)total_tat / (float)n);

            System.out.println("Average waiting time = " + 
                                (float)total_wt / (float)n); 
            System.out.println("Average turn around time = " + 
                                (float)total_tat / (float)n); 


            writer.close();


        }  


    }
    
    public static class PC_SJF {

        LinkedList<Integer> pTime = new LinkedList<Integer>();
        LinkedList<Integer> pId = new LinkedList<Integer>();

        boolean ready = false;
        boolean done = false;

        ArrayList<Integer> arrivalTimes = new ArrayList<Integer>();
        ArrayList<Integer> burstTimes = new ArrayList<Integer>();

        static int mat[][];

        public PC_SJF(int n, ArrayList<Integer> arrivTimes, ArrayList<Integer> remainTimes) {

            int size = arrivTimes.size();
            for (int i = 0; i < size; i++) {
                int v1 = arrivTimes.get(i);
                arrivalTimes.add(v1);
                int v2 = remainTimes.get(i);
                burstTimes.add(v2);
            }

            mat = new int[n][6];

        }

        public void produce() throws InterruptedException {

            ArrayList<ArrayList<Integer>> results = schedule(arrivalTimes, burstTimes);

            System.out.println(results);

            ArrayList<Integer> ids = results.get(0);
            ArrayList<Integer> ts = results.get(1);

            int value = 0;
            int size = ids.size();
            while (value < size) {
                synchronized (this) {
                    while (ready)
                        wait();

                    // Find Which Process Should Run


                    int prId = ids.get(value);
                    pId.add(prId);
                    int pT = ts.get(value);
                    pTime.add(pT);

                    System.out.println("SJF Producer : PID : " + prId + " Time : " + pT );


                    value++;

                    ready = true;

                    notify();

                    Thread.sleep(500);
                }
            }
            done = true;
        }

        public void consume() throws InterruptedException, IOException {

            ArrayList<Integer> listIds = new ArrayList<Integer>();
            ArrayList<Integer> listTimes = new ArrayList<Integer>();

            BufferedWriter writer = new BufferedWriter(new FileWriter("SJF_GC.txt", true));
            writer.append("PID   Time");
            writer.close();

            while (done == false) {
                synchronized (this) {
                    BufferedWriter writer2 = new BufferedWriter(new FileWriter("SJF_GC.txt", true));

                    while (ready == false)
                        wait();

                    int prId = pId.removeFirst();
                    listIds.add(prId);
                    int pT = pTime.removeFirst();
                    listTimes.add(pT);



                    // ganttChart(listIds,listTimes);

                    ready = false;

                    System.out.println("SJF Consumer : PID : " + prId + " Time : " + pT );


                    writer2.append('\n' + " P" + prId + "    " + pT);
                    writer2.close();
                    notify();

                    Thread.sleep(500);
                }
            }
        }



        static ArrayList<ArrayList<Integer>> schedule(ArrayList<Integer> atimes, ArrayList<Integer> btimes) {

            ArrayList<Integer> pruntimes = new ArrayList<Integer>();

            ArrayList<Integer> pids = new ArrayList<Integer>();

            ArrayList<Integer> patimes = atimes;

            ArrayList<Integer> rt = new ArrayList<Integer>();

            int n = atimes.size();

            int val = 0;
            for (int i = 0; i < n; i++) {
                val = btimes.get(i);
                rt.add(val);
            }

            int complete = 0, t = 0, minm = 1000;
            int shortest = 0;
            boolean check = false;

            while (true) {
                minm = 1000;

                shortest = 0;

                for (int j = 0; j < n; j++) {
                    int a = patimes.get(j);
   

                    int rmtime = rt.get(j);

                    if (rmtime > 0) {
                        check = true;
                    }



                    if ((a <= t) && (rmtime < minm) && rmtime > 0) {
                        minm = rmtime;
                        shortest = j + 1;
                    }
                }


                if (shortest > 0) {
                    // Reduce remaining time by one
                    int w = rt.get(shortest - 1);
                    rt.set(shortest - 1, 0);
                    pruntimes.add(w);
                    pids.add(shortest);

                    // Update minimum

                    complete++;
                    check = false;

                    // Increment time
                    t++;

                    if (complete == n) {
                        break;
                    }
                } else {
                    pruntimes.add(1);
                    pids.add(shortest);
                    t++;
                }

            }


            ArrayList<ArrayList<Integer>> results = new ArrayList<ArrayList<Integer>>(2);

            results.add(pids);
            results.add(pruntimes);

            return results;

        }

        static void compute(String filename, int[] processes, ArrayList<Integer> arrivalTimes,
                ArrayList<Integer> burstTimes) throws IOException {


            int size = arrivalTimes.size();

            int[] btimes = new int[size];
            int[] atimes = new int[size];

            for (int i = 0; i < size; i++) {

                btimes[i] = burstTimes.get(i).intValue();
                atimes[i] = arrivalTimes.get(i).intValue();

            }

            int n = arrivalTimes.size();



            findAvgTime(processes, n, btimes, atimes);

        }

        static void findAvgTime(int processes[],int n,int burstTimes[],int arrivalTimes[]) throws IOException{
            int pid[] = processes;
            int at[] = arrivalTimes; 
            int bt[] = burstTimes; 
            int ct[] = new int[n]; 
            int ta[] = new int[n];
            int wt[] = new int[n];  
            int f[] = new int[n];  
            int st=0, tot=0;
            float avgwt=0, avgta=0;

    
            boolean a = true;
            while(true)
            {
                int c=n, min=999;
                if (tot == n) 
                    break;
                
                for (int i=0; i<n; i++)
                {

                    if ((at[i] <= st) && (f[i] == 0) && (bt[i]<min))
                    {
                        min=bt[i];
                        c=i;
                    }
                }
                
                if (c==n) 
                    st++;
                else
                {
                    ct[c]=st+bt[c];
                    st+=bt[c];
                    ta[c]=ct[c]-at[c];
                    wt[c]=ta[c]-bt[c];
                    f[c]=1;
                    tot++;
                }
            }
            
            BufferedWriter writer = new BufferedWriter(new FileWriter("SJF_AVG.txt", true));
            
            writer.append("\npid  arrival brust  complete turn waiting");
            System.out.println("\npid  arrival brust  complete turn waiting");
            for(int i=0;i<n;i++)
            {
                avgwt+= wt[i];
                avgta+= ta[i];
                String s =  pid[i]+"\t"+at[i]+"\t"+bt[i]+"\t"+ct[i]+"\t"+ta[i]+"\t"+wt[i];
                writer.append('\n');
                writer.append(s);
                System.out.println(s);
            }

            writer.append("\naverage tat is "+ (float)(avgta/n));
            writer.append("\naverage wt is "+ (float)(avgwt/n));

            System.out.println ("\naverage tat is "+ (float)(avgta/n));
            System.out.println ("average wt is "+ (float)(avgwt/n));

            writer.close();
        }
 

    }


    public static class PC_SRTF {

        LinkedList<Integer> pTime = new LinkedList<Integer>();
        LinkedList<Integer> pId = new LinkedList<Integer>();

        boolean ready = false;
        static boolean done = false;

        ArrayList<Integer> arrivalTimes = new ArrayList<Integer>();
        ArrayList<Integer> burstTimes = new ArrayList<Integer>();

        public PC_SRTF(int timequantom, ArrayList<Integer> arrivTimes, ArrayList<Integer> remainTimes) {

            int size = arrivTimes.size();
            for (int i = 0; i < size; i++) {
                int v1 = arrivTimes.get(i);
                arrivalTimes.add(v1);
                int v2 = remainTimes.get(i);
                burstTimes.add(v2);
            }

        }

        public void produce() throws InterruptedException {

            ArrayList<ArrayList<Integer>> results = schedule( arrivalTimes, burstTimes);

            System.out.println(results);

            ArrayList<Integer> ids = results.get(0);
            ArrayList<Integer> ts = results.get(1);

            int value = 0;
            int size = ids.size();
            while (value < size) {
                synchronized (this) {
                    while (ready)
                        wait();

                    // Find Which Process Should Run

                    System.out.println("Producer produced");

                    int prId = ids.get(value);
                    pId.add(prId + 1);
                    int pT = ts.get(value);
                    pTime.add(pT);

                    value++;

                    ready = true;

                    notify();

                    Thread.sleep(500);
                }
            }
            done = true;
        }

        public void consume() throws InterruptedException, IOException {

            ArrayList<Integer> listIds = new ArrayList<Integer>();
            ArrayList<Integer> listTimes = new ArrayList<Integer>();

            BufferedWriter writer = new BufferedWriter(new FileWriter("SRTF_GC.txt", true));
            writer.append("PID   Time");
            writer.close();

            while (done == false) {
                synchronized (this) {

                    BufferedWriter writer2 = new BufferedWriter(new FileWriter("SRTF_GC.txt", true));

                    while (ready == false)
                        wait();

                    int prId = pId.removeFirst();
                    listIds.add(prId);
                    int pT = pTime.removeFirst();
                    listTimes.add(pT);

                    // ganttChart(listIds,listTimes);

                    ready = false;

                    System.out.println("Consumer Consumed : PID : " + prId + " Time : " + pT );

                    writer2.append('\n' + " P" + prId + "    " + pT);
                    writer2.close();
                    notify();

                    Thread.sleep(500);
                }
            }
            
        }

        static ArrayList<ArrayList<Integer>> schedule( ArrayList<Integer> atimes, ArrayList<Integer> btimes) {

            ArrayList<Integer> pruntimes = new ArrayList<Integer>();

            ArrayList<Integer> pids = new ArrayList<Integer>();

            ArrayList<Integer> patimes = atimes;

            ArrayList<Integer> rt =  new ArrayList<Integer>();



            int n = atimes.size();


            int val = 0 ;
            // Copy the burst time into rt[]
            for (int i = 0; i < n; i++){
                val = btimes.get(i);
                rt.add(val);
            }
                

                
            int complete = 0, t = 0, minm = 1000;
            int shortest = 0;
            boolean check = false;

            while (true) {
                minm = 1000;

                shortest = 0;

                for (int j = 0; j < n; j++) {
                    int a = patimes.get(j);


                    int rmtime = rt.get(j);



                    if(rmtime > 0){
                        check = true;
                    }



                    if ((a <= t) && (rmtime < minm) && rmtime > 0) {
                        minm = rmtime;
                        shortest = j+1;
                    }
                }


                if(shortest > 0){
                    // Reduce remaining time by one
                    int w = rt.get(shortest-1);
                    rt.set(shortest-1,w-1);
                    pruntimes.add(1);
                    pids.add(shortest);

                    // Update minimum
                    minm = rt.get(shortest-1);
                    if (minm == 0)
                        minm = Integer.MAX_VALUE;

                    // If a process gets completely
                    // executed
                    w = rt.get(shortest-1);
                    if (w == 0) {

                        // Increment complete
                        complete++;
                        check = false;

                        // Find finish time of current
                        // process
                        // finish_time = t + 1;

                    }
                    // Increment time
                    t++;

                    if (complete == n) {
                        break;
                    }
                }else{
                    pruntimes.add(1);
                    pids.add(shortest);
                    t++;
                }

            } 


            
            ArrayList<ArrayList<Integer>> results = new ArrayList<ArrayList<Integer>>(2);
    
    
            results.add(pids);
            results.add(pruntimes);
            
            return results;
    
        }

        static void compute(String filename,int[] processes ,ArrayList<Integer> arrivalTimes ,ArrayList<Integer> burstTimes)
                throws IOException {


            int size = arrivalTimes.size();

            int[] btimes = new int[size];
            int[] atimes = new int[size];
 
            for(int i = 0;i<size;i++){

                btimes[i] = burstTimes.get(i).intValue();
                atimes[i] = arrivalTimes.get(i).intValue();
                

            }

            int n = arrivalTimes.size();


            findAvgTime(btimes,atimes,n,processes);             

        }

        static void findWaitingTime(int burstimes[],int arrivaltimes[], int n, int wt[]) 
        { 
            int rt[] = new int[n]; 

            // Copy the burst time into rt[] 
            for (int i = 0; i < n; i++) 
            rt[i] = burstimes[i]; 

            int complete = 0, t = 0, minm = Integer.MAX_VALUE; 
            int shortest = 0, finish_time; 
            boolean check = false; 


            while (complete != n) { 


            for (int j = 0; j < n; j++)  
            { 
                if ((arrivaltimes[j] <= t) && (rt[j] < minm) && rt[j] > 0) { 
                    minm = rt[j]; 
                    shortest = j; 
                    check = true; 
                } 
            } 

            if (check == false) { 
                t++; 
                continue; 
            } 

            // Reduce remaining time by one 
            rt[shortest]--; 

            // Update minimum 
            minm = rt[shortest]; 
            if (minm == 0) 
                minm = Integer.MAX_VALUE; 

            // If a process gets completely 
            // executed 
            if (rt[shortest] == 0) { 

                // Increment complete 
                complete++; 
                check = false; 

                // Find finish time of current 
                // process 
                finish_time = t + 1; 

                // Calculate waiting time 
                wt[shortest] = finish_time - 
                burstimes[shortest] - 
                arrivaltimes[shortest]; 

                if (wt[shortest] < 0) 
                    wt[shortest] = 0; 
                } 
                // Increment time 
                t++; 
            } 
        } 

        // Method to calculate turn around time 
        static void findTurnAroundTime(int burstimes[],int arrivaltimes[], int n, int wt[], int tat[]) { 
            // calculating turnaround time by adding 
            for (int i = 0; i < n; i++) 
            tat[i] = burstimes[i] + wt[i]; 

        } 

        // Method to calculate average time 
        static void findAvgTime(int burstimes[],int arrivaltimes[], int n,int processes[]) throws IOException 
        { 

            BufferedWriter writer = new BufferedWriter(new FileWriter("SRTF_AVG.txt", true));


            int wt[] = new int[n], tat[] = new int[n]; 
            int  total_wt = 0, total_tat = 0; 

            // Function to find waiting time of all 
            // processes 
            findWaitingTime(burstimes,arrivaltimes, n, wt); 

            // Function to find turn around time for 
            // all processes 
            findTurnAroundTime(burstimes,arrivaltimes, n, wt, tat); 

            // Display processes along with all 
            // details 
            writer.append("Processes " + 
            " Burst time " + 
            " Waiting time " + 
            " Turn around time");


            System.out.println("Processes " + 
            " Burst time " + 
            " Waiting time " + 
            " Turn around time"); 

            // Calculate total waiting time and 
            // total turnaround time 
            for (int i = 0; i < n; i++) { 
                total_wt = total_wt + wt[i]; 
                total_tat = total_tat + tat[i]; 
                   
                String s = " " + processes[i] + "\t\t"
                + burstimes[i] + "\t\t " + wt[i] 
                + "\t\t" + tat[i];

                writer.append('\n' + s);
                
                System.out.println(" " + processes[i] + "\t\t"
                + burstimes[i] + "\t\t " + wt[i] 
                + "\t\t" + tat[i]); 
            } 

                System.out.println("Average waiting time = " + 
                (float)total_wt / (float)n);

                writer.append("\nAverage waiting time = " + 
                (float)total_wt / (float)n);
                
                System.out.println("Average turn around time = " + 
                (float)total_tat / (float)n);
                
                writer.append("\nAverage turn around time = " + 
                (float)total_tat / (float)n);

                writer.close();
        } 


    }

} 

