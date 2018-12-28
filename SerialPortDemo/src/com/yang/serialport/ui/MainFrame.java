/*
 * MainFrame.java
 *
 * Created on 2016.8.19
 */

package com.yang.serialport.ui;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.yang.serialport.exception.NoSuchPort;
import com.yang.serialport.exception.NotASerialPort;
import com.yang.serialport.exception.PortInUse;
import com.yang.serialport.exception.ReadDataFromSerialPortFailure;
import com.yang.serialport.exception.SendDataToSerialPortFailure;
import com.yang.serialport.exception.SerialPortInputStreamCloseFailure;
import com.yang.serialport.exception.SerialPortOutputStreamCloseFailure;
import com.yang.serialport.exception.SerialPortParameterFailure;
import com.yang.serialport.exception.TooManyListeners;
import com.yang.serialport.manage.SerialPortManager;
import com.yang.serialport.utils.ByteUtils;
import com.yang.serialport.utils.ShowUtils;

import org.sqlite.*;

import com.yang.serialport.ui.*;

public class MainFrame extends JFrame {
	public Client client = new Client(this);
	public TcpClientHandler TC = new TcpClientHandler();
	Connection c = null;
    Statement stmt = null;
    // 输出流对象
    OutputStream outputStream;
    // Socket变量
    private Socket socket=null;
    private Socket websocketlink=null;
    private ServerSocket serverSocket = null;
    public String IP;
    public Timer tExit = null; 

	/**
	 * 程序界面宽度
	 */
	public static final int WIDTH = 500;

	/**
	 * 程序界面高度
	 */
	public static final int HEIGHT = 360;

	private JTextArea dataView = new JTextArea();
	private JScrollPane scrollDataView = new JScrollPane(dataView);

	// 串口设置面板
	private JPanel serialPortPanel = new JPanel();
	private JLabel serialPortLabel = new JLabel("串口");
	private JLabel baudrateLabel = new JLabel("波特率");
	private JComboBox commChoice = new JComboBox();
	private JComboBox baudrateChoice = new JComboBox();

	// 操作面板
	private JPanel operatePanel = new JPanel();
	private JTextField dataInput = new JTextField();
	private JButton serialPortOperate = new JButton("关闭串口");
	private JButton sendData = new JButton("发送数据");
   
	byte[] data;
	private List<String> commList = null;
	private SerialPort serialport;
	public SocketChannel SocketCli = null;
	public String fitemid;
	private String com;
	public int count;
	public boolean first = true;
	public byte[] datashort;

