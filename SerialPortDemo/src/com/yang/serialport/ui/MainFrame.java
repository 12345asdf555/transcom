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

	public MainFrame() {
		new Thread(cli).start();
		initView();
		initComponents();
		actionListener();
		initData();
		
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
				commChoice.addItem(s);
			}
		}

		baudrateChoice.addItem("9600");
		baudrateChoice.addItem("19200");
		baudrateChoice.addItem("38400");
		baudrateChoice.addItem("57600");
		baudrateChoice.addItem("115200");
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

		sendData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendData(e);
			}
		});
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
		SerialPortManager.closePort(serialport);
		dataView.setText("串口已关闭" + "\r\n");
		serialPortOperate.setText("关闭串口");
	}

	/**
	 * 发送数据
	 * 
	 * @param evt
	 *            点击事件
	 */
	private void sendData(java.awt.event.ActionEvent evt) {
		
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
				if (serialport != null) {
					dataView.setText("串口已打开" + "\r\n");
				}
				
				
				try {
					SerialPortManager.addListener(serialport, new SerialListener());
				} catch (TooManyListeners e) {
					e.printStackTrace();
				}
		    	
				
				
				
				Timer tExit = null; 
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
				
				//读取串口数据处理
					try {
							in1 = serialport.getInputStream();
						//获取buffer里的数据长度
							int bufflenth = in1.available();
	
							while (bufflenth != 0) {                             
				                data = new byte[bufflenth];    //初始化byte数组为buffer中数据的长度
				                try {
									in1.read(data);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				                try {
									bufflenth = in1.available();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				            }
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					//new Thread(sqlite).start();
					new Thread(soctran).start();
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
	     		            	if(SocketCli!=null){
	     		            		/*try {
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
		     		                socketChannel.connect(socketAddress);*/
	     		            	}
	     		            	
	     		            	strdata=strdata.substring(0,106)+fitemid+"F5";
	     		            	
	     		            	/*byte[] data=new byte[strdata.length()/2];
	     		                 for (int i1 = 0; i1 < data.length; i1++)
	     		                 {
	     		                   String tstr1=strdata.substring(i1*2, i1*2+2);
	     		                   Integer k=Integer.valueOf(tstr1, 16);
	     		                   data[i1]=(byte)k.byteValue();
	     		                 }*/
	     		            	
	     		                SocketCli.writeAndFlush(strdata).sync();
	     		                
	     		                dataView.append(strdata + "\r\n");
	     		                    
	     		                /*String msg = SendAndReceiveUtil.receiveData(socketChannel);    
	     		                if(msg != null) 
	     		                	System.out.println(msg);*/
  
	     		            
	     		            } catch (Exception ex) {  
	     		            	dataView.setText("服务器未开启" + "\r\n");
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
	 