	public MainFrame() {
		new Thread(cli).start();
		
		initView();
		initComponents();
		actionListener();
		initData();
		sendData();
		
		//测试模拟焊机
		/*try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		tExit = new Timer();  
        tExit.schedule(new TimerTask() {  
            @Override  
            public void run() {
            	
            	Date timetran;
            	long timetran1;
            	Date time11;
            	long timetran2;
            	Date time22;
            	long timetran3;
            	Date time33;
            	
            	timetran = new Date();
            	timetran1 = timetran.getTime();
                time11 = new Date(timetran1);
                timetran2 = timetran1 + 1000;
                time22 = new Date(timetran2);
                timetran3 = timetran2 + 1000;
                time33 = new Date(timetran3);
                
                String time1 = DateTools.format("yyMMddHHmmss", time11);
                String time2 = DateTools.format("yyMMddHHmmss", time22);
                String time3 = DateTools.format("yyMMddHHmmss", time33);
                
                String year1 = time1.substring(0,2);
                String year161 = Integer.toHexString(Integer.valueOf(year1));
                year161=year161.toUpperCase();
                if(year161.length()==1){
                	year161='0'+year161;
              	}
                String month1 = time1.substring(2,4);
                String month161 = Integer.toHexString(Integer.valueOf(month1));
                month161=month161.toUpperCase();
                if(month161.length()==1){
                	month161='0'+month161;
              	}
                String day1 = time1.substring(4,6);
                String day161 = Integer.toHexString(Integer.valueOf(day1));
                day161=day161.toUpperCase();
                if(day161.length()==1){
                	day161='0'+day161;
              	}
                String hour1 = time1.substring(6,8);
                String hour161 = Integer.toHexString(Integer.valueOf(hour1));
                hour161=hour161.toUpperCase();
                if(hour161.length()==1){
                	hour161='0'+hour161;
              	}
                String minute1 = time1.substring(8,10);
                String minute161 = Integer.toHexString(Integer.valueOf(minute1));
                minute161=minute161.toUpperCase();
                if(minute161.length()==1){
                	minute161='0'+minute161;
              	}
                String second1 = time1.substring(10,12);
                String second161 = Integer.toHexString(Integer.valueOf(second1));
                second161=second161.toUpperCase();
                if(second161.length()==1){
                	second161='0'+second161;
              	}
                
                String year2 = time2.substring(0,2);
                String year162 = Integer.toHexString(Integer.valueOf(year2));
                year162=year162.toUpperCase();
                if(year162.length()==1){
                	year162='0'+year162;
              	}
                String month2 = time2.substring(2,4);
                String month162 = Integer.toHexString(Integer.valueOf(month2));
                month162=month162.toUpperCase();
                if(month162.length()==1){
                	month162='0'+month162;
              	}
                String day2 = time2.substring(4,6);
                String day162 = Integer.toHexString(Integer.valueOf(day2));
                day162=day162.toUpperCase();
                if(day162.length()==1){
                	day162='0'+day162;
              	}
                String hour2 = time2.substring(6,8);
                String hour162 = Integer.toHexString(Integer.valueOf(hour2));
                hour162=hour162.toUpperCase();
                if(hour162.length()==1){
                	hour162='0'+hour162;
              	}
                String minute2 = time2.substring(8,10);
                String minute162 = Integer.toHexString(Integer.valueOf(minute2));
                minute162=minute162.toUpperCase();
                if(minute162.length()==1){
                	minute162='0'+minute162;
              	}
                String second2 = time2.substring(10,12);
                String second162 = Integer.toHexString(Integer.valueOf(second2));
                second162=second162.toUpperCase();
                if(second162.length()==1){
                	second162='0'+second162;
              	}
                
                String year3 = time3.substring(0,2);
                String year163 = Integer.toHexString(Integer.valueOf(year3));
                year163=year163.toUpperCase();
                if(year163.length()==1){
                	year163='0'+year163;
              	}
                String month3 = time3.substring(2,4);
                String month163 = Integer.toHexString(Integer.valueOf(month3));
                month163=month163.toUpperCase();
                if(month163.length()==1){
                	month163='0'+month163;
              	}
                String day3 = time3.substring(4,6);
                String day163 = Integer.toHexString(Integer.valueOf(day3));
                day163=day163.toUpperCase();
                if(day163.length()==1){
                	day163='0'+day163;
              	}
                String hour3 = time3.substring(6,8);
                String hour163 = Integer.toHexString(Integer.valueOf(hour3));
                hour163=hour163.toUpperCase();
                if(hour163.length()==1){
                	hour163='0'+hour163;
              	}
                String minute3 = time3.substring(8,10);
                String minute163 = Integer.toHexString(Integer.valueOf(minute3));
                minute163=minute163.toUpperCase();
                if(minute163.length()==1){
                	minute163='0'+minute163;
              	}
                String second3 = time3.substring(10,12);
                String second163 = Integer.toHexString(Integer.valueOf(second3));
                second163=second163.toUpperCase();
                if(second163.length()==1){
                	second163='0'+second163;
              	}
		    	
              //FA000031010009000100000011007B00A400000311091D0C050C007B00A500000311091D14050C007B00A400000311091D14050CC0F5
                
                String datesend = "000031010009000100000011007B00A4000003" + year161 + month161 + day161 + hour161 + minute161 + second161
                		+ "007B00A5000003" + year162 + month162 + day162 + hour162 + minute162 + second162
                		+ "007B00A4000003" + year163 + month163 + day163 + hour163 + minute163 + second163;
                          
                int check = 0;
                byte[] data1=new byte[datesend.length()/2];
    			for (int i = 0; i < data1.length; i++)
    			{
    				String tstr1=datesend.substring(i*2, i*2+2);
    				Integer k=Integer.valueOf(tstr1, 16);
    				check += k;
    			}

    			String checksend = Integer.toHexString(check);
    			int a = checksend.length();
    			checksend = checksend.substring(a-2,a);
    			checksend = checksend.toUpperCase();
    			
    			datesend = "FA" + datesend + checksend + "F5";
    			datesend = datesend.toUpperCase();
    			
    			try {
					SocketCli.writeAndFlush(datesend).sync();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    			count++;
    			System.out.println(count);
    			
    			if(count == 1200){
    				tExit.cancel();
    				System.out.println("Done");
    			}
            }  
        }, 0,3000); */
		
		/*//创建数据库
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:Sqlite.db");
	      System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	      
	      String sql = "create table Tenghan (" +
	  			"electricity test, " +
				"voltage test, " +
				"sensor_Num test, " +
				"machine_id test, " +
				"welder_id test, " +
				"code test, " +
				"year test, " +
				"month test, " +
				"day test, " +
				"hour test, " +
				"minute test, " +
				"second test, " +
				"status test)"; 
	      stmt.executeUpdate(sql);
	      System.out.println("create table successfully");
	    }catch ( Exception e ) {
	    	System.out.println("The table has exist");
		}*/
	}

	public Runnable cli =new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			client.run();
		 }
	};
	
	private void initView() {
		// 关闭程序
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		// 禁止窗口最大化
		setResizable(false);

		// 设置程序窗口居中显示
		Point p = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getCenterPoint();
		setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, WIDTH, HEIGHT);
		this.setLayout(null);

		setTitle("串口通讯");
	}

	private void initComponents() {
		// 数据显示
		dataView.setFocusable(false);
		dataView.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(dataView.getLineCount() >= 1000){
							int end = 0;
							try{
								end = dataView.getLineEndOffset(500);
							}catch (Exception e) {  
                            }  
							dataView.replaceRange("", 0, end);
						}
					}
				});
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		scrollDataView.setBounds(10, 10, 475, 200);
		add(scrollDataView);

		// 串口设置
		serialPortPanel.setBorder(BorderFactory.createTitledBorder("串口设置"));
		serialPortPanel.setBounds(10, 220, 170, 100);
		serialPortPanel.setLayout(null);
		add(serialPortPanel);

		serialPortLabel.setForeground(Color.gray);
		serialPortLabel.setBounds(10, 25, 40, 20);
		serialPortPanel.add(serialPortLabel);

		commChoice.setFocusable(false);
		commChoice.setBounds(60, 25, 100, 20);
		serialPortPanel.add(commChoice);

		baudrateLabel.setForeground(Color.gray);
		baudrateLabel.setBounds(10, 60, 40, 20);
		serialPortPanel.add(baudrateLabel);

		baudrateChoice.setFocusable(false);
		baudrateChoice.setBounds(60, 60, 100, 20);
		serialPortPanel.add(baudrateChoice);

		// 操作
		operatePanel.setBorder(BorderFactory.createTitledBorder("操作"));
		operatePanel.setBounds(200, 220, 285, 100);
		operatePanel.setLayout(null);
		add(operatePanel);

		serialPortOperate.setFocusable(false);
		serialPortOperate.setBounds(155, 40, 90, 30);
		operatePanel.add(serialPortOperate);

		sendData.setFocusable(false);
		sendData.setBounds(45, 40, 90, 30);
		operatePanel.add(sendData);
	}

	@SuppressWarnings("unchecked")
	private void initData() {
		commList = SerialPortManager.findPort();
		// 检查是否有可用串口，有则加入选项中
		if (commList == null || commList.size() < 1) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			for (String s : commList) {
				//commChoice.addItem(s);
			}
		}

		baudrateChoice.addItem("38400");
	}

	private void actionListener() {
		serialPortOperate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if ("关闭串口".equals(serialPortOperate.getText())
						&& serialport == null) {
					//openSerialPort(e);
				} else {
					closeSerialPort(e);
				}
			}
		});

		/*sendData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendData(e);
			}
		});*/
	}


	/**
	 * 打开串口
	 * 
	 * @param evt
	 *            点击事件
	 *//*
	private void openSerialPort(java.awt.event.ActionEvent evt) {
		// 获取串口名称
		String commName = (String) commChoice.getSelectedItem();
		// 获取波特率
		int baudrate = 9600;
		String bps = (String) baudrateChoice.getSelectedItem();
		baudrate = Integer.parseInt(bps);

		// 检查串口名称是否获取正确
		if (commName == null || commName.equals("")) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			try {
				serialport = SerialPortManager.openPort(commName, baudrate);
				if (serialport != null) {
					dataView.setText("串口已打开" + "\r\n");
					serialPortOperate.setText("关闭串口");
				}
			} catch (SerialPortParameterFailure e) {
				e.printStackTrace();
			} catch (NotASerialPort e) {
				e.printStackTrace();
			} catch (NoSuchPort e) {
				e.printStackTrace();
			} catch (PortInUse e) {
				e.printStackTrace();
				ShowUtils.warningMessage("串口已被占用！");
			}
		}

		try {
			SerialPortManager.addListener(serialport, new SerialListener());
		} catch (TooManyListeners e) {
			e.printStackTrace();
		}
	}*/

	/**
	 * 关闭串口
	 * 
	 * @param evt
	 *            点击事件
	 */
	private void closeSerialPort(java.awt.event.ActionEvent evt) {
		serialport.removeEventListener();
		tExit.cancel();
		SerialPortManager.closePort(serialport);
		dataView.setText("串口已关闭" + "\r\n");
		serialPortOperate.setText("关闭串口");
	}

	/**
	 * 发送数据
	 * @param e 
	 * 
	 * @param evt
	 *            点击事件
	 */
	private void sendData() {
		
		//初始化
		OutputStream out = null;
		byte[] order = null;
		InputStream in = null;
		byte[] bytes = null;
		
		try {
			FileInputStream in1 = new FileInputStream("IPconfig.txt");  
            InputStreamReader inReader = new InputStreamReader(in1, "UTF-8");  
            BufferedReader bufReader = new BufferedReader(inReader);  
            String line = null; 
            int writetime=0;
			
		    while((line = bufReader.readLine()) != null){ 
		    	if(writetime==0){
	                IP=line;
	                writetime++;
		    	}
		    	else if(writetime==1){
		    		fitemid=line;
		    		writetime++;
		    	}else{
		    		com=line;
		    		writetime=0;

		    		commChoice.addItem(com);
		    	}
            }  

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		   
		if(fitemid.length()!=2){
     		int count = 2-fitemid.length();
     		for(int i=0;i<count;i++){
     			fitemid="0"+fitemid;
     		}
     	}
		
		
		//打开串口
		String commName = (String) commChoice.getSelectedItem();
		// 获取波特率
		int baudrate = 9600;
		String bps = (String) baudrateChoice.getSelectedItem();
		baudrate = Integer.parseInt(bps);

		// 检查串口名称是否获取正确
		if (commName == null || commName.equals("")) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			try {
				/*if(serialport!=null){
					serialport.removeEventListener();
					serialport.close();
				}*/
				serialport = SerialPortManager.openPort(commName, baudrate);
				serialport.setInputBufferSize(2048);
				if (serialport != null) {
					dataView.setText("串口已打开" + "\r\n");
				}
				
				
				try {
					SerialPortManager.addListener(serialport, new SerialListener());
				} catch (TooManyListeners e) {
					e.printStackTrace();
				}
		    	
				tExit = new Timer();  
		        tExit.schedule(new TimerTask() {  
		            @Override  
		            public void run() { 
						Date date=new Date();
				    	SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
				    	String time=format.format(date);
				    	String time1="";
				    	String time2="";
				    	
				    	for (int i = 0; i < time.length()/2; i++)
						{
							String tstr1=time.substring(i*2, i*2+2);
							Integer k=Integer.valueOf(tstr1).intValue();
							time1=Integer.toHexString(k);
							time1=time1.toUpperCase();
							if(time1.length()==1){
				    			time1='0'+time1;
				        	}
							time2+=time1;
						}
				    	
				    	String data1 = "FAFFFF0804";
				    	String data2 = time2;
				    	String data3 = data1+data2;
				    	int data4 = 0;
				    	
					     for (int i11 = 1; i11 < data3.length()/2; i11++)
					     {
					    	String tstr1=data3.substring(i11*2, i11*2+2);
					    	data4+=Integer.valueOf(tstr1,16);
					     }
					    String data5 = ((Integer.toHexString(data4)).toUpperCase()).substring(1,3);
				    	String data  = data1+data2+data5+"F5";
				    	try {
							SerialPortManager.sendToPort(serialport,
									ByteUtils.hexStr2Byte(data));
						} catch (SendDataToSerialPortFailure e) {
							e.printStackTrace();
						} catch (SerialPortOutputStreamCloseFailure e) {
							e.printStackTrace();
						}
		            }  
		        }, 0,3000); 
				
				
				
			} catch (SerialPortParameterFailure e) {
				e.printStackTrace();
			} catch (NotASerialPort e) {
				e.printStackTrace();
			} catch (NoSuchPort e) {
				e.printStackTrace();
			} catch (PortInUse e) {
				e.printStackTrace();
				ShowUtils.warningMessage("串口已被占用！");
			}
		}

		
		//读取数据
		try {
			in = serialport.getInputStream();
			// 获取buffer里的数据长度
			int bufflenth = in.available();
			while (bufflenth != 0) {
				// 初始化byte数组为buffer中数据的长度
				bytes = new byte[bufflenth];
				in.read(bytes);
				bufflenth = in.available();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	
    	
    	
    	/*Timer tExit = null; 
		tExit = new Timer();  
        tExit.schedule(new TimerTask() {  
            @Override  
            public void run() {  
            	Date date=new Date();
            	SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
            	String time=format.format(date);
            	String data1 = "FEFE04";
            	String data2 = time;
            	String data3 = "010101FD";
            	String data  = data1+data2+data3;
            	try {
        			SerialPortManager.sendToPort(serialport,
        					ByteUtils.hexStr2Byte(data));
        		} catch (SendDataToSerialPortFailure e) {
        			e.printStackTrace();
        		} catch (SerialPortOutputStreamCloseFailure e) {
        			e.printStackTrace();
        		}
            }  
        }, 0,30*100); */
			
		
		/*Timer tExit = null; 
		tExit = new Timer();  
        tExit.schedule(new TimerTask() {  
            @Override  
            public void run() {  
            	String data = "FE0000000000000000000000FD";
            	try {
        			SerialPortManager.sendToPort(serialport,
        					ByteUtils.hexStr2Byte(data));
        		} catch (SendDataToSerialPortFailure e) {
        			e.printStackTrace();
        		} catch (SerialPortOutputStreamCloseFailure e) {
        			e.printStackTrace();
        		}
            }  
        }, 0,30*100); */
        
		
		/*String data = dataInput.getText().toString();
		try {
			SerialPortManager.sendToPort(serialport,
					ByteUtils.hexStr2Byte(data));
		} catch (SendDataToSerialPortFailure e) {
			e.printStackTrace();
		} catch (SerialPortOutputStreamCloseFailure e) {
			e.printStackTrace();
		}*/
	}

	private class SerialListener implements SerialPortEventListener {
		/**
		 * 处理监控到的串口事件
		 */
		public void serialEvent(SerialPortEvent serialPortEvent) {

			switch (serialPortEvent.getEventType()) {

			case SerialPortEvent.BI: // 10 通讯中断
				ShowUtils.errorMessage("与串口设备通讯中断");
				break;

			case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
				
				
				try {
					Thread.sleep(20);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				
				InputStream in1 = null;
				
				/*try {
					in1 = serialport.getInputStream();
					//获取buffer里的数据长度
					int bufflenth = in1.available();

					while (bufflenth > 0 && (bufflenth > 54 || bufflenth == 54)) {                             
		                data = new byte[54];    //初始化byte数组为buffer中数据的长度
		                try {
							in1.read(data);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                new Thread(new soctran(data,client,fitemid,SocketCli,dataView)).start();
		                bufflenth = bufflenth-54;
		            }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
				
				try {
					Thread.sleep(2900);
					//FA00003101000100010000000100130011000003120B1800000000130011000003120B1800000000130011000003120B180000001AF5
					//FA00003101000100010000000100130011000003120B1800000000130011000003120B1800000000130011000003120B180000001aF5
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				//读取串口数据处理
				try {
					in1 = serialport.getInputStream();
					//获取buffer里的数据长度
					int bufflenth = in1.available();
                             
	                data = new byte[bufflenth];    //初始化byte数组为buffer中数据的长度
	                try {
						in1.read(data,0,bufflenth);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				int length = 0;
				int count = 0;
				
				for(int i=0;i<data.length;i++){
					if(first){
						if(data[i] == -6 && (data.length - i > 54 || data.length - i == 54)){
							byte[] databuf = new byte[54];
							for(int j=0;j<54;j++){
								databuf[j] = data[j+i];
							}
							new Thread(new soctran(databuf,client,fitemid,SocketCli,dataView)).start();
							i = i + 53;
						}
						first = false;
					}else{
						if(data[i] == -6 && (data.length - i > 54 || data.length - i == 54)){
							if(datashort != null && datashort.length != 0){
								datashort = null;
							}
							byte[] databuf = new byte[54];
							for(int j=0;j<54;j++){
								databuf[j] = data[j+i];
							}
							new Thread(new soctran(databuf,client,fitemid,SocketCli,dataView)).start();
							i = i + 53;
						}else{
							if(datashort == null || datashort.length == 0){
								datashort = new byte[data.length - i];
								for(int j=0;j<data.length-i;j++){
									datashort[j] = data[j+i];
								}
								i = i + datashort.length - 1; 
							}else{
								try{
									if(data[i + 54-datashort.length - 1] == -11){
										byte[] databuf = new byte[54];
										for(int k=0;k<datashort.length;k++){
											databuf[k] = datashort[k];
										}
										for(int l=0;l<54-datashort.length;l++){
											databuf[l+datashort.length] = data[l];
										}
										new Thread(new soctran(databuf,client,fitemid,SocketCli,dataView)).start();
										int len = datashort.length;
										datashort = null;
										i = i + 54-len - 1;
									}else{
										for(int i1=0;i1<data.length;i1++){
											if(data[i1] == -11){
												i = i + i1;
												break;
											}
										}
									}
								}catch(Exception e){
									byte[] buf = datashort;
									datashort = new byte[datashort.length+data.length];
									for(int i1=0;i1<(buf.length);i1++){
										datashort[i1] = buf[i1];
									}
									for(int i1=0;i1<data.length;i1++){
										datashort[i1+buf.length] = data[i1];
									}
									i = i + data.length - 1;
								}
							}
						}
					}
				}
				
				/*for(int i=0;i<data.length;i++){
					if(data[i] == -11){
						length = i - length * count + 1;
						if(length>54 || length==54){
							byte[] databuf = new byte[length];
							for(int j=0;j<length;j++){
								databuf[j] = data[j+i-length+1];
							}
							//soctran = new soctran(databuf,client,fitemid,SocketCli,dataView);
							new Thread(new soctran(databuf,client,fitemid,SocketCli,dataView)).start();
							count++;
						}
					}
				}*/
				
				//new Thread(sqlite).start();
				//new Thread(websocketstart).start();
			
				
			}
		}
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new MainFrame().setVisible(true);
			}
		});
	}
	
	public Runnable sqlite = new Runnable() {
			public void run() {
				String strdata = "";
				String insql;
			    // 接收服务器发送过来的消息
			    String response;
			    
			    
				try {
					if (serialport == null) {
						ShowUtils.errorMessage("串口对象为空！监听失败！");
					}
					
					else {
						dataView.append(ByteUtils.byteArrayToHexString(data,
								true) + "\r\n");
						
						 for(int i=0;i<data.length;i++){
                         	
                         	//判断为数字还是字母，若为字母+256取正数
                         	if(data[i]<0){
                         		String r = Integer.toHexString(data[i]+256);
                         		String rr=r.toUpperCase();
                             	System.out.print(rr);
                             	//数字补为两位数
                             	if(rr.length()==1){
                         			rr='0'+rr;
                             	}
                             	//strdata为总接收数据
                         		strdata += rr;
                         		
                         	}
                         	else{
                         		String r = Integer.toHexString(data[i]);
                             	System.out.print(r);
                             	if(r.length()==1)
                         			r='0'+r;
                         		strdata+=r;	
                         		
                         	}
                         }
                         
                         response=strdata;
                         
                         //数据写入数据库
                         String [] stringArr = strdata.split("FD");
                         for(int i =0;i < stringArr.length;i++)
         		        {
                      		String electricity = stringArr[i].subSequence(5, 9).toString();
                            String voltage = stringArr[i].subSequence(9, 13).toString();
                            String sensor_Num = stringArr[i].subSequence(13, 17).toString();
                            String machine_id = stringArr[i].subSequence(17, 21).toString();
                            String welder_id = stringArr[i].subSequence(21, 25).toString();
                            String code = stringArr[i].subSequence(25, 33).toString();
                            String year = stringArr[i].subSequence(33, 35).toString();
                            String month = stringArr[i].subSequence(35, 37).toString();
                            String day = stringArr[i].subSequence(37, 39).toString();
                            String hour = stringArr[i].subSequence(39, 41).toString();
                            String minute = stringArr[i].subSequence(41, 43).toString();
                            String second = stringArr[i].subSequence(43, 45).toString();
                            String status = stringArr[i].subSequence(45, 47).toString();
                            
                            insql = "insert into Tenghan(electricity,voltage,sensor_Num,machine_id,welder_id,code,year,month,day,hour,minute,second,status) "
                            		+ "values('"+ electricity +"','" + voltage + "','" + sensor_Num + "'"
                            				+ ","+ "'" + machine_id + "','" + welder_id + "','" + code + "'"
                            						+ ",'" + year + "','" + month + "','" + day + "','" + hour + "'"
                            								+ ",'" + minute + "','" + second + "','" + status + "')";
                            stmt.executeUpdate(insql);

         		        }
                         System.out.println("The data has writed");
                         
					}
				} catch (Exception e) {
					ShowUtils.errorMessage(e.toString());
					// 发生读取错误时显示错误信息后退出系统
					System.exit(0);
				}
			}
	 };
	 
	 public Runnable soctran = new Runnable() {
			public void run() {
				
				Bootstrap c = client.bootstrap;
				
				String strdata = "";
				String insql;
			    // 接收服务器发送过来的消息
			    String response;
				
				try {
					if (serialport == null) {
						ShowUtils.errorMessage("串口对象为空！监听失败！");
					}
					
					else {
						
						if(data==null){
							
						}
						else
						{			
							 for(int i=0;i<data.length;i++){
	                         	
	                         	//判断为数字还是字母，若为字母+256取正数
	                         	if(data[i]<0){
	                         		String r = Integer.toHexString(data[i]+256);
	                         		String rr=r.toUpperCase();
	                             	//数字补为两位数
	                             	if(rr.length()==1){
	                         			rr='0'+rr;
	                             	}
	                             	//strdata为总接收数据
	                         		strdata += rr;
	                         		
	                         	}
	                         	else{
	                         		String r = Integer.toHexString(data[i]);
	                             	if(r.length()==1)
	                         			r='0'+r;
	                             	r=r.toUpperCase();
	                         		strdata+=r;	
	                         		
	                         	}
	                         }
							 
	     		            try {    
	     		            	/*if(SocketCli!=null){
	     		            		try {
	    	     						FileInputStream in = new FileInputStream("IPconfig.txt");  
	    	     			            InputStreamReader inReader = new InputStreamReader(in, "UTF-8");  
	    	     			            BufferedReader bufReader = new BufferedReader(inReader);  
	    	     			            String line = null; 
	    	     			            int writetime=0;
	    	     						
	    	     					    while((line = bufReader.readLine()) != null){ 
	    	     					    	if(writetime==0){
	    	     				                IP=line;
	    	     				                writetime++;
	    	     					    	}
	    	     					    	else{
	    	     					    		fitemid=line;
	    	     					    		writetime=0;
	    	     					    	}
	    	     			            }  

	    	     					} catch (FileNotFoundException e) {
	    	     						// TODO Auto-generated catch block
	    	     						e.printStackTrace();
	    	     					} catch (IOException e) {
	    	     						// TODO Auto-generated catch block
	    	     						e.printStackTrace();
	    	     					} 
	    	     					   
	    	     					if(fitemid.length()!=2){
	         		            		int count = 2-fitemid.length();
	         		            		for(int i=0;i<count;i++){
	         		            			fitemid="0"+fitemid;
	         		            		}
	         		            	}
	     		            		
	     		            		socketChannel = SocketChannel.open(); 
		     		                SocketAddress socketAddress = new InetSocketAddress(IP, 5555);    
		     		                socketChannel.connect(socketAddress);
	     		            	}*/
	     		            	
	     		            	//数字化焊机
	     		            	if(strdata.length() == 168){
	     		            		strdata=strdata.substring(0,166)+fitemid+"F5";
	     		            	}
	     		            	
	     		            	//核五旧正常
	     		            	if(strdata.length() == 108){
	     		            		strdata=strdata.substring(0,106)+fitemid+"F5";
	     		            	}
	     		            	
	     		            	/*byte[] data=new byte[strdata.length()/2];
	     		                 for (int i1 = 0; i1 < data.length; i1++)
	     		                 {
	     		                   String tstr1=strdata.substring(i1*2, i1*2+2);
	     		                   Integer k=Integer.valueOf(tstr1, 16);
	     		                   data[i1]=(byte)k.byteValue();
	     		                 }*/
	     		            	try{
	     		            		SocketCli.writeAndFlush(strdata).sync();
	     		            		dataView.append(strdata + "\r\n");
	     		            	} catch (Exception ex) {  
		     		            	dataView.setText("服务器未开启" + "\r\n");
		     		            	ex.printStackTrace();
		     		            }
	     		                
	     		                    
	     		                /*String msg = SendAndReceiveUtil.receiveData(socketChannel);    
	     		                if(msg != null) 
	     		                	System.out.println(msg);*/
  
	     		            
	     		            } catch (Exception ex) {  
	     		            	ex.printStackTrace();
	     		            } /*finally {    
	     		                try {            
	     		                    socketChannel.close();    
	     		                } catch(Exception ex) {  
	     		                    ex.printStackTrace();  
	     		                }    
	     		            }*/
	     					
	     					/*if(socket==null){
	     						try {
									socket = new Socket(IP, 5555);
								} catch (IOException e1) {
									dataView.setText("服务器连接失败" + "\r\n" + e1 + "\r\n");
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
	     					}
	     					
	                        
	                        if(socket!=null){
	                        	System.out.print("服务器连接成功"+ "\r\n");
	                        }
	                        else{
	                        	dataView.setText("服务器连接失败" + "\r\n");
	                        	System.out.print("服务器连接失败"+ "\r\n");
	                        }
						
	                    try {
	                    	//发送消息
	                        // 步骤1：从Socket 获得输出流对象OutputStream
	                        // 该对象作用：发送数据
	                        outputStream = socket.getOutputStream();
	
	                        // 步骤2：写入需要发送的数据到输出流对象中
							dataView.append(ByteUtils.byteArrayToHexString(data,
									true) + "\r\n");
	                        outputStream.write(bb3);
	                        // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
	
	                        // 步骤3：发送数据到服务端
	                        outputStream.flush();
	                        
	                       
	     		           
	                    } catch (IOException e1) {
	                        e1.printStackTrace();
	                    }*/
						}
					}
				}catch (Exception e) {

					}
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
	 };
				
 }  
	
	class soctran implements Runnable {

		private byte[] data;
		private Client client;
		private String fitemid;
		private SocketChannel SocketCli;
		private JTextArea dataView;

		public soctran(byte[] databuf, Client client1) {
			// TODO Auto-generated constructor stub
			
		}

		public soctran(byte[] databuf, Client client1, String fitemid1, SocketChannel SocketCli1, JTextArea dataView1) {
			// TODO Auto-generated constructor stub
			data = databuf;
			client = client1;
			fitemid = fitemid1;
			SocketCli = SocketCli1;
			dataView = dataView1;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Bootstrap c = client.bootstrap;
			
			String strdata = "";
			String insql;
		    // 接收服务器发送过来的消息
		    String response;
			
			try {
					
				if(data==null){
					
				}
				else
				{			
					 for(int i=0;i<data.length;i++){
                     	
                     	//判断为数字还是字母，若为字母+256取正数
                     	if(data[i]<0){
                     		String r = Integer.toHexString(data[i]+256);
                     		String rr=r.toUpperCase();
                         	//数字补为两位数
                         	if(rr.length()==1){
                     			rr='0'+rr;
                         	}
                         	//strdata为总接收数据
                     		strdata += rr;
                     		
                     	}
                     	else{
                     		String r = Integer.toHexString(data[i]);
                         	if(r.length()==1)
                     			r='0'+r;
                         	r=r.toUpperCase();
                     		strdata+=r;	
                     		
                     	}
                     }
					 
 		            try {    
 		            	/*if(SocketCli!=null){
 		            		try {
	     						FileInputStream in = new FileInputStream("IPconfig.txt");  
	     			            InputStreamReader inReader = new InputStreamReader(in, "UTF-8");  
	     			            BufferedReader bufReader = new BufferedReader(inReader);  
	     			            String line = null; 
	     			            int writetime=0;
	     						
	     					    while((line = bufReader.readLine()) != null){ 
	     					    	if(writetime==0){
	     				                IP=line;
	     				                writetime++;
	     					    	}
	     					    	else{
	     					    		fitemid=line;
	     					    		writetime=0;
	     					    	}
	     			            }  

	     					} catch (FileNotFoundException e) {
	     						// TODO Auto-generated catch block
	     						e.printStackTrace();
	     					} catch (IOException e) {
	     						// TODO Auto-generated catch block
	     						e.printStackTrace();
	     					} 
	     					   
	     					if(fitemid.length()!=2){
     		            		int count = 2-fitemid.length();
     		            		for(int i=0;i<count;i++){
     		            			fitemid="0"+fitemid;
     		            		}
     		            	}
 		            		
 		            		socketChannel = SocketChannel.open(); 
     		                SocketAddress socketAddress = new InetSocketAddress(IP, 5555);    
     		                socketChannel.connect(socketAddress);
 		            	}*/
 		            	
 		            	//数字化焊机
 		            	if(strdata.length() == 168){
 		            		strdata=strdata.substring(0,166)+fitemid+"F5";
 		            	}
 		            	
 		            	//核五旧正常
 		            	if(strdata.length() == 108){
 		            		strdata=strdata.substring(0,106)+fitemid+"F5";
 		            	}
 		            	
 		            	/*byte[] data=new byte[strdata.length()/2];
 		                 for (int i1 = 0; i1 < data.length; i1++)
 		                 {
 		                   String tstr1=strdata.substring(i1*2, i1*2+2);
 		                   Integer k=Integer.valueOf(tstr1, 16);
 		                   data[i1]=(byte)k.byteValue();
 		                 }*/
 		            	try{
 		            		SocketCli.writeAndFlush(strdata).sync();
 		            		dataView.append(strdata + "\r\n");
 		            	} catch (Exception ex) {  
     		            	dataView.setText("服务器未开启" + "\r\n");
     		            	ex.printStackTrace();
     		            }
 		                
 		                    
 		                /*String msg = SendAndReceiveUtil.receiveData(socketChannel);    
 		                if(msg != null) 
 		                	System.out.println(msg);*/

 		            
 		            } catch (Exception ex) {  
 		            	ex.printStackTrace();
 		            } /*finally {    
 		                try {            
 		                    socketChannel.close();    
 		                } catch(Exception ex) {  
 		                    ex.printStackTrace();  
 		                }    
 		            }*/
 					
 					/*if(socket==null){
 						try {
							socket = new Socket(IP, 5555);
						} catch (IOException e1) {
							dataView.setText("服务器连接失败" + "\r\n" + e1 + "\r\n");
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
 					}
 					
                    
                    if(socket!=null){
                    	System.out.print("服务器连接成功"+ "\r\n");
                    }
                    else{
                    	dataView.setText("服务器连接失败" + "\r\n");
                    	System.out.print("服务器连接失败"+ "\r\n");
                    }
				
                try {
                	//发送消息
                    // 步骤1：从Socket 获得输出流对象OutputStream
                    // 该对象作用：发送数据
                    outputStream = socket.getOutputStream();

                    // 步骤2：写入需要发送的数据到输出流对象中
					dataView.append(ByteUtils.byteArrayToHexString(data,
							true) + "\r\n");
                    outputStream.write(bb3);
                    // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞

                    // 步骤3：发送数据到服务端
                    outputStream.flush();
                    
                   
 		           
                } catch (IOException e1) {
                    e1.printStackTrace();
                }*/
				}
			}catch (Exception e) {
				e.getStackTrace();
			}
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

